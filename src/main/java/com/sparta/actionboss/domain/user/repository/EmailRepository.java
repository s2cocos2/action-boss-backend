package com.sparta.actionboss.domain.user.repository;

import com.sparta.actionboss.domain.user.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {
    Optional<Email> findByEmail(String email);
}
