package com.travelmate.travelmate.model;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class DirectMessage extends ChatRoom {

    public DirectMessage(String id, User user1, User user2) throws ExecutionException, InterruptedException {
        super(id, "Direct");
        this.addUser(user1);
        this.addUser(user2);
    }

    public DirectMessage(String id, ArrayList<String> activeUsers, ArrayList<String> messages) throws ExecutionException, InterruptedException {
        super(id, "Direct", messages, activeUsers);
    }
}
