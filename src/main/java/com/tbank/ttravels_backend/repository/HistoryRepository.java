package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
}