package com.travelmate.travelmate.model;

import com.google.cloud.Timestamp; // Import this
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.UserList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Message {
    private String id;
    private String message;
    private Date createdAt;
    private User sender;

    // Empty constructor for Firestore
    public Message() {}

    // --- Sending Constructor ---
    // This constructor AUTOMATICALLY saves to Firestore.
    public Message(String id, String message, User sender) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.createdAt = new Date();

        Firestore db = FirebaseService.getFirestore();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        // We only save the sender ID, not the whole User object (prevents array errors)
        messageMap.put("sender", sender.getId());
        messageMap.put("createdAt", createdAt); // Save as Date object (Firestore handles conversion)

        db.collection("messages").document(id).set(messageMap);
    }

    // --- Legacy Slow Constructor ---
    public Message(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot data = db.collection("messages").document(id).get().get();

        if (data.exists()) {
            this.message = data.getString("message");
            parseCreatedAt(data);
            if (data.getString("sender") != null) {
                this.sender = UserList.getUser(data.getString("sender"));
            }
        }
    }

    // --- NEW: Fast Loading Constructor (FIXED) ---
    public Message(DocumentSnapshot data) {
        this.id = data.getId();
        if (data.exists()) {
            this.message = data.getString("message");
            parseCreatedAt(data);
            if (data.getString("sender") != null) {
                this.sender = UserList.getUser(data.getString("sender"));
            }
        }
    }

    // Helper to safely parse date
    private void parseCreatedAt(DocumentSnapshot data) {
        try {
            Timestamp timestamp = data.getTimestamp("createdAt");
            if (timestamp != null) {
                this.createdAt = timestamp.toDate();
            } else {
                Long time = data.getLong("createdAt");
                if (time != null) {
                    this.createdAt = new Date(time);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing date for message " + id);
            this.createdAt = new Date();
        }
    }

    // Getters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public Date getCreatedAt() { return createdAt; }
    public User getSender() { return sender; }
}