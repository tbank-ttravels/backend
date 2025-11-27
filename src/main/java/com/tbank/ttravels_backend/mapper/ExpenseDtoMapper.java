package com.tbank.ttravels_backend.mapper;

import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.MemberExpenseResponseDTO;
import com.tbank.ttravels_backend.entity.Expense;
import com.tbank.ttravels_backend.entity.MemberExpense;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ExpenseDtoMapper {

    public ExpenseResponseDTO createExpenseResponseDTO(Expense expense) {

        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .payerId(expense.getPayer().getId())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .sum(expense.getSum())
                .name(expense.getName())
                .description(expense.getDescription())
                .date(expense.getDate())
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
                .share(memberExpense.getShare().abs())
                .name(memberExpense.getParticipant().getName())
                .build();
    }

}
