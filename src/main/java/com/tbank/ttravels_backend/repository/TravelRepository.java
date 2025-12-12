package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.enums.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelRepository extends JpaRepository<Travel, Long> {
}
