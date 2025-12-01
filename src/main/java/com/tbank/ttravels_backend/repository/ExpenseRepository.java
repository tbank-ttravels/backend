package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndTravelId(Long id, Long travelId);

    List<Expense> findAllByTravelIdOrderByDateDesc(Long travelId);

    List<Expense> findAllByTravelIdAndPayerIdOrderByDateDesc(Long travelId, Long payerId);

    @Query("""
    select e from Expense e
    join e.memberExpenses me
    where me.participant.id = :userId
      and e.payer.id <> :userId
      and e.travel.id = :travelId
""")
    List<Expense> findExpensesWhereUserIsDebtor(@Param("userId") Long userId,
                                                @Param("travelId") Long travelId);

}