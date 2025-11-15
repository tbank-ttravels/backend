package com.tbank.ttravels_backend.service;


import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.MemberExpenseResponseDTO;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


// TODO батч операции
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;


    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;
    private final TravelMemberRepository travelMemberRepository;


    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository,
                          UserRepository userRepository, TravelRepository travelRepository, TravelMemberRepository travelMemberRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.travelRepository = travelRepository;
        this.travelMemberRepository = travelMemberRepository;
    }


    // TODO более одного плательщика
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO expenseRequestDTO, Long travelId, Long payerId) {

        // TODO должен вызываться сервис
        Category category = findCategory(expenseRequestDTO.getCategoryId());
        Travel travel = findTravel(travelId);
        User payer = this.findUserInTravel(payerId, travelId);

        Expense expense = Expense.create(
                expenseRequestDTO.getName(),
                expenseRequestDTO.getDescription(),
                expenseRequestDTO.getSum(),
                expenseRequestDTO.getDate(),
                category,
                payer,
                travel
        );

        distributeExpenseShares(expense,
                expenseRequestDTO.getParticipantShares(),
                payerId);

        expenseRepository.save(expense);

        return createExpenseResponseDTO(expense);
    }


    private void distributeExpenseShares(Expense expense,
                                         Map<Long, BigDecimal> participantShares,
                                         Long payerId) {

        BigDecimal sharePayer = expense.getSum();

        if (participantShares != null) {
            for (Map.Entry<Long, BigDecimal> entry : participantShares.entrySet()) {

                if (!entry.getKey().equals(payerId)) {

                    User participant = this.findUserInTravel(entry.getKey(), expense.getTravel().getId());
                    BigDecimal share = entry.getValue();

                    // TODO обработать
                    checkShare(share, expense.getSum(), entry.getKey());

                    MemberExpense memberExpense = MemberExpense.create(participant, entry.getValue().negate());
                    expense.addMemberExpense(memberExpense);
                    sharePayer = sharePayer.subtract(entry.getValue());
                }
            }

            // TODO обработать
            if (sharePayer.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PayerShareInvalidException(payerId);
            }
        }

        expense.addMemberExpense(MemberExpense.create(expense.getPayer(), sharePayer));
    }


    private void checkShare(BigDecimal share, BigDecimal expenseSum, Long userId) {
        if (share == null)
            throw new InvalidParticipantShareException(userId, "Доля участника не может быть пустой");
        if (share.signum() <= 0)
            throw new InvalidParticipantShareException(userId, "Доля участника должна быть положительной");
        if (share.compareTo(expenseSum) > 0)
            throw new InvalidParticipantShareException(userId, "Доля участника больше суммы расхода");
    }


    private ExpenseResponseDTO createExpenseResponseDTO(Expense expense) {

        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .sum(expense.getSum())
                .name(expense.getName())
                .description(expense.getDescription())
                .date(expense.getDate())
                .payer(expense.getPayer().getName() + " " + expense.getPayer().getSurname())
                .participants(createSetParticipantsResponseDTO(expense.getMemberExpenses()))
                .build();
    }


    private Set<MemberExpenseResponseDTO> createSetParticipantsResponseDTO(Set<MemberExpense> memberExpenses) {

        if (memberExpenses == null) {
            return Set.of();
        }

        return memberExpenses.stream()
                .map(this::createMemberExpenseResponseDTO)
                .collect(Collectors.toSet());
    }


    private MemberExpenseResponseDTO createMemberExpenseResponseDTO(MemberExpense memberExpense) {
        return MemberExpenseResponseDTO.builder()
                .surname(memberExpense.getParticipant().getSurname())
                .userId(memberExpense.getParticipant().getId())
                .share(memberExpense.getShare())
                .name(memberExpense.getParticipant().getName())
                .build();
    }


    public void deleteExpense(Long travelId, Long expenseId) {

        Expense expense = findExpenseInTravel(expenseId, travelId);
        expenseRepository.delete(expense);
    }


    // 💡 TODO Можно оптимизировать, чтобы не делать три запроса, а сразу искать в TravelMemberRepository с join fetch.
    private User findUserInTravel(Long userId, Long travelId) {

        // TODO нужны ли эти запросы?
        findUser(userId);
        findTravel(travelId);

        return travelMemberRepository.findByUserIdAndTravelId(userId, travelId)
                .orElseThrow(() ->
                        new UserNotInTravelException(userId, travelId))
                .getUser();
    }

    private Expense findExpenseInTravel(Long expenseId, Long travelId) {

        // TODO нужнен ли?
        findTravel(travelId);

        return expenseRepository.findByIdAndTravelId(expenseId, travelId)
                .orElseThrow(() -> new ExpenseNotInTravelException(expenseId, travelId));
    }

    private Travel findTravel(Long travelId) {
        return travelRepository.findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException(travelId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(categoryId));
    }
}
