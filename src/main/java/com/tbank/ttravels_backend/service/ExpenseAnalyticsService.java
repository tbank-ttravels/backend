package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.expense_analytics.CategoryAnalyticsResponseDTO;
import com.tbank.ttravels_backend.dto.expense_analytics.TravelExpenseAnalyticsDTO;
import com.tbank.ttravels_backend.entity.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.tbank.ttravels_backend.dto.expense_analytics.CategoryAnalyticsResponseDTO.ParticipantStats;

/**
 * Сервис формирования аналитики расходов по категориям внутри поездки.
 */

@Service
@RequiredArgsConstructor
public class ExpenseAnalyticsService {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final TravelService travelService;
    private final TravelMemberService travelMemberService;

    /**
     * Формирует аналитику расходов по категориям в рамках одной поездки.
     *
     * <p>
     * Собирает данные по категориям, тратам и пользователям, рассчитывает
     * общие суммы, доли категорий, количество трат и статистику по каждому участнику.
     *
     * @param travelId ID поездки
     * @return итоговый объект аналитики, содержащий список категорий с данными
     * и общую сумму расходов по поездке
     */
    @Transactional
    public TravelExpenseAnalyticsDTO generateExpenseReport(Long travelId) {

        this.travelService.checkTravel(travelId);


        // Сбор данных
        // 1. Все категории
        var categories = categoryService.findAllCategoryInTravel(travelId);

        // 2. мапа "Категория : список трат"
        Map<Long, List<Expense>> expensesByCategory = loadAndGroupExpensesByCategory(travelId);

        // 3. мапа "Категрия : сумма потраченная в ней"
        Map<Long, BigDecimal> totalAmountByCategory = calculateTotalAmountPerCategory(expensesByCategory);

        // 4. Общая сумма все трат по всем категориям
        var totalAmount = totalAmountByCategory.values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Кто, сколько в каждой категории потратил
        Map<Long, List<ParticipantStats>> participantStats = buildParticipantStatsByCategory(
                buildCategoryUserStats(expensesByCategory),
                this.travelMemberService.findAllMembersInTravel(travelId).stream()
                        .map(TravelMember::getUser)
                        .toList());

        // 6. мапа "Категория : количество трат"
        Map<Long, Integer> expenseCountByCategory = calculateExpenseCountByCategory(expensesByCategory);


        // Ответ
        List<CategoryAnalyticsResponseDTO> CategoriesAnalyticsResponseDTO = new ArrayList<>();


        // Формирование ответа
        for (Long categoryId : expensesByCategory.keySet()) {

            var totalAmountCategory = totalAmountByCategory.get(categoryId);

            var percentage = totalAmountCategory
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalAmount, 2, RoundingMode.HALF_UP);

            String categoryName;
            Optional<Category> category = categories.stream()
                    .filter(c -> Objects.equals(c.getId(), categoryId))
                    .findFirst();
            categoryName = category.map(Category::getName).orElse("Без категории");

            CategoriesAnalyticsResponseDTO.add(buildCategoryAnalyticsResponseDTO(
                    categoryId,
                    categoryName,
                    totalAmountCategory,
                    percentage,
                    expenseCountByCategory.get(categoryId),
                    participantStats.get(categoryId)));
        }

        return TravelExpenseAnalyticsDTO.builder()
                .categories(CategoriesAnalyticsResponseDTO)
                .totalAmount(totalAmount)
                .build();
    }


    /**
     * Загружает все траты в поездке и группирует их по категориям.
     *
     * <p>
     * Категория может быть null — такие траты попадают в группу с ключом {@code null}.
     *
     * @param travelId ID поездки
     * @return мапа вида "ID категории : список трат"
     */
    private Map<Long, List<Expense>> loadAndGroupExpensesByCategory(Long travelId) {

        var expenses = expenseService.findAllExpensesInTravel(travelId);
        var expensesByCategory = new HashMap<Long, List<Expense>>();

        for (var expense : expenses) {

            Category category = expense.getCategory();
            Long categoryId = category == null ? null : category.getId();

            expensesByCategory.computeIfAbsent(categoryId, k -> new ArrayList<>())
                    .add(expense);
        }

        return expensesByCategory;
    }


    /**
     * Подсчитывает количество трат в каждой категории.
     *
     * @param expensesByCategory мапа "категория → список трат"
     * @return мапа "категория : количество трат"
     */
    private Map<Long, Integer> calculateExpenseCountByCategory(Map<Long, List<Expense>> expensesByCategory) {

        return expensesByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,                    // categoryId
                        entry -> entry.getValue().size()      // количество трат в категории
                ));
    }


    /**
     * Рассчитывает суммарную сумму расходов по каждой категории.
     *
     * @param expensesByCategory мапа "категория : список трат"
     * @return мапа "категория : общая сумма расходов"
     */
    private Map<Long, BigDecimal> calculateTotalAmountPerCategory(Map<Long, List<Expense>> expensesByCategory) {

        Map<Long, BigDecimal> totalAmountByCategory = new HashMap<>();

        for (Map.Entry<Long, List<Expense>> entry : expensesByCategory.entrySet()) {

            var sum = BigDecimal.ZERO;

            for (Expense expense : entry.getValue()) {
                sum = sum.add(expense.getSum());
            }

            totalAmountByCategory.put(entry.getKey(), sum);
        }

        return totalAmountByCategory;
    }


    /**
     * Формирует статистику по каждому участнику внутри каждой категории.
     *
     * <p>
     * Для каждой категории вычисляется, сколько каждый пользователь потратил.
     * Участник включается в статистику только если его сумма больше нуля.
     *
     * @param categoryUserStats мапа "категория : (пользователь : сумма)"
     * @param membersTravel     список всех участников поездки
     * @return мапа "категория : список статистики участников"
     */
    private Map<Long, List<ParticipantStats>> buildParticipantStatsByCategory(
            Map<Long, Map<Long, BigDecimal>> categoryUserStats,
            List<User> membersTravel) {

        // мапа "категория : список участников со статистикой" - возвращаемое значение
        Map<Long, List<ParticipantStats>> participantStatsByCategory = new HashMap<>();

        // Проход по каждой категории
        for (Map.Entry<Long, Map<Long, BigDecimal>> categoryEntry : categoryUserStats.entrySet()) {

            var categoryId = categoryEntry.getKey();

            // мапа "id участника : потраченная сумма"
            Map<Long, BigDecimal> amountsByUser = categoryEntry.getValue();

            var statsList = new ArrayList<ParticipantStats>();

            // Проход по всем участникам поездки
            for (User member : membersTravel) {

                // Берём сумму участника по его userId, если нет, возвращаем 0
                var amount = amountsByUser.getOrDefault(member.getId(), BigDecimal.ZERO);

                // Добавляем только тех, кто реально участвовал (сумма > 0)
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    statsList.add(
                            ParticipantStats.builder()
                                    .name(member.getName())
                                    .surname(member.getSurname())
                                    .expenseAmount(amount)
                                    .build()
                    );
                }
            }

            // Добавление списка участников по категории
            participantStatsByCategory.put(categoryId, statsList);
        }

        return participantStatsByCategory;
    }


    /**
     * Вычисляет суммарные траты каждого пользователя в каждой категории.
     *
     * <p>
     * Обходит все траты категории, суммируя доли участника из таблицы members_expenses.
     *
     * @param expensesByCategory мапа "категория : список трат"
     * @return мапа "категория : (пользователь : сумма его трат)"
     */
    private Map<Long, Map<Long, BigDecimal>> buildCategoryUserStats(
            Map<Long, List<Expense>> expensesByCategory) {

        Map<Long, Map<Long, BigDecimal>> categoryUserStats = new HashMap<>();

        for (Map.Entry<Long, List<Expense>> entry : expensesByCategory.entrySet()) {

            var categoryId = entry.getKey();
            Map<Long, BigDecimal> userAmounts = new HashMap<>();

            // Проход по всем тратам категории
            for (var expense : entry.getValue()) {

                // И каждого участника траты
                for (MemberExpense me : expense.getMemberExpenses()) {

                    var userId = me.getParticipant().getId();
                    BigDecimal current = userAmounts.getOrDefault(userId, BigDecimal.ZERO);

                    // Слажение доли (abs, чтобы убрать минус)
                    userAmounts.put(userId, current.add(me.getShare().abs()));
                }
            }

            categoryUserStats.put(categoryId, userAmounts);
        }

        return categoryUserStats;
    }


    /**
     * Создаёт DTO аналитики одной категории.
     *
     * @param categoryId          ID категории (может быть null)
     * @param categoryName        название категории или "Без категории"
     * @param totalAmountCategory общая сумма расходов в категории
     * @param percentage          доля категории от общей суммы расходов
     * @param expenseCount        количество трат в категории
     * @param participantStats    список статистики участников
     * @return DTO аналитики категории
     */
    private CategoryAnalyticsResponseDTO buildCategoryAnalyticsResponseDTO(Long categoryId,
                                                                           String categoryName,
                                                                           BigDecimal totalAmountCategory,
                                                                           BigDecimal percentage,
                                                                           Integer expenseCount,
                                                                           List<ParticipantStats> participantStats) {
        return CategoryAnalyticsResponseDTO.builder()
                .id(categoryId)
                .name(categoryName)
                .totalAmount(totalAmountCategory)
                .percentageOfTotal(percentage)
                .expenseCount(expenseCount)
                .participants(participantStats)
                .build();
    }
}