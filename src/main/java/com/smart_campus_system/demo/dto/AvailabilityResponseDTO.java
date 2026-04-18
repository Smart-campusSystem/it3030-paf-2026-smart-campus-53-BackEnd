package com.smart_campus_system.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityResponseDTO {
    private boolean available;
    private LocalDateTime conflictingBookingStart;
    private LocalDateTime conflictingBookingEnd;
    private String message;

    /** Set when {@code date} is provided: free 1-hour slots on that day. */
    private List<TimeSlotDTO> availableSlots = new ArrayList<>();

    /**
     * When {@code date} is provided: every 1-hour window in the resource day (e.g. 06:00–22:00),
     * each marked available or not. UI can show unavailable slots as disabled.
     */
    private List<DaySlotDTO> daySlots = new ArrayList<>();

    /** When optional same-day window is requested alongside {@code date}. */
    private Boolean requestedWindowAvailable;

    private LocalDate date;

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

    public List<TimeSlotDTO> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<TimeSlotDTO> availableSlots) {
        this.availableSlots = availableSlots != null ? availableSlots : new ArrayList<>();
    }

    public Boolean getRequestedWindowAvailable() {
        return requestedWindowAvailable;
    }

    public void setRequestedWindowAvailable(Boolean requestedWindowAvailable) {
        this.requestedWindowAvailable = requestedWindowAvailable;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<DaySlotDTO> getDaySlots() {
        return daySlots;
    }

    public void setDaySlots(List<DaySlotDTO> daySlots) {
        this.daySlots = daySlots != null ? daySlots : new ArrayList<>();
    }
}

