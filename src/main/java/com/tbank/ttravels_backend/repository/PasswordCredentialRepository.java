package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.PasswordCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordCredentialRepository extends JpaRepository<PasswordCredential, Long> {
    Optional<PasswordCredential> findByUserId(Long id);
}
