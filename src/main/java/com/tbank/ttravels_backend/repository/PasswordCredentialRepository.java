package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.PasswordCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordCredentialRepository extends JpaRepository<PasswordCredential, Long> {
}
