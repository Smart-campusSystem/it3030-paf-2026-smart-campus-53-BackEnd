package com.smart_campus_system.demo.dto;

public class AdminNotificationRequest {

    private String message;
    private String type;       // INFO, WARNING, ALERT
    private String userEmail;  // null = broadcast to all

    public String getMessage()  { return message; }
    public void setMessage(String m) { this.message = m; }

    public String getType()     { return type; }
    public void setType(String t) { this.type = t; }

    public String getUserEmail()  { return userEmail; }
    public void setUserEmail(String e) { this.userEmail = e; }
}
