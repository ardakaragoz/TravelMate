package com.travelmate.travelmate.model;

public class JoinRequest {
    private int id;
    private User requester;
    private User tripOwner;
    private String message;
    private String status;
    private Trip trip;

    public JoinRequest(int id, User requester, User tripOwner, String message, Trip trip) {
        this.id = id;
        this.requester = requester;
        this.tripOwner = tripOwner;
        this.message = message;
        this.trip = trip;
        this.status = "PENDING";
    }

    public JoinRequest(int id, User requester, User tripOwner, String message, Trip trip, String status) {
        this.id = id;
        this.requester = requester;
        this.tripOwner = tripOwner;
        this.message = message;
        this.trip = trip;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public User getRequester() { return requester; }
    public User getTripOwner() { return tripOwner; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Trip getTrip() { return trip; }

    public void setStatus(String status) { this.status = status; }
}