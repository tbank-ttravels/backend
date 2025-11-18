package com.tbank.ttravels_backend.dto.exspense;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @param id           ID траты
 * @param name         Название
 * @param payerId      ID платящего
 * @param description  Описание
 * @param sum          Сумма
 * @param date         Дата расхода
 * @param categoryId   ID категории
 * @param categoryName Название категории
 * @param participants Список участников
 */

@Builder
public record ExpenseResponseDTO(Long id,
                                 Long payerId,
                                 String name,
                                 String description,
                                 BigDecimal sum,
                                 OffsetDateTime date,
                                 Long categoryId,
                                 String categoryName,
                                 Set<MemberExpenseResponseDTO> participants) {

}
