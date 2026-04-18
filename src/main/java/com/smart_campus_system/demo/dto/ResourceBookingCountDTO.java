package com.smart_campus_system.demo.dto;

public class ResourceBookingCountDTO {
    private Long resourceId;
    private String resourceName;
    private int bookingCount;

    public ResourceBookingCountDTO() {
    }

    public ResourceBookingCountDTO(Long resourceId, String resourceName, int bookingCount) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.bookingCount = bookingCount;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int bookingCount) {
        this.bookingCount = bookingCount;
    }
}
