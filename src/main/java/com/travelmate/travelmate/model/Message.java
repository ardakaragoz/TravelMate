package com.travelmate.travelmate.model;

import java.sql.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Message {
    private String id;
    private String message;
    private Date createdAt;
    private User sender;

    public Message(String id, String message, User sender) throws ExecutionException, InterruptedException {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.createdAt = new Date();
        Firestore db = FirebaseService.getFirestore();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("sender", sender);
        messageMap.put("createdAt", (long) createdAt.getTime());
        db.collection("messages").document(id).set(messageMap).get();
    }

    public Message(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot data = db.collection("messages").document(id).get().get();
        this.message = data.getString("message");
        this.createdAt = new Date(data.getLong("createdAt"));
        this.sender = new User(data.getString("sender"));

    }

    // Getters and Setters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public Date getCreatedAt() { return createdAt; }
    public User getSender() { return sender; }
}
