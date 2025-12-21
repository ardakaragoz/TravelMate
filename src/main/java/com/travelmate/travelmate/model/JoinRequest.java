package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class JoinRequest {
    private String id;
    private User requester;
    private User tripOwner;
    private String message;
    private String status;
    private Trip trip;

    public JoinRequest(String id, User requester, User tripOwner, String message, Trip trip) throws ExecutionException, InterruptedException {
        this.id = id;
        this.requester = requester;
        this.tripOwner = tripOwner;
        this.message = message;
        this.trip = trip;
        this.status = "PENDING";
        Firestore db = FirebaseService.getFirestore();
        Map<String, Object> data = new HashMap<>();
        data.put("requester", requester.getId());
        data.put("tripOwner", tripOwner.getId());
        data.put("message", message);
        data.put("status", status);
        data.put("trip", trip.getId());
        db.collection("join_requests").document(id).set(data).get();
    }

    public JoinRequest(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot data = db.collection("join_requests").document(id).get().get();
        this.requester = UserList.getUser(data.get("requester").toString());
        this.tripOwner = UserList.getUser(data.get("tripOwner").toString());
        this.message = data.get("message").toString();
        this.status = data.get("status").toString();
        this.trip = TripList.getTrip(data.get("trip").toString());
    }

    // Getters and Setters
    public String getId() { return id; }
    public User getRequester() { return requester; }
    public User getTripOwner() { return tripOwner; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Trip getTrip() { return trip; }

    public void setStatus(String status) throws ExecutionException, InterruptedException {
        this.status = status;
        Firestore db = FirebaseService.getFirestore();
        db.collection("joinrequests").document(id).update("status", status).get();
    }
}