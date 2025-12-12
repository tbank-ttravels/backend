package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserIdAndIsActiveTrue(Long userId);

    void deleteByToken(String token);

    Optional<DeviceToken> findByToken(String token);
}
