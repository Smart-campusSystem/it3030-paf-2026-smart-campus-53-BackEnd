package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.config.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectRequestDTO {

    @NotBlank(message = "reason is required")
    @Size(min = AppConstants.REJECTION_REASON_MIN_LENGTH, max = AppConstants.REJECTION_REASON_MAX_LENGTH,
            message = "reason must be between 10 and 500 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

