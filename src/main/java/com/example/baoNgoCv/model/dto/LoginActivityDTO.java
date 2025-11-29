package com.example.baoNgoCv.model.dto;

import java.time.LocalDateTime;

public class LoginActivityDTO {

    private Long id;
    private LocalDateTime loginTimestamp;
    private String ipAddress;
    private String userAgent;
    private String location;

    public LoginActivityDTO() {
    }

    public LoginActivityDTO(Long id, LocalDateTime loginTimestamp, String ipAddress, String userAgent, String location) {
        this.id = id;
        this.loginTimestamp = loginTimestamp;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.location = location;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getLoginTimestamp() { return loginTimestamp; }
    public void setLoginTimestamp(LocalDateTime loginTimestamp) { this.loginTimestamp = loginTimestamp; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}