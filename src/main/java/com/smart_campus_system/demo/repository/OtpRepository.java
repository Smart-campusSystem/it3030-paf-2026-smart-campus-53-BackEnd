package com.smart_campus_system.demo.repository;

import com.smart_campus_system.demo.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
