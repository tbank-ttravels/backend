package com.tbank.ttravels_backend.dto.exspense;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @param id           ID траты
 * @param name         Название
 * @param description  Описание
 * @param sum          Сумма
 * @param date         Дата расхода
 * @param categoryId   ID категории
 * @param categoryName Название категории
 * @param payer        Имя Фамилия Плательщик
 * @param participants Список участников
 */

@Builder
public record ExpenseResponseDTO(Long id,
                                 String name,
                                 String description,
                                 BigDecimal sum,
                                 OffsetDateTime date,
                                 Long categoryId,
                                 String categoryName,
                                 String payer,
                                 Set<MemberExpenseResponseDTO> participants) {

}
