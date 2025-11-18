package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndTravelId(Long id, Long travelId);
}