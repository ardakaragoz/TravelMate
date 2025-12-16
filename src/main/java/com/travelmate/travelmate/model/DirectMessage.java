package com.travelmate.travelmate.model;

public class DirectMessage extends ChatRoom {

    public DirectMessage(int id, User user1, User user2) {
        super(id, "DIRECT");
        this.addUser(user1);
        this.addUser(user2);
    }
}
