package com.smart_campus_system.demo.service;

import com.smart_campus_system.demo.model.Otp;
import com.smart_campus_system.demo.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Generates a 6-digit OTP, saves it, and sends the email
    @Transactional
    public void generateAndSendOtp(String email) {
        // Clear any previous OTPs for this email
        otpRepository.deleteByEmail(email);

        String code = String.format("%06d", new Random().nextInt(999999));

        Otp otp = Otp.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // Valid for 5 minutes
                .build();
        
        otpRepository.save(otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Your Smart Campus Registration OTP");
        message.setText("Your Smart Campus system registration OTP code is: " + code + "\n\nIt is valid for 5 minutes.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("SMTP Exception: " + e.getMessage());
            throw new com.smart_campus_system.demo.exception.ApiException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Email delivery failed. Please check your SMTP App Password and settings: " + e.getMessage()
            );
        }
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        // Clean up expired OTPs just in case
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        Optional<Otp> optionalOtp = otpRepository.findByEmailAndCode(email, code);
        if (optionalOtp.isPresent()) {
            Otp otp = optionalOtp.get();
            if (otp.getExpiresAt().isAfter(LocalDateTime.now())) {
                otpRepository.delete(otp); // OTP is verified and should be discarded
                return true;
            } else {
                otpRepository.delete(otp); // Discard expired OTP
            }
        }
        return false;
    }
}
