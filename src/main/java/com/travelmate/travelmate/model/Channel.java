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
    private String channelChat;

    public Channel(String id, String name, ArrayList<String> members, ArrayList<String> tripRequests, ArrayList<String> recommendations, String channelChatID) throws Exception {
        this.id = id;
        this.name = name;
        this.members = members;
        this.tripRequests = tripRequests;
        this.recommendations = recommendations;
        this.channelChat = channelChatID;
    }




    public void updateChannel() throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();

        data.put("name", name);
        data.put("members", members);
        data.put("tripRequests", tripRequests);
        data.put("recommendations", recommendations);
        data.put("channelChat", channelChat);

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
        user.leaveChannel(this);
        updateChannel();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public ArrayList<String> getMembers() { return members; }
    public ArrayList<String> getTripRequests() { return tripRequests; }
    public ArrayList<String> getRecommendations() { return recommendations; }
    public String getChannelChat() { return channelChat; }
}
