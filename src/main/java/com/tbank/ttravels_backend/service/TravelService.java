package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.dto.travel.EditTravelRequest;
import com.tbank.ttravels_backend.dto.travel.TravelResponse;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.enums.TravelStatus;
import com.tbank.ttravels_backend.exception.ConflictStateException;
import com.tbank.ttravels_backend.exception.DuplicateExpenseException;
import com.tbank.ttravels_backend.exception.InvalidDateRangeException;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.factory.TravelFactory;
import com.tbank.ttravels_backend.factory.TravelMemberFactory;
import com.tbank.ttravels_backend.mapper.TravelMapper;
import com.tbank.ttravels_backend.repository.TravelMemberRepository;
import com.tbank.ttravels_backend.repository.TravelRepository;
import com.tbank.ttravels_backend.dto.notification.PushNotificationRequest;
import com.tbank.ttravels_backend.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelMemberRepository travelMemberRepository;
    private final TravelMapper travelMapper;
    private final AccountService accountService;
    private final NotificationService notificationService;

    @Transactional
    public TravelResponse createTravel(CreateTravelRequest request, Long userId) {
        User owner = accountService.findUser(userId);


        Travel newTravel = TravelFactory.createTravel(request, owner);

        Set<TravelMember> members = newTravel.getTravelMembers();
        members.add(TravelMemberFactory.createOwnerMember(newTravel, owner));

        Travel travel = saveTravel(newTravel);

        PushNotificationRequest notificationRequest = PushNotificationRequest.builder()
                .title("Поездка создана")
                .body("Поездка \"" + travel.getName() + "\" успешно создана")
                .userId(owner.getId())
                .data(Map.of(
                        "type", "TRAVEL_CREATED",
                        "travelId", travel.getId().toString()
                ))
                .build();

        notificationService.sendToUser(owner.getId(), notificationRequest);

        return travelMapper.toTravelResponse(travel);
    }

    public TravelResponse getTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        return travelMapper.toTravelResponse(travel);
    }

    @Transactional
    public TravelResponse editTravel(Long travelId, EditTravelRequest request) {
        Travel travel = findTravel(travelId);

        OffsetDateTime updatedStart = request.getStartDate() != null ? request.getStartDate() : travel.getStartDate();
        OffsetDateTime updatedEnd = request.getEndDate() != null ? request.getEndDate() : travel.getEndDate();
        validateDateRange(updatedStart, updatedEnd);

        applyUpdates(travel, request);
        Travel updated = saveTravel(travel);

        return travelMapper.toTravelResponse(updated);
    }

    @Transactional
    public void closeTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        if (travel.getStatus() == TravelStatus.CLOSED) {
            throw new ConflictStateException("Поездка уже закрыта");
        }

        travel.setStatus(TravelStatus.CLOSED);
        saveTravel(travel);

        for (TravelMember member : travel.getTravelMembers()) {
            User user = member.getUser();
            if (user == null || user.getId() == null) {
                continue;
            }

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title("Поездка закрыта")
                    .body("Поездка \"" + travel.getName() + "\" была закрыта")
                    .userId(user.getId())
                    .data(Map.of(
                            "type", "TRAVEL_CLOSED",
                            "travelId", travel.getId().toString()
                    ))
                    .build();

            notificationService.sendToUser(user.getId(), request);
        }
    }

    @Transactional
    public void reopenTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        if (travel.getStatus() == TravelStatus.ACTIVE) {
            throw new ConflictStateException("Поездка уже открыта");
        }
        travel.setStatus(TravelStatus.ACTIVE);
        saveTravel(travel);

        for (TravelMember member : travel.getTravelMembers()) {
            User user = member.getUser();
            if (user == null || user.getId() == null) {
                continue;
            }

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title("Поездка снова открыта")
                    .body("Поездка \"" + travel.getName() + "\" была снова открыта")
                    .userId(user.getId())
                    .data(Map.of(
                            "type", "TRAVEL_REOPENED",
                            "travelId", travel.getId().toString()
                    ))
                    .build();

            notificationService.sendToUser(user.getId(), request);
        }
    }

    @Transactional
    public void deleteTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        travelRepository.delete(travel);
    }

    private void applyUpdates(Travel travel, EditTravelRequest request) {
        if (request.getName() != null) {
            travel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            travel.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            travel.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            travel.setEndDate(request.getEndDate());
        }
    }

    public Travel saveTravel(Travel travel) {
        return travelRepository.save(travel);
    }

    public Travel findTravel(Long travelId) {
        return travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка с id = " + travelId + " не найдена"));
    }

    public void addExpense(Travel travel, Expense expense) {
        checkTravelAndExpenseIsNull(travel, expense);

        if (travel.getExpenses().contains(expense)) {
            throw new DuplicateExpenseException("Трата с id = " + expense.getId() +
                    "уже существует в поездке '" + travel.getName() + "'");
        }

        travel.getExpenses().add(expense);
        expense.setTravel(travel);
    }

    public void removeExpense(Travel travel, Expense expense) {

        checkTravelAndExpenseIsNull(travel, expense);

        travel.getExpenses().remove(expense);
        expense.setTravel(null);
    }

    public void addTransfer(Travel travel, Transfer transfer) {
        checkTravelAndTransferIsNull(travel, transfer);

        travel.getTransfers().add(transfer);
        transfer.setTravel(travel);
    }

    public void removeTransfer(Travel travel, Transfer transfer) {
        checkTravelAndTransferIsNull(travel, transfer);

        travel.getTransfers().remove(transfer);
        transfer.setTravel(null);
    }

    public void addTravelMember(Travel travel, TravelMember travelMember) {
        checkTravelAndMemberIsNull(travel, travelMember);

        travel.getTravelMembers().add(travelMember);
        travelMember.setTravel(travel);

        User user = travelMember.getUser();
        if (user != null && user.getId() != null) {
            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title("Вы добавлены в поездку")
                    .body("Вы были добавлены в поездку \"" + travel.getName() + "\"")
                    .userId(user.getId())
                    .data(Map.of(
                            "type", "TRAVEL_MEMBER_ADDED",
                            "travelId", travel.getId().toString()
                    ))
                    .build();

            notificationService.sendToUser(user.getId(), request);
        }
    }

    private void checkTravelAndMemberIsNull(Travel travel, TravelMember travelMember) {
        if (travelMember == null) {
            throw new IllegalArgumentException("Travel member is null");
        }
        if (travel == null) {
            throw new IllegalArgumentException("Travel is null");
        }
    }

    private void checkTravelAndTransferIsNull(Travel travel, Transfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer is null");
        }
        if (travel == null) {
            throw new IllegalArgumentException("Travel is null");
        }
    }

    private void validateDateRange(OffsetDateTime start, OffsetDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new InvalidDateRangeException("Дата окончания должна быть позже даты начала");
        }
    }

    private void checkTravelAndExpenseIsNull(Travel travel, Expense expense) {

        if (expense == null) {
            throw new IllegalArgumentException("Expense is null");
        }
        if (travel == null) {
            throw new IllegalArgumentException("Travel is null");
        }
    }

    public void checkTravel(Long travelId) {
        travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Поездка с id = " + travelId + " не найдена"));
    }
}
