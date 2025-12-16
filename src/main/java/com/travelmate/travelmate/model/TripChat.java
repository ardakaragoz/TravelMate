package com.travelmate.travelmate.model;

public class TripChat extends ChatRoom {
    private Trip trip;

    public TripChat(int id, String name, Trip trip) {
        super(id, name);
        this.trip = trip;
    }

    public Trip getTrip() {
        return trip;
    }
}
