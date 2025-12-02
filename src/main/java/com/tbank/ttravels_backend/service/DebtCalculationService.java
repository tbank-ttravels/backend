package com.tbank.ttravels_backend.service;


import com.tbank.ttravels_backend.dto.debt.DebtInfoDTO;
import com.tbank.ttravels_backend.dto.debt.TravelDebtsResponseDTO;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.mapper.UserDtoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DebtCalculationService {

    private final TravelMemberService travelMemberService;
    private final TransferService transferService;
    private final ExpenseService expenseService;
    private final UserDtoMapper userDtoMapper;


    @Transactional
    public TravelDebtsResponseDTO calculateDebtsForUser(Long userId, Long travelId) {

        var travelMembers = travelMemberService.findAllMembersInTravel(travelId).stream()
                .map(TravelMember::getUser).toList();

        Map<User, BigDecimal> balances = new HashMap<>();
        for (User travelMember : travelMembers) {
            balances.put(travelMember, BigDecimal.ZERO);
        }

        calculateDebtsFromExpenses(balances, travelId, userId);

        applyTransfers(balances, travelId, userId);

        applyExpensesWhereUserIsDebtor(balances, travelId, userId);

        balances.replaceAll((u, v) -> v.setScale(2, RoundingMode.HALF_UP));

        return createResponse(balances);
    }


    private TravelDebtsResponseDTO createResponse(Map<User, BigDecimal> balances) {


        List<DebtInfoDTO> debts = new ArrayList<>();        // кому пользователь должен
        List<DebtInfoDTO> creditors = new ArrayList<>();    // кто должен пользователю

        for (Map.Entry<User, BigDecimal> balance : balances.entrySet()) {

            var userDto = userDtoMapper.createUserDTO(balance.getKey());
            var amount = balance.getValue();

            if (amount.compareTo(BigDecimal.ZERO) > 0) {

                creditors.add(DebtInfoDTO.builder()
                        .user(userDto)
                        .totalAmount(amount)
                        .build());
            }
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                debts.add(DebtInfoDTO.builder()
                        .user(userDto)
                        .totalAmount(amount.abs())
                        .build());
            }
        }

        return TravelDebtsResponseDTO.builder()
                .creditors(creditors)
                .debts(debts)
                .build();
    }


    private void calculateDebtsFromExpenses(Map<User, BigDecimal> balances, Long travelId, Long userId) {

        var expenses =
                expenseService.findAllByTravelIdAndPayerIdOrderByDateDesc(travelId, userId);

        for (var expense : expenses) {

            for (var memberExpense : expense.getMemberExpenses()) {

                User participant = memberExpense.getParticipant();

                if (!participant.getId().equals(userId))
                    balances.merge(participant, memberExpense.getShare().abs(), BigDecimal::add);
            }
        }
    }


    private void applyTransfers(Map<User, BigDecimal> balances, Long travelId, Long userId) {

        // Все переводы, которые совершены пользователю
        var incoming = transferService.findAllByTravelIdAndRecipientId(travelId, userId);

        if (!incoming.isEmpty()) {

            for (Transfer transfer : incoming) {

                User sender = transfer.getSender();
                var sum = transfer.getSum();

                balances.merge(sender, sum, BigDecimal::subtract);
            }
        }

        // Все переводы, которые отправил пользователь
        var outgoing = transferService
                .findAllByTravelIdAndSenderId(travelId, userId);

        if (!outgoing.isEmpty()) {

            for (Transfer transfer : outgoing) {

                User recipient = transfer.getRecipient();
                var sum = transfer.getSum();

                balances.merge(recipient, sum, BigDecimal::add);
            }
        }
    }


    private void applyExpensesWhereUserIsDebtor(Map<User, BigDecimal> balances, Long travelId, Long userId) {

        var debtorExpenses =
                expenseService.findExpensesWhereUserIsDebtor(userId, travelId);

        for (Expense expense : debtorExpenses) {

            User payer = expense.getPayer();

            // Уже отрицательный
            var debt = expense.getMemberExpenses().stream()
                    .filter(me -> me.getParticipant().getId().equals(userId))
                    .map(MemberExpense::getShare)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            balances.merge(payer, debt, BigDecimal::add);
        }
    }
}
