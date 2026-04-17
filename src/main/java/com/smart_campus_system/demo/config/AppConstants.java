package com.smart_campus_system.demo.config;

import java.time.LocalTime;

public final class AppConstants {
    private AppConstants() {
    }

    public static final String API_BASE = "/api";
    public static final String BOOKINGS_BASE = API_BASE + "/bookings";

    public static final LocalTime RESOURCE_AVAILABLE_FROM = LocalTime.of(6, 0);
    public static final LocalTime RESOURCE_AVAILABLE_UNTIL = LocalTime.of(22, 0);

    public static final int PURPOSE_MAX_LENGTH = 500;
    public static final int REJECTION_REASON_MIN_LENGTH = 10;
    public static final int REJECTION_REASON_MAX_LENGTH = 500;
    public static final int EXPECTED_ATTENDEES_MIN = 1;
    public static final int EXPECTED_ATTENDEES_MAX = 500;
}

