package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.ChannelList;
import com.travelmate.travelmate.session.UserList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Recommendation {
    private String message;
    private String id;
    private String sender;
    private String channel;
    private Date createdAt;
    private String status;
    private String link;
    private Firestore db = FirebaseService.getFirestore();

    public Recommendation(String id, String message, String sender, String channel, String link) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.channel = channel;
        this.createdAt = new Date();
        this.status = "PENDING";
        this.link = link;
        Map<String, Object> recoMap = new HashMap<>();
        recoMap.put("message", message);
        recoMap.put("sender", sender);
        recoMap.put("createdAt", (long) createdAt.getTime());
        recoMap.put("status", status);
        recoMap.put("link", link);
        recoMap.put("channel", channel);
        db.collection("recommendations").document(id).set(recoMap);
    }

    public Recommendation(String id, String message, String sender, String channel, String link, String status, long createdAt) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.channel = channel;
        this.createdAt = new Date(createdAt);
        this.status = status;
        this.link = link;
    }

    public Recommendation(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        DocumentSnapshot data = db.collection("recommendations").document(id).get().get();
        this.message = data.getString("message");
        this.sender = data.getString("sender");
        this.createdAt = new Date(data.getLong("createdAt"));
        this.status = data.getString("status");
        this.link = data.getString("link");
        this.channel = data.getString("channel");
    }


    public String getId() {
        return id;
    }

    public String getMessage() { return message; }
    public String getSender() { return sender; }
    public String getChannel() { return channel; }
    public Date getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }

    public String getLink() {
        return link;
    }

    // Setters
    public void setStatus(String status) throws ExecutionException, InterruptedException {
        this.status = status;
        if (status.equals("ACCEPTED")){
            ChannelList.getChannel(channel).addRecommendation(this);
        }
        db.collection("recommendations").document(id).update("status", status);
    }
}

