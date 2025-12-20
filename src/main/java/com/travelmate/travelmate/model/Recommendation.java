package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Recommendation {
    private String message;
    private String id;
    private User sender;
    private Channel channel;
    private Date createdAt;
    private String status;
    private Firestore db = FirebaseService.getFirestore();

    public Recommendation(String id, String message, User sender, Channel channel) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.channel = channel;
        this.createdAt = new Date();
        this.status = "PENDING";
        Map<String, Object> recoMap = new HashMap<>();
        recoMap.put("message", message);
        recoMap.put("sender", sender);
        recoMap.put("createdAt", (long) createdAt.getTime());
        db.collection("recommendations").document(id).set(recoMap);
    }

    public Recommendation(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        DocumentSnapshot data = db.collection("recommendations").document(id).get().get();
        this.message = data.getString("message");
        this.sender = new User(data.getString("sender"));
        this.createdAt = new Date(data.getLong("createdAt"));
    }


    public String getId() {
        return id;
    }

    public String getMessage() { return message; }
    public User getSender() { return sender; }
    public Channel getChannel() { return channel; }
    public Date getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }

    // Setters
    public void setStatus(String status) {
        this.status = status;
        db.collection("recommendations").document(id).update("status", status);
    }
}

