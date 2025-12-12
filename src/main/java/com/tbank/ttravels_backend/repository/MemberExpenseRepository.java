package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.MemberExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberExpenseRepository extends JpaRepository<MemberExpense, Long> {
}