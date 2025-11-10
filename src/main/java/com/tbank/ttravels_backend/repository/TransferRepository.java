package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findAllByTravel_Id(Long travelId);
}
