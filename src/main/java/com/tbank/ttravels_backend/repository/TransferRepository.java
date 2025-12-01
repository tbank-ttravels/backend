package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findAllByTravel_Id(Long travelId);

    Optional<Transfer> findByIdAndTravel_Id(Long id, Long travelId);

    List<Transfer> findAllByTravel_IdAndRecipient_Id(Long travelId, Long recipientId);

    List<Transfer> findAllByTravel_IdAndSender_Id(Long travelId, Long senderId);
}
