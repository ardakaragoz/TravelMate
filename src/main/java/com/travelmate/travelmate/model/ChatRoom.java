package com.travelmate.travelmate.model;

import java.util.ArrayList;

public class ChatRoom {
    private int id;
    private ArrayList<Message> messages;
    private ArrayList<User> activeUsers;
    private String type;

    public ChatRoom(int id, String type) {
        this.id = id;
        this.type = type;
        this.messages = new ArrayList<>();
        this.activeUsers = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void addUser(User user) {
        if (!activeUsers.contains(user)) {
            activeUsers.add(user);
        }
    }

    public void removeUser(User user) {
        activeUsers.remove(user);
    }

    // Getters and Setters
    public int getId() { return id; }
    public ArrayList<Message> getMessages() { return messages; }
    public ArrayList<User> getActiveUsers() { return activeUsers; }
    public String getType() { return type; }
}