package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Channel {
    private String id;
    private String name;
    private ArrayList<String> members;
    private ArrayList<String> tripRequests;
    private ArrayList<String> recommendations;
    private ChannelChat channelChat;

    public Channel(String id, String name) throws Exception {
        this.id = id;
        Firestore db = FirebaseService.getFirestore();

        DocumentSnapshot doc = db.collection("channels")
                .document(id)
                .get()
                .get();

        if (doc.exists()) {
            this.name = doc.get("name").toString();
            this.members = (ArrayList<String>) doc.get("members");
            this.tripRequests = (ArrayList<String>) doc.get("tripRequests");
            this.recommendations = (ArrayList<String>) doc.get("recommendations");
            this.channelChat = new ChannelChat(doc.getString("channelChat"), this);
        } else {
            this.name = name;
            this.members = new ArrayList<>();
            this.tripRequests = new ArrayList<>();
            this.recommendations = new ArrayList<>();
            this.channelChat = new ChannelChat(id, this);
            updateChannel();
        }



    }

    public void updateChannel() throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();

        data.put("name", name);
        data.put("members", members);
        data.put("tripRequests", tripRequests);
        data.put("recommendations", recommendations);
        data.put("channelChat", channelChat.getId());

        Firestore db = FirebaseService.getFirestore();
        db.collection("channels")
                .document(this.id)
                .set(data)
                .get();
    }

    public void addTripRequest(Trip trip) throws ExecutionException, InterruptedException {
        tripRequests.add(trip.getId());
        updateChannel();
    }

    public void addRecommendation(Recommendation recommendation) throws ExecutionException, InterruptedException {
        recommendations.add(recommendation.getId());
        updateChannel();
    }



    public void addParticipant(User user) throws ExecutionException, InterruptedException {
        if (!members.contains(user.getId())) {
            members.add(user.getId());
            user.joinChannel(this);
            updateChannel();
        }
    }

    public void removeParticipant(User user) throws ExecutionException, InterruptedException {
        members.remove(user.getId());
        user.getChannels().remove(this);
        updateChannel();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public ArrayList<String> getMembers() { return members; }
    public ArrayList<String> getTripRequests() { return tripRequests; }
    public ArrayList<String> getRecommendations() { return recommendations; }
    public ChannelChat getChannelChat() { return channelChat; }
}
