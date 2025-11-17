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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


// TODO батч операции
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ReferenceLookupService referenceLookupService;
    private final ExpenseDtoMapper expenseDtoMapper;


    /**
     * Конструктор сервиса ExpenseService.
     *
     * @param expenseRepository      репозиторий для работы с сущностью Expense
     * @param referenceLookupService сервис для проверки участия пользователей, поездок и категорий
     * @param expenseDtoMapper       маппер для преобразования сущности Expense в DTO
     */
    public ExpenseService(ExpenseRepository expenseRepository,
                          ReferenceLookupService referenceLookupService,
                          ExpenseDtoMapper expenseDtoMapper) {
        this.expenseRepository = expenseRepository;
        this.referenceLookupService = referenceLookupService;
        this.expenseDtoMapper = expenseDtoMapper;
    }


    // TODO более одного плательщика
    // TODO дублирование в мапе участников

    /**
     * Создаёт новую трату в указанной поездке.
     * <p>
     * Особенности:
     * - Любой участник поездки может создать расход, указав категорию, название, описание, дату (по умолчанию текущая дата)
     * и финансовые доли участников.
     * - Плательщик (payer) указывается явно через поле payerId в DTO.
     * - Доли участников (participantShares) распределяются автоматически:
     * - Плательщик получает положительную долю.
     * - Остальные участники получают отрицательные доли.
     * - Сумма траты вычисляется как сумма всех долей (без передачи sum клиентом).
     * - Валидируются:
     * - Существование поездки.
     * - Существование категории.
     * - Все участники траты действительно входят в поездку.
     * - Плательщик присутствует в списке участников.
     * - Все доли участников положительные.
     *
     * @param expenseRequestDTO DTO с данными для создания траты. Должно содержать participantShares и payerId.
     * @param travelId          идентификатор поездки, в которой создаётся трата.
     * @return DTO с данными созданной траты.
     * @throws InvalidParticipantShareException если participantShares пусты или доли некорректны.
     * @throws PayerNotInParticipantsException  если payerId отсутствует среди участников.
     * @throws UserNotFoundInTravelException    если плательщик не является участником поездки.
     * @throws TravelNotFoundException          если поездка с travelId не найдена.
     * @throws CategoryNotFoundException        если категория с указанным id не найдена.
     */
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO expenseRequestDTO, Long travelId) {

        validateExpenseRequestDTO(expenseRequestDTO, travelId);

        // TODO должен вызываться сервис
        Category category = referenceLookupService.findCategory(expenseRequestDTO.getCategoryId());
        Travel travel = referenceLookupService.findTravel(travelId);

        User payer = referenceLookupService.findUserInTravel(expenseRequestDTO.getPayerId(), travelId);

        BigDecimal sum = calculateSum(expenseRequestDTO.getParticipantShares().values());

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
                payer.getId());

        expenseRepository.save(expense);

        return expenseDtoMapper.createExpenseResponseDTO(expense);
    }


    /**
     * Валидирует DTO перед созданием траты.
     * <p>
     * Проверяет:
     * - participantShares не пусты.
     * - Все пользователи присутствуют в поездке.
     * - Плательщик указан среди участников.
     */
    private void validateExpenseRequestDTO(ExpenseRequestDTO expenseRequestDTO, Long travelId) {

        if (expenseRequestDTO.getParticipantShares() == null || expenseRequestDTO.getParticipantShares().isEmpty()) {
            throw new InvalidParticipantShareException();
        }

        // Все ли являются участниками поездки?
        referenceLookupService.validateAllUsersInTravel(travelId, expenseRequestDTO.getParticipantShares().keySet());

        // Есть ли в участниках платящий?
        validatePayerInParticipants(expenseRequestDTO.getPayerId(), expenseRequestDTO.getParticipantShares().keySet());


        // Валидация долей
        validateShares(expenseRequestDTO.getParticipantShares());
    }


    /**
     * Валидирует доли всех участников траты.
     * <p>
     * Проверяет для каждой доли:
     * - не является null
     * - является положительным числом (> 0)
     *
     * @param participantShares мапа участников и их долей
     * @throws InvalidParticipantShareException если какая-либо доля некорректна
     */
    private void validateShares(Map<Long, BigDecimal> participantShares) {

        for (Map.Entry<Long, BigDecimal> entry : participantShares.entrySet()) {
            if (entry.getValue() == null)
                throw new InvalidParticipantShareException(entry.getKey(), "Доля участника не может быть пустой");
            if (entry.getValue().signum() <= 0)
                throw new InvalidParticipantShareException(entry.getKey(), "Доля участника должна быть положительной");
        }
    }


    /**
     * Проверяет, что плательщик указан среди участников траты.
     */
    private void validatePayerInParticipants(Long payerId, Set<Long> participantUserIds) {

        if (participantUserIds != null && !participantUserIds.contains(payerId)) {
            throw new PayerNotInParticipantsException(payerId);
        }
    }


    /**
     * Вычисляет сумму траты как сумму всех долей участников.
     *
     * @return сумма траты
     */
    private BigDecimal calculateSum(Collection<BigDecimal> shares) {

        return shares.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Распределяет финансовые доли среди участников траты.
     * <p>
     * Особенности работы:
     * - Плательщик получает положительную долю, участники - отрицательную
     * - Сумма траты вычисляется как сумма всех долей из participantShares
     *
     * @param expense           сущность траты, для которой распределяются доли
     * @param participantShares мапа участников и их долей (userId:сумма), исключая плательщика
     * @param payerId           идентификатор плательщика
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
     * Удаляет указанную трату из поездки.
     * <p>
     * Проверяется, что пользователь является участником поездки.
     * Если трата не найдена в указанной поездке, выбрасывается исключение ExpenseNotFoundInTravelException.
     *
     * @param travelId  идентификатор поездки
     * @param expenseId идентификатор траты для удаления
     * @throws ExpenseNotFoundInTravelException если трата не найдена в поездке
     * @throws UserNotFoundInTravelException    если пользователь не участвует в поездке
     */
    public void deleteExpense(Long travelId, Long expenseId) {

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


    /**
     * Обновляет существующую трату в указанной поездке.
     * <p>
     * Особенности работы:
     * - Можно обновлять базовую информацию: название, описание, категорию, дату
     * - Можно обновлять финансовые доли участников (participantShares)
     * - Можно изменить плательщика (payerId). В этом случае пересчитываются знаки долей:
     * плательщик — положительная, остальные участники — отрицательные
     * - Если участники переданы, проверяется, что все они уже существуют в тратах.
     * Новых участников добавить нельзя — для этого есть отдельный endpoint
     * - Если DTO пустое (нет полей для обновления), выбрасывается EmptyUpdateRequestException
     * - Сумма траты пересчитывается как сумма модулей всех долей участников
     *
     * @param travelId                идентификатор поездки
     * @param expenseId               идентификатор траты
     * @param expenseUpdateRequestDTO DTO с данными для обновления
     * @return DTO с обновлёнными данными траты
     * @throws EmptyUpdateRequestException      если DTO пустое
     * @throws UserNotFoundExpenseException     если передан participantId, которого нет в тратах
     * @throws InvalidParticipantShareException если доли некорректны
     * @throws TravelNotFoundException          если поездка не найдена
     * @throws ExpenseNotFoundInTravelException если трата не найдена
     * @throws UserNotFoundInTravelException    если новый плательщик не участник поездки
     */
    @Transactional
    public ExpenseResponseDTO updateExpense(Long travelId, Long expenseId,
                                            ExpenseUpdateRequestDTO expenseUpdateRequestDTO) {

        // Если DTO пустое, обновлять нечего
        if (!this.hasAnyField(expenseUpdateRequestDTO)) {
            // TODO корректнее обработать
            throw new EmptyUpdateRequestException();
        }

        // Переданы ли участники
        boolean hasShares = expenseUpdateRequestDTO.participantShares() != null
                && !expenseUpdateRequestDTO.participantShares().isEmpty();

        Expense expense = this.findExpenseInTravel(expenseId, travelId);

        // Редактируем участников, если они переданы
        if (hasShares) {

            validateParticipantSharesForUpdate(
                    expenseUpdateRequestDTO.participantShares(),
                    expense);

            updateFinancialFields(
                    expense,
                    expenseUpdateRequestDTO.participantShares());
        }

        updateBasicInfo(expense,
                expenseUpdateRequestDTO.name(), expenseUpdateRequestDTO.description(),
                expenseUpdateRequestDTO.categoryId(), expenseUpdateRequestDTO.date());

        // Меняем платящего, если есть
        if (expenseUpdateRequestDTO.payerId() != null &&
                !expenseUpdateRequestDTO.payerId().equals(expense.getPayer().getId())) {
            updatePayer(
                    expense,
                    referenceLookupService.findUserInTravel(
                            expenseUpdateRequestDTO.payerId(),
                            travelId));
        }

        expenseRepository.save(expense);

        return expenseDtoMapper.createExpenseResponseDTO(expense);
    }


    /**
     * Обновляет плательщика траты.
     * <p>
     * Особенности:
     * - Меняет поле payer в сущности Expense
     * - Меняет знаки долей участников: новый плательщик получает положительную долю,
     * остальные участники — отрицательные
     *
     * @param expense сущность траты
     * @param payer   новый плательщик
     */
    private void updatePayer(Expense expense, User payer) {

        expense.setPayer(payer);

        Set<MemberExpense> membersExpense = expense.getMemberExpenses();

        membersExpense.forEach((me) ->
        {
            Long participantId = me.getParticipant().getId();
            BigDecimal share = me.getShare().abs();

            if (!participantId.equals(expense.getPayer().getId())) {
                me.setShare(share.negate());
            } else {
                me.setShare(share.abs());
            }
        });
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
                dto.payerId() != null ||
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
     * Обновляет финансовые доли существующих участников траты.
     * <p>
     * Особенности:
     * - Только существующие участники могут быть обновлены
     * - Доля плательщика всегда положительная, доли остальных участников отрицательные
     * - После обновления пересчитывается сумма траты как сумма модулей всех долей
     *
     * @param expense           сущность траты
     * @param participantShares новые значения долей для существующих участников
     */
    private void updateFinancialFields(Expense expense,
                                       Map<Long, BigDecimal> participantShares) {

        Set<MemberExpense> membersExpense = expense.getMemberExpenses();


        membersExpense.forEach(me -> {
            Long participantId = me.getParticipant().getId();
            if (participantShares.containsKey(participantId)) {
                BigDecimal newShare = participantShares.get(participantId);
                me.setShare(!participantId.equals(expense.getPayer().getId())
                        ? newShare.negate()
                        : newShare);
            }
        });

        expense.setSum(calculateSum(expense.getMemberExpenses().stream()
                .map(MemberExpense::getShare)
                .map(BigDecimal::abs)
                .toList()));
    }


    /**
     * Валидирует переданные доли участников при обновлении траты.
     * <p>
     * Правила:
     * - Если participantShares не переданы или пусты — проверка пропускается (ничего не обновляем).
     * - Каждый переданный пользователь должен уже существовать в текущей трате.
     * - Каждая доля должна быть валидна (не null, > 0).
     *
     * @param participantShares новые доли участников, переданные для обновления
     * @param expense           текущая сущность траты
     * @throws UserNotFoundExpenseException     если пытаются обновить пользователя, не входящего в трату
     * @throws InvalidParticipantShareException если переданные доли некорректны
     */
    private void validateParticipantSharesForUpdate(Map<Long, BigDecimal> participantShares, Expense expense) {

        if (participantShares != null && !participantShares.isEmpty()) {

            participantShares.keySet().forEach(userId -> checkUserInExpense(expense, userId));
            validateShares(participantShares);
        }
    }

    /**
     * Проверяет, что пользователь с указанным userId участвует в данной трате.
     * <p>
     * Метод предварительно кэширует IDs всех участников траты для оптимизации проверки.
     *
     * @param expense текущая трата
     * @param userId  идентификатор пользователя, которого пытаются обновить
     * @throws UserNotFoundExpenseException если пользователь отсутствует среди участников траты
     */
    private void checkUserInExpense(Expense expense, Long userId) {

        // Кэширую ID
        Set<Long> membersExpense = expense.getMemberExpenses().stream()
                .map(me -> me.getParticipant().getId())
                .collect(Collectors.toSet());

        if (!membersExpense.contains(userId)) {
            throw new UserNotFoundExpenseException(userId, expense.getName());
        }
    }
}