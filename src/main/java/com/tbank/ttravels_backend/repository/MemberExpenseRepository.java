package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.MemberExpense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberExpenseRepository extends JpaRepository<MemberExpense, Long> {
}