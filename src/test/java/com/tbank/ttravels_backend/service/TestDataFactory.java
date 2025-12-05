package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.entity.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class TestDataFactory {

    private static final String TRAVEL_NAME = "travel name";
    private static final String EXPENSE_NAME = "expense name";
    private static final String CATEGORY_NAME = "category name";
    private static final String USER_NAME_PREFIX = "User";
    private static final String USER_SURNAME_PREFIX = "Surname";


    public static User user(long id) {
        return User.builder()
                .id(id)
                .name(USER_NAME_PREFIX + " " + id)
                .surname(USER_SURNAME_PREFIX + " " + id)
                .build();
    }


    public static Travel travel(Long id) {
        return Travel.builder()
                .id(id)
                .name(TRAVEL_NAME)
                .build();
    }

    public static Category category(Long id) {
        return Category.builder()
                .id(id)
                .name(CATEGORY_NAME)
                .build();
    }

    public static MemberExpense memberExpense(User user, double share) {
        return MemberExpense.builder()
                .participant(user)
                .share(BigDecimal.valueOf(share))
                .build();
    }


    public static Expense fullExpense(long expenseId) {

        String ex1Name = "Expense " + expenseId;

        BigDecimal ex1PayerShare = BigDecimal.valueOf(100L * expenseId),
                ex1ParticipantShare = BigDecimal.valueOf(200L * expenseId).negate();

        BigDecimal ex1Sum = (ex1PayerShare.add(ex1ParticipantShare)).abs();

        User ex1Payer = User.builder().id(10 * expenseId + 1).build(),
                ex1Participant = User.builder().id(10 * expenseId + 2).build();

        MemberExpense ex1PayerME = MemberExpense.builder()
                .participant(ex1Payer)
                .share(ex1PayerShare)
                .build(),
                ex1ParticipantME = MemberExpense.builder()
                        .participant(ex1Participant)
                        .share(ex1ParticipantShare)
                        .build();

        Category ex1Category = Category.builder()
                .id(expenseId)
                .name("Category " + expenseId)
                .build();

        OffsetDateTime dateTime =
                OffsetDateTime.of(2024, 1, 1,
                                0, 0, 0, 0, ZoneOffset.UTC)
                        .plusDays(expenseId + 50);

        return Expense.builder()
                .id(expenseId)
                .name(ex1Name)
                .sum(ex1Sum)
                .payer(ex1Payer)
                .date(dateTime)
                .memberExpenses(Set.of(ex1PayerME, ex1ParticipantME))
                .category(ex1Category)
                .build();
    }

    public static Expense expense(long expenseId) {
        return Expense.builder().id(expenseId).build();
    }


    public static Expense expense(User payer, MemberExpense... memberExpenses) {
        return Expense.builder()
                .payer(payer)
                .memberExpenses(Set.of(memberExpenses))
                .build();
    }


    public static Expense expense(long expenseId, User payer,
                                  Set<MemberExpense> memberExpenses, Category category) {

        return Expense.builder()
                .id(expenseId)
                .payer(payer)
                .memberExpenses(memberExpenses)
                .category(category)
                .build();
    }

    public static Expense expense(long expenseId, Long... participantsId) {

        return Expense.builder()
                .id(expenseId)
                .memberExpenses(
                        Arrays.stream(participantsId)
                                .map(id -> MemberExpense.builder()
                                        .participant(User.builder()
                                                .id(id)
                                                .build())
                                        .build()
                                )
                                .collect(Collectors.toSet())
                )
                .build();
    }

    public static Transfer transfer(User sender, User recipient, double amount) {
        return Transfer.builder()
                .sender(sender)
                .recipient(recipient)
                .sum(BigDecimal.valueOf(amount))
                .build();
    }


    public static List<TravelMember> listTravelMember(User... users) {

        return Arrays.stream(users)
                .filter(Objects::nonNull)
                .map(u -> TravelMember.builder().user(u).build())
                .toList();
    }

    public static ExpenseUpdateRequestDTO expenseUpdateRequestDTO(long me2Id, OffsetDateTime newDate,
                                                                  String newExpenseName, String newExpenseDesc,
                                                                  long categoryId,
                                                                  Map<Long, BigDecimal> participantShares) {
        return ExpenseUpdateRequestDTO.builder()
                .payerId(me2Id)
                .date(newDate)
                .name(newExpenseName)
                .description(newExpenseDesc)
                .categoryId(categoryId)
                .participantShares(participantShares)
                .build();
    }

    public static ExpenseRequestDTO expenseRequestDTO(long payerId,
                                                      Map<Long, BigDecimal> participantShares,
                                                      long categoryId) {

        return ExpenseRequestDTO.builder()
                .name(EXPENSE_NAME)
                .payerId(payerId)
                .date(OffsetDateTime.now())
                .participantShares(participantShares)
                .categoryId(categoryId)
                .build();
    }

    public static ExpenseRequestDTO expenseRequestDTO(long payerId, Map<Long, BigDecimal> participantShares) {
        return ExpenseRequestDTO.builder()
                .name(EXPENSE_NAME)
                .payerId(payerId)
                .date(OffsetDateTime.now())
                .participantShares(participantShares)
                .build();
    }


//    public static List<Expense> expenses(long nExpenses) {
//
//        List<Expense> expenses = new ArrayList<>();
//
//        for (long i = 1; i <= nExpenses; i++) {
//
//            String ex1Name = "Expense " + i;
//
//            BigDecimal ex1PayerShare = BigDecimal.valueOf(100L * i),
//                    ex1ParticipantShare = BigDecimal.valueOf(200L * i).negate();
//
//            BigDecimal ex1Sum = (ex1PayerShare.add(ex1ParticipantShare)).abs();
//
//            User ex1Payer = User.builder().id(10 * i + 1).build(),
//                    ex1Participant = User.builder().id(10 * i + 2).build();
//
//            MemberExpense ex1PayerME = MemberExpense.builder()
//                    .participant(ex1Payer)
//                    .share(ex1PayerShare)
//                    .build(),
//                    ex1ParticipantME = MemberExpense.builder()
//                            .participant(ex1Participant)
//                            .share(ex1ParticipantShare)
//                            .build();
//
//            Category ex1Category = Category.builder().id(i).build();
//
//            expenses.add(Expense.builder()
//                    .id(i)
//                    .name(ex1Name)
//                    .sum(ex1Sum)
//                    .payer(ex1Payer)
//                    .memberExpenses(Set.of(ex1PayerME, ex1ParticipantME))
//                    .category(ex1Category)
//                    .build());
//        }
//
//        return expenses;
//    }
}
