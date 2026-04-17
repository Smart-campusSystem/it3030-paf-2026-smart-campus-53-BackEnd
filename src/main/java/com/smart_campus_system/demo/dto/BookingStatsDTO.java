package com.smart_campus_system.demo.dto;

public class BookingStatsDTO {
    private int totalBookingsToday;
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
    private String mostBookedResourceName;
    private int mostBookedResourceCount;

    public int getTotalBookingsToday() {
        return totalBookingsToday;
    }

    public void setTotalBookingsToday(int totalBookingsToday) {
        this.totalBookingsToday = totalBookingsToday;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(int approvedCount) {
        this.approvedCount = approvedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public String getMostBookedResourceName() {
        return mostBookedResourceName;
    }

    public void setMostBookedResourceName(String mostBookedResourceName) {
        this.mostBookedResourceName = mostBookedResourceName;
    }

    public int getMostBookedResourceCount() {
        return mostBookedResourceCount;
    }

    public void setMostBookedResourceCount(int mostBookedResourceCount) {
        this.mostBookedResourceCount = mostBookedResourceCount;
    }
}

