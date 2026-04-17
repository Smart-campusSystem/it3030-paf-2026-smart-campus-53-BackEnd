package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.config.AppConstants;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingRequestDTO {

    @NotNull(message = "resourceId is required")
    private Long resourceId;

    @NotNull(message = "startTime is required")
    @Future(message = "startTime must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    @NotBlank(message = "purpose is required")
    @Size(max = AppConstants.PURPOSE_MAX_LENGTH, message = "purpose must be at most 500 characters")
    private String purpose;

    @NotNull(message = "expectedAttendees is required")
    @Min(value = AppConstants.EXPECTED_ATTENDEES_MIN, message = "expectedAttendees must be at least 1")
    @Max(value = AppConstants.EXPECTED_ATTENDEES_MAX, message = "expectedAttendees must be at most 500")
    private Integer expectedAttendees;

    @AssertTrue(message = "endTime must be after startTime")
    public boolean isEndAfterStart() {
        if (startTime == null || endTime == null) return true;
        return endTime.isAfter(startTime);
    }

    @AssertTrue(message = "Booking must be within resource availability window (06:00-22:00)")
    public boolean isWithinAvailabilityWindow() {
        if (startTime == null || endTime == null) return true;
        final LocalTime start = startTime.toLocalTime();
        final LocalTime end = endTime.toLocalTime();
        return !start.isBefore(AppConstants.RESOURCE_AVAILABLE_FROM)
                && !end.isAfter(AppConstants.RESOURCE_AVAILABLE_UNTIL);
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
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

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getExpectedAttendees() {
        return expectedAttendees;
    }

    public void setExpectedAttendees(Integer expectedAttendees) {
        this.expectedAttendees = expectedAttendees;
    }
}

