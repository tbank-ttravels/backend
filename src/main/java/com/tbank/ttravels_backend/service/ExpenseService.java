package com.tbank.ttravels_backend.service;


import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;


// TODO батч операции
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ReferenceLookupService referenceLookupService;
    private final ExpenseDtoMapper expenseDtoMapper;


    /**
     * Конструктор сервиса ExpenseService.
     *
     * @param expenseRepository репозиторий для работы с сущностью Expense
     * @param validationService сервис для проверки участия пользователей, поездок и категорий
     * @param expenseDtoMapper  маппер для преобразования сущности Expense в DTO
     */
    public ExpenseService(ExpenseRepository expenseRepository,
                          ReferenceLookupService validationService,
                          ExpenseDtoMapper expenseDtoMapper) {
        this.expenseRepository = expenseRepository;
        this.referenceLookupService = validationService;
        this.expenseDtoMapper = expenseDtoMapper;
    }


    // TODO более одного плательщика
    // TODO дублирование в мапе участников

    /**
     * Создаёт новую трату в указанной поездке
     * <p>
     * Особенности:
     * - Любой участник поездки может создать расход, указав сумму, категорию, название, описание и дату (по умолчанию текущая дата).
     * - Плательщик (payer) указывается явно, берется из jwt (инициатор запроса).
     * - Доли участников (participantShares) распределяются автоматически, включая плательщика.
     * - Валидируются существование поездки, категории и участников.
     *
     * @param expenseRequestDTO DTO с данными для создания траты
     * @param travelId          идентификатор поездки, в которой создаётся трата
     * @param payerId           идентификатор пользователя, который оплачивает расход (должен быть участником поездки)
     * @return DTO с данными созданной траты
     * @throws UserNotFoundInTravelException    если плательщик не является участником поездки
     * @throws TravelNotFoundException          если поездка с travelId не найдена
     * @throws CategoryNotFoundException        если категория с указанным id не найдена
     * @throws InvalidParticipantShareException если доли участников некорректны
     * @throws PayerShareInvalidException       если доля плательщика получается меньше или равна нулю
     */
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO expenseRequestDTO, Long travelId, Long payerId) {

        // TODO должен вызываться сервис
        Category category = referenceLookupService.findCategory(expenseRequestDTO.getCategoryId());
        Travel travel = referenceLookupService.findTravel(travelId);
        User payer = referenceLookupService.findUserInTravel(payerId, travelId);

        checkPayerIncludeExpense(payerId, expenseRequestDTO.getParticipantShares());

        BigDecimal sum = calculateSum(expenseRequestDTO.getParticipantShares());

        Expense expense = Expense.create(
                expenseRequestDTO.getName(),
                expenseRequestDTO.getDescription(),
                sum,
                expenseRequestDTO.getDate(),
                category,
                payer,
                travel
        );

        distributeExpenseShares(expense,
                expenseRequestDTO.getParticipantShares(),
                payerId);

        expenseRepository.save(expense);

        return expenseDtoMapper.createExpenseResponseDTO(expense);
    }

    private void checkPayerIncludeExpense(Long payerId, Map<Long, BigDecimal> participantShares) {
        if (participantShares == null || !participantShares.containsKey(payerId)) {
            throw new PayerNotInParticipantsException(payerId);
        }
    }

    private BigDecimal calculateSum(Map<Long, BigDecimal> participantShares) {

        BigDecimal sum = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : participantShares.entrySet()) {
            sum = sum.add(checkShare(entry.getValue(), entry.getKey()));
        }

        return sum;
    }



     /**
     * Распределяет финансовые доли среди участников траты.
     * <p>
     * Особенности работы:
     * - Все доли в participantShares должны быть положительными
     * - Плательщик получает положительную долю, участники - отрицательную
     * - Сумма траты вычисляется как сумма всех долей из participantShares
     *
     * @param expense           сущность траты, для которой распределяются доли
     * @param participantShares мапа участников и их долей (userId:сумма), исключая плательщика
     * @param payerId           идентификатор плательщика, его доля рассчитывается автоматически
     */
    private void distributeExpenseShares(Expense expense,
                                         Map<Long, BigDecimal> participantShares,
                                         Long payerId) {

        if (participantShares != null) {
            for (Map.Entry<Long, BigDecimal> entry : participantShares.entrySet()) {

                User participant = referenceLookupService.findUserInTravel(entry.getKey(), expense.getTravel().getId());

                MemberExpense memberExpense;

                if (participant.getId().equals(payerId)) {
                    memberExpense = MemberExpense.create(participant, entry.getValue());
                } else {
                    memberExpense = MemberExpense.create(participant, entry.getValue().negate());
                }

                expense.addMemberExpense(memberExpense);
            }

        }
    }


    /**
     * Проверяет корректность доли участника траты.
     * <p>
     * Доля должна быть:
     * - не null
     * - положительной
     * - не больше суммы самой траты
     *
     * @param share  доля участника
     * @param userId идентификатор участника, для которого проверяется доля
     * @throws InvalidParticipantShareException если доля некорректна
     */
    private BigDecimal checkShare(BigDecimal share, Long userId) {

        if (share == null)
            throw new InvalidParticipantShareException(userId, "Доля участника не может быть пустой");
        if (share.signum() <= 0)
            throw new InvalidParticipantShareException(userId, "Доля участника должна быть положительной");

        return share;
    }


    /**
     * Удаляет указанную трату из поездки.
     * <p>
     * Проверяется, что пользователь является участником поездки.
     * Если трата не найдена в указанной поездке, выбрасывается исключение ExpenseNotFoundInTravelException.
     *
     * @param travelId  идентификатор поездки
     * @param expenseId идентификатор траты для удаления
     * @param userId    идентификатор пользователя, инициирующего удаление
     * @throws ExpenseNotFoundInTravelException если трата не найдена в поездке
     * @throws UserNotFoundInTravelException    если пользователь не участвует в поездке
     */
    public void deleteExpense(Long travelId, Long expenseId, Long userId) {

        referenceLookupService.checkUserInTravel(userId, travelId);

        Expense expense = this.findExpenseInTravel(expenseId, travelId);

        expenseRepository.delete(expense);
    }


    /**
     * Находит трату по идентификатору в рамках указанной поездки.
     *
     * @param expenseId идентификатор траты
     * @param travelId  идентификатор поездки
     * @return найденная сущность Expense
     * @throws ExpenseNotFoundInTravelException если трата не найдена в указанной поездке
     */
    public Expense findExpenseInTravel(Long expenseId, Long travelId) {

        // TODO нужнен ли?
        referenceLookupService.findTravel(travelId);

        return expenseRepository.findByIdAndTravelId(expenseId, travelId)
                .orElseThrow(() -> new ExpenseNotFoundInTravelException(expenseId, travelId));
    }

    // TODO полностью удалять старый список участников или редактировать сущетсвующий?
    //  Тогда удаление из траты и добавление делать отдельными эндпоинтами?

    /**
     * Обновляет существующую трату в поездке.
     * <p>
     * Особенности:
     * - Обновляются только поля, переданные в DTO.
     * - "Безопасные" поля (name, description, category, date) обновляются без пересчета долей участников.
     * - Финансовые поля (sum и participantShares) можно обновлять только вместе.
     * - При обновлении финансовых полей пересчитываются доли всех участников, включая плательщика.
     *
     * @param travelId                идентификатор поездки, в которой находится трата
     * @param expenseId               идентификатор траты для обновления
     * @param expenseUpdateRequestDTO DTO с новыми значениями полей
     * @param userId                  идентификатор пользователя, который делает изменение (должен быть участником поездки)
     * @return DTO с актуальными данными траты после обновления
     * @throws IllegalArgumentException         если DTO пустое или опасные поля переданы некорректно
     * @throws ExpenseNotFoundInTravelException если трата не найдена в указанной поездке
     * @throws UserNotFoundInTravelException    если пользователь не является участником поездки
     * @throws EmptyUpdateRequestException      если DTO пустое
     */
    @Transactional
    // Обновление траты
    public ExpenseResponseDTO updateExpense(Long travelId, Long expenseId,
                                            ExpenseUpdateRequestDTO expenseUpdateRequestDTO, Long userId) {

        // Если DTO пустое, обновлять нечего
        if (!this.hasAnyField(expenseUpdateRequestDTO)) {
            throw new EmptyUpdateRequestException();
        }

        // Только участник этой поездки может изменять трату
        referenceLookupService.checkUserInTravel(userId, travelId);

        boolean hasSum = expenseUpdateRequestDTO.sum() != null;
        boolean hasShares = expenseUpdateRequestDTO.participantShares() != null
                && !expenseUpdateRequestDTO.participantShares().isEmpty();


        // TODO сделать исключение
        // Обновлять поля sum и participantShares можно только, когда переданы оба
        if (hasSum != hasShares) {
            throw new IllegalArgumentException("Опасные поля должны передаваться вместе или не передаваться вовсе");
        }

        Expense expense = this.findExpenseInTravel(expenseId, travelId);

        updateBasicInfo(expense,
                expenseUpdateRequestDTO.name(), expenseUpdateRequestDTO.description(),
                expenseUpdateRequestDTO.categoryId(), expenseUpdateRequestDTO.date());

        // Проверка hasSum != hasShares показала, что hasSum == hasShares
        // поэтому првоеряем ток одну переменную
        if (hasSum) {
            updateFinancialFields(expense,
                    expenseUpdateRequestDTO.sum(),
                    expenseUpdateRequestDTO.participantShares());
        }

        return expenseDtoMapper.createExpenseResponseDTO(expense);
    }


    /**
     * Проверяет, что DTO обновления траты содержит хотя бы одно поле для изменения.
     *
     * @param dto объект ExpenseUpdateRequestDTO с возможными изменениями
     * @return true, если есть хотя бы одно поле для обновления, иначе false
     */
    private boolean hasAnyField(ExpenseUpdateRequestDTO dto) {
        return (dto.participantShares() != null && !dto.participantShares().isEmpty()) ||
                dto.date() != null ||
                dto.sum() != null ||
                dto.description() != null ||
                dto.categoryId() != null ||
                dto.name() != null;
    }


    /**
     * Частично обновляет "безопасные" поля траты, которые не влияют на
     * финансовое распределение между участниками.
     * <p>
     * Любое поле, переданное как null, будет проигнорировано.
     *
     * @param expense     сущность Expense для обновления
     * @param name        новое название траты (или null, если не изменять)
     * @param description новое описание траты (или null, если не изменять)
     * @param categoryId  новый id категории (или null, если не изменять)
     * @param date        новая дата траты (или null, если не изменять)
     */
    private void updateBasicInfo(Expense expense, String name, String description,
                                 Long categoryId, OffsetDateTime date) {

        if (name != null) {
            expense.setName(name);
        }

        if (description != null) {
            expense.setDescription(description);
        }

        if (categoryId != null) {
            expense.setCategory(referenceLookupService.findCategory(categoryId));
        }

        if (date != null) {
            expense.setDate(date);
        }
    }


    /**
     * Обновляет финансовые поля траты, включая сумму и доли участников.
     * <p>
     * Особенности:
     * - Нельзя менять плательщика (payer) — его доля сохраняется.
     * - Все существующие участники (MemberExpense) очищаются и пересоздаются
     * согласно новым долям из DTO.
     * - Выполняется проверка валидности долей участников.
     *
     * @param expense           сущность траты, которую нужно обновить
     * @param newSum            новая сумма
     * @param participantShares новые участники
     * @throws InvalidParticipantShareException если доля участника некорректна
     * @throws PayerShareInvalidException       если доля плательщика получается меньше или равна нулю
     */
    private void updateFinancialFields(Expense expense, BigDecimal newSum,
                                       Map<Long, BigDecimal> participantShares) {

        // TODO НЕЛЬЗЯ МЕНЯТЬ ТОГО КТО ПЛАТИЛ

        expense.setSum(newSum);

        expense.getMemberExpenses().clear();

        this.distributeExpenseShares(expense, participantShares, expense.getPayer().getId());
    }
}