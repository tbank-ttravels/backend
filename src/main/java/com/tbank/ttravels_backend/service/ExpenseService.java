package com.tbank.ttravels_backend.service;


import com.tbank.ttravels_backend.dto.expense_update.ExpenseUpdateRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseRequestDTO;
import com.tbank.ttravels_backend.dto.exspense.ExpenseResponseDTO;
import com.tbank.ttravels_backend.dto.exspense.TravelExpensesResponseDTO;
import com.tbank.ttravels_backend.entity.*;
import com.tbank.ttravels_backend.exception.*;
import com.tbank.ttravels_backend.factory.ExpenseFactory;
import com.tbank.ttravels_backend.factory.MemberExpenseFactory;
import com.tbank.ttravels_backend.mapper.ExpenseDtoMapper;
import com.tbank.ttravels_backend.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


// TODO батч операции
@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    private final MemberExpenseService memberExpenseService;
    private final TravelMemberService travelMemberService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ExpenseDtoMapper expenseDtoMapper;
    private final TravelService travelService;


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
     * @throws DuplicateExpenseException        если трата уже добавлена, или участник траты уже в трате
     */
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO expenseRequestDTO, Long travelId) {

        validateExpenseRequestDTO(expenseRequestDTO, travelId);

        Category category = this.categoryService.findCategory(expenseRequestDTO.getCategoryId());
        Travel travel = travelService.findTravel(travelId);

        User payer = travelMemberService.findUserInTravel(expenseRequestDTO.getPayerId(), travelId);

        BigDecimal sum = calculateSum(expenseRequestDTO.getParticipantShares().values());

        Expense expense = ExpenseFactory.create(expenseRequestDTO.getName(),
                expenseRequestDTO.getDescription(),
                sum,
                expenseRequestDTO.getDate(),
                category,
                payer);

        travelService.addExpense(travel, expense);

        distributeExpenseShares(expense,
                expenseRequestDTO.getParticipantShares(),
                payer.getId());

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

        if (expenseRequestDTO.getParticipantShares().isEmpty()) {
            throw new InvalidParticipantShareException("Трата должна содержать хотя бы одного участника");
        }

        // Все ли являются участниками поездки?
        travelMemberService.validateAllUsersInTravel(travelId, expenseRequestDTO.getParticipantShares().keySet());

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
                throw new InvalidParticipantShareException("Доля участника с id = " + entry.getKey() + " не может быть пустой");
            if (entry.getValue().signum() <= 0)
                throw new InvalidParticipantShareException("Доля участника с id = " + entry.getKey() + " должна быть положительной");
        }
    }


    /**
     * Проверяет, что плательщик указан среди участников траты.
     */
    private void validatePayerInParticipants(Long payerId, Set<Long> participantUserIds) {

        if (participantUserIds != null && !participantUserIds.contains(payerId)) {
            throw new PayerNotInParticipantsException("Плательщик должен участвовать в трате id = " + payerId);
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
            for (Map.Entry<Long, BigDecimal> participantShare : participantShares.entrySet()) {

                User participant = travelMemberService.findUserInTravel(participantShare.getKey(), expense.getTravel().getId());

                MemberExpense memberExpense;

                if (participant.getId().equals(payerId)) {
                    memberExpense = MemberExpenseFactory.create(participant, participantShare.getValue());
                } else {
                    memberExpense = MemberExpenseFactory.create(participant, participantShare.getValue().negate());
                }

                memberExpenseService.addMemberExpense(expense, memberExpense);
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
     * @throws TravelNotFoundException          если поездка не найдена
     */
    @Transactional
    public void deleteExpense(Long travelId, Long expenseId) {

        Expense expense = this.findExpenseInTravel(expenseId, travelId);
        Travel travel = travelService.findTravel(travelId);

        travelService.removeExpense(travel, expense);
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

        return expenseRepository.findByIdAndTravelId(expenseId, travelId)
                .orElseThrow(() -> new ExpenseNotFoundInTravelException("Трата с id = " +
                        expenseId + " не найдена в поездке с id = " + travelId));
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
     *                                          //TODO    * @throws TravelNotFoundException          если поездка не найдена
     * @throws ExpenseNotFoundInTravelException если трата не найдена
     */
    @Transactional
    public ExpenseResponseDTO updateExpense(Long travelId, Long expenseId,
                                            ExpenseUpdateRequestDTO expenseUpdateRequestDTO) {

        // Если DTO пустое, обновлять нечего
        if (!this.hasAnyField(expenseUpdateRequestDTO)) {

            throw new EmptyUpdateRequestException("Отсутствуют поля для обновления траты");
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
                    travelMemberService.findUserInTravel(
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
            expense.setCategory(categoryService.findCategory(categoryId));
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

            var participants = expense.getMemberExpenses().stream()
                    .map(me -> me.getParticipant().getId()).collect(Collectors.toSet());
            for (var id : participantShares.keySet()) {
                if (!participants.contains(id))
                    throw new UserNotFoundExpenseException("Пользователь с id = " + id +
                            " не является участником траты '" + expense.getName() + "'");
            }
            validateShares(participantShares);
        }
    }


    /**
     * Удаляет участников из траты и пересчитывает сумму расхода.
     * <p>
     * Метод проверяет:
     * - непустой список участников для удаления
     * - что все участники присутствуют в данной трате
     * - что плательщик не удаляется
     *
     * @param travelId       ID путешествия
     * @param expenseId      ID траты
     * @param participantsId набор ID участников для удаления
     * @return актуальный ExpenseResponseDTO после удаления участников
     * @throws EmptyParticipantsListException        если participantsId пустой
     * @throws CannotRemovePayerFromExpenseException если пытаются удалить плательщика
     * @throws UserNotFoundExpenseException          если участник не найден в трате
     */
    @Transactional
    public ExpenseResponseDTO deleteParticipantsFromExpense(Long travelId, Long expenseId, Set<Long> participantsId) {

        if (participantsId == null || participantsId.isEmpty()) {
            throw new EmptyParticipantsListException("Список участников для удаления из траты пуст");
        }

        Expense expense = this.findExpenseInTravel(expenseId, travelId);

        var participantsIdInExpense = expense.getMemberExpenses().stream()
                .map(me -> me.getParticipant().getId())
                .collect(Collectors.toSet());
        for (Long id : participantsId) {
            if (!participantsIdInExpense.contains(id)) {
                throw new UserNotFoundExpenseException("Невозможно удалить пользователя с id = " + id +
                        ", так как его нет в трате '" + expense.getName() + "'");
            }
        }

        if (participantsId.contains(expense.getPayer().getId())) {
            throw new CannotRemovePayerFromExpenseException("Невозможно удалить плательщикас с id = " +
                    expense.getPayer().getId() + " из траты '" + expense.getName() + "'");
        }

        getMemberExpensesForIds(expense, participantsId).forEach(me ->
                memberExpenseService.removeMemberExpense(expense, me));

        expense.setSum(
                calculateSum(
                        expense.getMemberExpenses().stream()
                                .map(me -> me.getShare().abs())
                                .toList()
                )
        );

        return expenseDtoMapper.createExpenseResponseDTO(expense);
    }


    /**
     * Возвращает список MemberExpense для переданных ID участников.
     *
     * @param expense        текущая трата
     * @param participantsId набор ID участников
     * @return список MemberExpense для удаления
     */
    private List<MemberExpense> getMemberExpensesForIds(Expense expense, Set<Long> participantsId) {

        return expense.getMemberExpenses().stream()
                .filter((me) -> participantsId.contains(me.getParticipant().getId()))
                .toList();
    }


    /**
     * Добавляет новых участников к существующей трате.
     * <p>
     * Для каждого нового участника создается объект MemberExpense с отрицательной долей
     * (отражающей долг участника), после чего он добавляется в трату и пересчитывается общая сумма.
     * <p>
     * Проверки:
     * - participantShares не может быть null или пустым.
     * - Все пользователи должны быть участниками поездки.
     * - Новые участники не должны дублировать уже существующих в этой трате.
     * - Доли участников должны быть валидными (не null, > 0).
     *
     * @param travelId          идентификатор поездки
     * @param expenseId         идентификатор траты
     * @param participantShares карта "id пользователя → доля" для добавления
     * @return ExpenseResponseDTO с обновленной информацией о трате
     * @throws EmptyParticipantsListException   если participantShares null или пустая
     * @throws UserNotFoundInTravelException    если один или несколько пользователей не являются участниками поездки
     * @throws DuplicateParticipantException    если один или несколько участников уже есть в этой трате
     * @throws InvalidParticipantShareException если доля одного из участников некорректна
     * @throws ExpenseNotFoundInTravelException если трата не найдена
     */
    @Transactional
    public ExpenseResponseDTO addParticipantsToExpense(Long travelId, Long expenseId, Map<Long, BigDecimal> participantShares) {

        if (participantShares == null || participantShares.isEmpty()) {
            throw new EmptyParticipantsListException("Список участников для добавления в трату пуст");
        }

        this.validateShares(participantShares);

        Expense expense = findExpenseInTravel(expenseId, travelId);

        // Все ли являются участниками поездки?
        travelMemberService.validateAllUsersInTravel(travelId, participantShares.keySet());

        ensureParticipantsAreNew(expense, participantShares.keySet());

        List<MemberExpense> newParticipants = userService.getUsers(participantShares.keySet()).stream()
                .map(user -> MemberExpenseFactory.create(user, participantShares.get(user.getId()).negate()))
                .toList();

        newParticipants.forEach(me -> memberExpenseService.addMemberExpense(expense, me));

        expense.setSum(expense.getSum().add(calculateSum(participantShares.values())));

        return expenseDtoMapper.createExpenseResponseDTO(expense);
    }


    /**
     * Проверяет, что среди переданных идентификаторов участников нет дубликатов
     * относительно уже существующих участников данной траты.
     *
     * @param expense           текущая трата
     * @param newParticipantIds идентификаторы новых участников
     * @throws DuplicateParticipantException если один или несколько идентификаторов уже присутствуют в тратах
     */
    private void ensureParticipantsAreNew(Expense expense, Set<Long> newParticipantIds) {

        Set<Long> existingParticipantIds = expense.getMemberExpenses().stream()
                .map(me -> me.getParticipant().getId())
                .collect(Collectors.toSet());

        Set<Long> duplicates = newParticipantIds.stream()
                .filter(existingParticipantIds::contains)
                .collect(Collectors.toSet());

        if (!duplicates.isEmpty()) {
            throw new DuplicateParticipantException("В трате '" + expense.getName() + "'" +
                    " уже участвует пользователь с id: " +
                    duplicates.stream().sorted()
                            .map(Object::toString)
                            .collect(Collectors.joining(", ")));
        }
    }


    /**
     * Возвращает все расходы указанной поездки в виде DTO с общей суммой и количеством расходов.
     *
     * @param travelId идентификатор поездки
     * @return DTO с суммой всех расходов, количеством и списком расходов
     * @throws TravelNotFoundException если поездка с указанным travelId не найдена
     */
    public TravelExpensesResponseDTO getAllExpensesInTravel(Long travelId) {

        travelService.checkTravel(travelId);

        List<Expense> expenses = findAllExpensesInTravel(travelId);

        List<ExpenseResponseDTO> expensesDTO = expenses.stream()
                .map(expenseDtoMapper::createExpenseResponseDTO)
                .toList();

        BigDecimal amount = expensesDTO.stream()
                .map(ExpenseResponseDTO::sum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return TravelExpensesResponseDTO.builder()
                .expenses(expensesDTO)
                .totalAmount(amount)
                .totalCount(expensesDTO.size())
                .build();
    }


    /**
     * Возвращает все расходы указанной поездки в виде DTO с общей суммой и количеством расходов.
     *
     * @param travelId идентификатор поездки
     * @return DTO с суммой всех расходов, количеством и списком расходов
     */
    public List<Expense> findAllExpensesInTravel(Long travelId) {

        return expenseRepository.findAllByTravelIdOrderByDateDesc(travelId);
    }

    public List<Expense> findAllByTravelIdAndPayerIdOrderByDateDesc(Long travelId, Long payerId) {

        return expenseRepository.findAllByTravelIdAndPayerIdOrderByDateDesc(travelId, payerId);
    }

    public List<Expense> findExpensesWhereUserIsDebtor(Long travelId, Long userId) {

        return expenseRepository.findExpensesWhereUserIsDebtor(userId, travelId);
    }

}