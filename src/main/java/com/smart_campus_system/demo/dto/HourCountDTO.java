package com.smart_campus_system.demo.dto;

public class HourCountDTO {
    private int hourOfDay;
    private long bookingCount;

    public HourCountDTO() {
    }

    public HourCountDTO(int hourOfDay, long bookingCount) {
        this.hourOfDay = hourOfDay;
        this.bookingCount = bookingCount;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(long bookingCount) {
        this.bookingCount = bookingCount;
    }
}
