package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.dto.travel.EditTravelRequest;
import com.tbank.ttravels_backend.dto.travel.TravelResponse;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.TravelMember;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.TravelStatus;
import com.tbank.ttravels_backend.exception.ConflictStateException;
import com.tbank.ttravels_backend.exception.InvalidDateRangeException;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.factory.TravelFactory;
import com.tbank.ttravels_backend.factory.TravelMemberFactory;
import com.tbank.ttravels_backend.mapper.TravelMapper;
import com.tbank.ttravels_backend.repository.TravelRepository;
import com.tbank.ttravels_backend.security.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelMapper travelMapper;
    private final AccountService accountService;

    @Transactional
    public TravelResponse createTravel(CreateTravelRequest request, Long userId) {
        User owner = accountService.findUser(userId);


        Travel newTravel = TravelFactory.createTravel(request, owner);

        Set<TravelMember> members = newTravel.getTravelMembers();
        members.add(TravelMemberFactory.ownerMembership(newTravel, owner));

        Travel travel = saveTravel(newTravel);

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
    }

    @Transactional
    public void reopenTravel(Long travelId) {
        Travel travel = findTravel(travelId);
        if (travel.getStatus() == TravelStatus.ACTIVE) {
            throw new ConflictStateException("Поездка уже открыта");
        }
        travel.setStatus(TravelStatus.ACTIVE);
        saveTravel(travel);
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
                .orElseThrow(() -> new TravelNotFoundException(travelId));
    }

    private void validateDateRange(OffsetDateTime start, OffsetDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new InvalidDateRangeException("Дата окончания должна быть позже даты начала");
        }
    }
}
