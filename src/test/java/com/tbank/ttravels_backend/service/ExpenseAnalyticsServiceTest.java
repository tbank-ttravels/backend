package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.expense_analytics.CategoryAnalyticsResponseDTO;
import com.tbank.ttravels_backend.dto.expense_analytics.TravelExpenseAnalyticsDTO;
import com.tbank.ttravels_backend.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseAnalyticsServiceTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private TravelService travelService;

    @Mock
    private TravelMemberService travelMemberService;

    @InjectMocks
    private ExpenseAnalyticsService expenseAnalyticsService;

    @Test
    void generateExpenseReport() {

        // === Given ===
        long travelId = 1L;
        Travel travel = TestDataFactory.travel(travelId);

        User user1 = TestDataFactory.user(1);
        User user2 = TestDataFactory.user(2);

        List<TravelMember> travelMembers =
                TestDataFactory.listTravelMember(user1, user2);

        List<Category> categories = List.of(
                TestDataFactory.category(1, travel),
                TestDataFactory.category(2, travel)
        );

        Set<MemberExpense> memberExpenses1 =
                Set.of(TestDataFactory.memberExpense(user1, 500));
        Set<MemberExpense> memberExpenses2 =
                Set.of(TestDataFactory.memberExpense(user1, 400),
                        TestDataFactory.memberExpense(user2, 150));
        Set<MemberExpense> memberExpenses3 =
                Set.of(TestDataFactory.memberExpense(user2, 800));
        Set<MemberExpense> memberExpenses4 =
                Set.of(TestDataFactory.memberExpense(user1, 200),
                        TestDataFactory.memberExpense(user2, 900));

        List<Expense> expenses = List.of(
                TestDataFactory.expense(1, categories.get(0),
                        calculateSum(memberExpenses1), travel, memberExpenses1),
                TestDataFactory.expense(2, categories.get(1),
                        calculateSum(memberExpenses2), travel, memberExpenses2),
                TestDataFactory.expense(3, categories.get(0),
                        calculateSum(memberExpenses3), travel, memberExpenses3),
                TestDataFactory.expense(4, null,
                        calculateSum(memberExpenses4), travel, memberExpenses4)
        );


        // === Mocking ===
        doReturn(categories).when(categoryService).findAllCategoryInTravel(travelId);
        doReturn(expenses).when(expenseService).findAllExpensesInTravel(travelId);
        doReturn(travelMembers).when(travelMemberService).findAllMembersInTravel(travelId);
        doNothing().when(travelService).checkTravel(travelId);


        // === When ===
        TravelExpenseAnalyticsDTO actual =
                expenseAnalyticsService.generateExpenseReport(travelId);


        // === Then ===
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getTotalAmount())
                        .isEqualByComparingTo(totalExpenses(expenses)),
                () -> assertThat(actual.getCategories()).hasSize(3),
                () -> assertCategories(actual, expenses)
        );


        // === VERIFY ===
        verify(travelService).checkTravel(travelId);
        verify(categoryService).findAllCategoryInTravel(travelId);
        verify(expenseService).findAllExpensesInTravel(travelId);
        verify(travelMemberService).findAllMembersInTravel(travelId);
    }


    private void assertCategories(
            TravelExpenseAnalyticsDTO actual,
            List<Expense> expenses
    ) {

        BigDecimal totalAmount = actual.getTotalAmount();

        List<CategoryAnalyticsResponseDTO> categories = actual.getCategories();

        CategoryAnalyticsResponseDTO noCategory = findNoCategory(categories);
        assertThat(noCategory.getName()).isEqualTo("Без категории");

        categories.stream()
                .filter(c -> c.getId() != null)
                .forEach(c ->
                        assertCategory(c, expenses, totalAmount)
                );

        assertCategory(noCategory, expenses, totalAmount);
    }

    private void assertCategory(
            CategoryAnalyticsResponseDTO category,
            List<Expense> expenses,
            BigDecimal totalAmount
    ) {

        List<Expense> categoryExpenses = expenses.stream()
                .filter(e -> Objects.equals(
                        e.getCategory() == null ? null : e.getCategory().getId(),
                        category.getId()
                ))
                .toList();

        BigDecimal expectedCategoryTotal =
                totalExpenses(categoryExpenses);

        assertAll(
                () -> assertThat(category.getTotalAmount())
                        .isEqualByComparingTo(expectedCategoryTotal),
                () -> assertThat(category.getExpenseCount())
                        .isEqualTo(categoryExpenses.size()),
                () -> assertThat(category.getPercentageOfTotal())
                        .isEqualByComparingTo(
                                calculatePercentage(expectedCategoryTotal, totalAmount)
                        )
        );

        assertParticipants(category, categoryExpenses);
    }

    private void assertParticipants(
            CategoryAnalyticsResponseDTO category,
            List<Expense> categoryExpenses
    ) {

        Map<String, BigDecimal> expected = categoryExpenses.stream()
                .flatMap(e -> e.getMemberExpenses().stream())
                .collect(Collectors.groupingBy(
                        me -> me.getParticipant().getName()
                                + " "
                                + me.getParticipant().getSurname(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                MemberExpense::getShare,
                                BigDecimal::add
                        )
                ));

        assertThat(category.getParticipants())
                .hasSize(expected.size());

        category.getParticipants().forEach(p -> {
            String key = p.getName() + " " + p.getSurname();
            assertThat(expected).containsKey(key);
            assertThat(p.getExpenseAmount())
                    .isEqualByComparingTo(expected.get(key));
        });
    }

    private CategoryAnalyticsResponseDTO findNoCategory(
            List<CategoryAnalyticsResponseDTO> categories
    ) {
        return categories.stream()
                .filter(c -> c.getId() == null)
                .findFirst()
                .orElseThrow();
    }


    private BigDecimal calculateSum(Set<MemberExpense> memberExpenses) {
        return memberExpenses.stream()
                .map(MemberExpense::getShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePercentage(
            BigDecimal part,
            BigDecimal total
    ) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }
}
