package com.travelmate.travelmate.model;

import java.util.Date;

public class Recommendation {
    private String message;
    private User sender;
    private Channel channel;
    private Date createdAt;
    private String status;

    public Recommendation(String message, User sender, Channel channel) {
        this.message = message;
        this.sender = sender;
        this.channel = channel;
        this.createdAt = new Date();
        this.status = "PENDING";
    }

    public Recommendation(String message, User sender, Channel channel, String status) {
        this.message = message;
        this.sender = sender;
        this.channel = channel;
        this.createdAt = new Date();
        this.status = status;
    }

    // Getters
    public String getMessage() { return message; }
    public User getSender() { return sender; }
    public Channel getChannel() { return channel; }
    public Date getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}

