package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.UserList;
import com.google.cloud.Timestamp; // Required for Firestore dates


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Message {
    private String id;
    private String message;
    private Date createdAt;
    private User sender;

    public Message() {}

    // --- Sending Constructor (Saves to DB) ---
    public Message(String id, String message, User sender) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.createdAt = new Date();

        Firestore db = FirebaseService.getFirestore();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("sender", sender.getId());
        messageMap.put("createdAt", createdAt); // Firestore saves this as Timestamp

        db.collection("messages").document(id).set(messageMap);
    }

    // --- Loading Constructor (Fixes the Crash) ---
    public Message(DocumentSnapshot data) {
        this.id = data.getId();
        if (data.exists()) {
            this.message = data.getString("message");

            // --- FIX: Safely handle Timestamp vs Long ---
            Object createdObj = data.get("createdAt");
            if (createdObj instanceof Timestamp) {
                this.createdAt = ((Timestamp) createdObj).toDate();
            } else if (createdObj instanceof Number) {
                this.createdAt = new Date(((Number) createdObj).longValue());
            } else {
                this.createdAt = new Date(); // Fallback
            }

            String senderId = data.getString("sender");
            if (senderId != null) {
                this.sender = UserList.getUser(senderId);
            }
        }
    }

    // Getters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public Date getCreatedAt() { return createdAt; }
    public User getSender() { return sender; }
}