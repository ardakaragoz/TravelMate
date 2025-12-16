package com.travelmate.travelmate.model;

import java.util.Date;
public class Message {
    private int id;
    private String message;
    private Date createdAt;
    private User sender;

    public Message(int id, String message, User sender) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getMessage() { return message; }
    public Date getCreatedAt() { return createdAt; }
    public User getSender() { return sender; }
}
