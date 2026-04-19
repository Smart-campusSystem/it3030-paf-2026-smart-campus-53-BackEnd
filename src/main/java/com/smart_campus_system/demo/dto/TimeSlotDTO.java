package com.smart_campus_system.demo.dto;

import java.time.LocalDateTime;

public class TimeSlotDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public TimeSlotDTO() {
    }

    public TimeSlotDTO(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
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
}
