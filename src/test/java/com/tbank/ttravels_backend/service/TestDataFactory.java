package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.category.CreateCategoryRequest;
import com.tbank.ttravels_backend.dto.category.EditCategoryRequest;
import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.transfer.CreateTransferRequest;
import com.tbank.ttravels_backend.dto.transfer.EditTransferRequest;
import com.tbank.ttravels_backend.dto.travel.CreateTravelRequest;
import com.tbank.ttravels_backend.dto.travel.EditTravelRequest;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import com.tbank.ttravels_backend.enums.TravelStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class TestDataFactory {

    private static final String TRAVEL_NAME_PREFIX = "Travel name";
    private static final String EXPENSE_NAME = "Expense name";
    private static final String CATEGORY_NAME = "Category name";
    private static final String USER_NAME_PREFIX = "Name";
    private static final String USER_SURNAME_PREFIX = "Surname";

    private static final String TRAVEL_DESC_PREFIX = "Travel Description";

    private static final String PHONE = "+78005553535";

    private static final OffsetDateTime START = OffsetDateTime.parse("2025-01-10T10:00:00+03:00");

    private static final OffsetDateTime END = OffsetDateTime.parse("2025-01-20T10:00:00+03:00");




    public static User user(long id) {
        return User.builder()
                .id(id)
                .name(USER_NAME_PREFIX + " " + id)
                .surname(USER_SURNAME_PREFIX + " " + id)
                .build();
    }

    public static User user(long userId, String phone) {
        return User.builder()
                .id(userId)
                .phone(phone)
                .build();
    }



    public static Travel fullTravel(long id) {

        return Travel.builder()
                .id(id)
                .name(TRAVEL_NAME_PREFIX + id)
                .description(TRAVEL_DESC_PREFIX + id)
                .startDate(START)
                .endDate(END)
                .owner(user(id))
                .status(TravelStatus.ACTIVE)
                .build();
    }

    public static Travel travel(long id, TravelStatus status) {

        return Travel.builder()
                .id(id)
                .name(TRAVEL_NAME_PREFIX + id)
                .description(TRAVEL_DESC_PREFIX + id)
                .startDate(START)
                .endDate(END)
                .owner(user(id))
                .status(status)
                .build();
    }

    public static Travel travel(long id) {
        return Travel.builder()
                .id(id)
                .name(TRAVEL_NAME_PREFIX + id)
                .build();
    }

    public static Travel travel(long id, User owner) {

        return Travel.builder()
                .id(id)
                .name(TRAVEL_NAME_PREFIX + id)
                .description(TRAVEL_DESC_PREFIX + id)
                .startDate(START)
                .endDate(END)
                .owner(owner)
                .status(TravelStatus.ACTIVE)
                .build();
    }



    public static TravelMember travelMember(long id, long userId, long travelId) {
        return TravelMember.builder().id(id).travel(travel(travelId)).user(user(userId)).build();
    }

    public static TravelMember travelMember(long id, long userId) {
        return TravelMember.builder().id(id).user(user(userId)).build();
    }

    public static TravelMember travelMemberInvite(long id, long userId) {
        return TravelMember.builder().id(id).user(user(userId)).status(MemberStatus.INVITED).build();
    }

    public static TravelMember travelMember(long id, long userId, MemberStatus status) {
        return TravelMember.builder().id(id).user(user(userId)).status(status).build();
    }

    public static TravelMember travelMember(long travelMemberId, long userId, long travelId,
                                            MemberRole role, MemberStatus status) {
        return TravelMember.builder()
                .id(travelMemberId)
                .travel(travel(travelId))
                .user(user(userId))
                .role(role)
                .status(status)
                .build();
    }



    public static Category category(long id) {
        return Category.builder()
                .id(id)
                .name(CATEGORY_NAME)
                .build();
    }

    public static Category category(long id, Travel travel) {
        return Category.builder()
                .id(id)
                .travel(travel)
                .name(CATEGORY_NAME)
                .build();
    }



    public static MemberExpense memberExpense(User user, double share) {
        return MemberExpense.builder()
                .participant(user)
                .share(BigDecimal.valueOf(share))
                .build();
    }

    public static MemberExpense memberExpense(long userId, double share) {

        User user = user(userId);

        return MemberExpense.builder()
                .id(userId)
                .participant(user)
                .share(BigDecimal.valueOf(share))
                .build();
    }

    public static MemberExpense memberExpense(long userId, double share, Expense expense) {

        User user = user(userId);

        return MemberExpense.builder()
                .id(userId)
                .participant(user)
                .share(BigDecimal.valueOf(share))
                .expense(expense)
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

    public static Expense expense(long id, Category category,
                                  BigDecimal sum, Travel travel, Set<MemberExpense> memberExpenses) {

        return Expense.builder()
                .id(id)
                .name(EXPENSE_NAME + id)
                .category(category)
                .sum(sum)
                .travel(travel)
                .memberExpenses(memberExpenses)
                .build();
    }



    public static Transfer transfer(User sender, User recipient, double amount) {
        return Transfer.builder()
                .sender(sender)
                .recipient(recipient)
                .sum(BigDecimal.valueOf(amount))
                .build();
    }

    public static Transfer transfer(long id, User sender, User recipient, double amount) {
        return Transfer.builder()
                .id(id)
                .sender(sender)
                .recipient(recipient)
                .sum(BigDecimal.valueOf(amount))
                .build();
    }

    public static Transfer transfer(long id, long senderId, long recipientId, long travelId, double amount) {
        return Transfer.builder()
                .id(id)
                .sender(user(senderId))
                .recipient(user(recipientId))
                .sum(BigDecimal.valueOf(amount))
                .travel(travel(travelId))
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



    public static CreateCategoryRequest createCategoryRequest(String name) {
        return new CreateCategoryRequest(name);
    }



    public static EditCategoryRequest editCategoryRequest(String name) {
        return new EditCategoryRequest(name);
    }



    public static CreateTransferRequest createTransferRequest(long senderId, long recipientId, BigDecimal sum) {

        return new CreateTransferRequest(senderId, recipientId, sum);
    }



    public static EditTransferRequest editTransferRequest(BigDecimal sum) {
        return new EditTransferRequest(sum);
    }



    public static CreateTravelRequest createTravelRequest(String name, String desc) {

        return new CreateTravelRequest(name, desc, START, END);
    }



    public static EditTravelRequest editTravelRequest(String name, String description,
                                                      OffsetDateTime startDate, OffsetDateTime endDate) {

        return new EditTravelRequest(name, description, startDate, endDate);
    }



    public static PasswordCredential passwordCredential(String passwordHash, User user) {
        return PasswordCredential.builder()
                .userId(user.getId())
                .passwordHash(passwordHash)
                .user(user)
                .build();
    }
}
