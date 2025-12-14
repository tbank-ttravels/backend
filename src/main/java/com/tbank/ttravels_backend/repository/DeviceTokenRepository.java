package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserIdAndIsActiveTrue(Long userId);

    void deleteByToken(String token);

    Optional<DeviceToken> findByToken(String token);

     @Modifying
    @Transactional
    @Query("""
        update DeviceToken dt
        set dt.isActive = false
        where dt.token = :token and dt.isActive = true
    """)
    int deactivateByToken(@Param("token") String token);
}
