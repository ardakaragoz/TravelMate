package com.travelmate.travelmate.model;

import java.util.concurrent.ExecutionException;

public class TripChat extends ChatRoom {
    private Trip trip;

    public TripChat(String id, Trip trip) throws ExecutionException, InterruptedException {
        super(id, "TripChat");
        this.trip = trip;
    }

    public Trip getTrip() {
        return trip;
    }
}
