package com.travelmate.travelmate.model;

import java.util.ArrayList;

public class Channel {
    private int id;
    private String name;
    private ArrayList<User> members;
    private ArrayList<Trip> tripRequests;
    private ArrayList<Recommendation> recommendations;
    private ArrayList<Message> messages;

    public Channel(int id, String name) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
        this.tripRequests = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public void addTripRequest(Trip trip) {
        tripRequests.add(trip);
    }

    public void addRecommendation(Recommendation recommendation) {
        recommendations.add(recommendation);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void addParticipant(User user) {
        if (!members.contains(user)) {
            members.add(user);
            user.getChannels().add(this);
        }
    }

    public void removeParticipant(User user) {
        members.remove(user);
        user.getChannels().remove(this);
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public ArrayList<User> getMembers() { return members; }
    public ArrayList<Trip> getTripRequests() { return tripRequests; }
    public ArrayList<Recommendation> getRecommendations() { return recommendations; }
    public ArrayList<Message> getMessages() { return messages; }
}
