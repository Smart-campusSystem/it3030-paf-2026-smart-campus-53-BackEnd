package com.smart_campus_system.demo.dto;

import java.util.ArrayList;
import java.util.List;

public class BookingStatsDTO {
    private int totalBookingsToday;
    private long totalBookings;
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
    private int cancelledCount;
    private String mostBookedResourceName;
    private int mostBookedResourceCount;
    private List<ResourceBookingCountDTO> mostBookedResources = new ArrayList<>();
    private List<HourCountDTO> peakHours = new ArrayList<>();

    public int getTotalBookingsToday() {
        return totalBookingsToday;
    }

    public void setTotalBookingsToday(int totalBookingsToday) {
        this.totalBookingsToday = totalBookingsToday;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
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

    public int getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(int cancelledCount) {
        this.cancelledCount = cancelledCount;
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

    public List<ResourceBookingCountDTO> getMostBookedResources() {
        return mostBookedResources;
    }

    public void setMostBookedResources(List<ResourceBookingCountDTO> mostBookedResources) {
        this.mostBookedResources = mostBookedResources != null ? mostBookedResources : new ArrayList<>();
    }

    public List<HourCountDTO> getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(List<HourCountDTO> peakHours) {
        this.peakHours = peakHours != null ? peakHours : new ArrayList<>();
    }
}

