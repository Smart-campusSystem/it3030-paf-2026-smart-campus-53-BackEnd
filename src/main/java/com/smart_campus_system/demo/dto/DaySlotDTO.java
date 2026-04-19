package com.smart_campus_system.demo.dto;

import java.time.LocalDateTime;

/** One bookable hour window on a calendar day, with conflict flag. */
public class DaySlotDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;

    public DaySlotDTO() {
    }

    public DaySlotDTO(LocalDateTime startTime, LocalDateTime endTime, boolean available) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
