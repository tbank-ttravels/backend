package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}