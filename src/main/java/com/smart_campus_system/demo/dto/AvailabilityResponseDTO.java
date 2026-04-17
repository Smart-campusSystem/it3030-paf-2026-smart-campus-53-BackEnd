package com.smart_campus_system.demo.dto;

import java.time.LocalDateTime;

public class AvailabilityResponseDTO {
    private boolean available;
    private LocalDateTime conflictingBookingStart;
    private LocalDateTime conflictingBookingEnd;
    private String message;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getConflictingBookingStart() {
        return conflictingBookingStart;
    }

    public void setConflictingBookingStart(LocalDateTime conflictingBookingStart) {
        this.conflictingBookingStart = conflictingBookingStart;
    }

    public LocalDateTime getConflictingBookingEnd() {
        return conflictingBookingEnd;
    }

    public void setConflictingBookingEnd(LocalDateTime conflictingBookingEnd) {
        this.conflictingBookingEnd = conflictingBookingEnd;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

