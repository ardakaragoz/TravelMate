package com.travelmate.travelmate.session;

import com.google.cloud.firestore.*;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.Trip;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TripList {
    public static HashMap<String, Trip> trips = new HashMap<>();

    public static void addTrip(Trip trip) {
        trips.put(trip.getId(), trip);
    }

    public static Trip getTrip(String id) {
        return trips.get(id);
    }

    public static void loadAllTrips() {
        Firestore db = FirebaseService.getFirestore();
        db.collection("trips").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            if (snapshots != null) {
                Platform.runLater(() -> {
                    // --- OPTIMIZATION: Process ONLY changes ---
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        QueryDocumentSnapshot doc = dc.getDocument();
                        String id = doc.getId();

                        switch (dc.getType()) {
                            case ADDED:
                            case MODIFIED:
                                // Parse and update/add ONLY this trip
                                Trip trip = parseTrip(doc);
                                if (trip != null) trips.put(id, trip);
                                break;
                            case REMOVED:
                                trips.remove(id);
                                break;
                        }
                    }
                    System.out.println("Trip list updated efficiently. Total: " + trips.size());
                });
            }
        });
    }

    private static Trip parseTrip(QueryDocumentSnapshot doc) {
        try {
            String id = doc.getId();
            String user = doc.getString("user");

            ArrayList<String> pendingMates = (ArrayList<String>) doc.get("pendingMates");
            ArrayList<String> joinedMates = (ArrayList<String>) doc.get("joinedMates"); // Fixed Typo

            String additionalNotes = doc.getString("additionalNotes");
            int averageBudget = doc.getLong("averageBudget") != null ? doc.getLong("averageBudget").intValue() : 0;
            String currency = doc.getString("currency");
            int days = doc.getLong("days") != null ? doc.getLong("days").intValue() : 0;
            Date departureDate = doc.getDate("departureDate");
            String destination = doc.getString("destination");
            int mateCount = doc.getLong("mateCount") != null ? doc.getLong("mateCount").intValue() : 0;
            String itinerary = doc.getString("itinerary");
            String departureLocation = doc.getString("departureLocation");
            Date endDate = doc.getDate("endDate");

            Trip trip = new Trip(id, destination, departureLocation, days, averageBudget, currency, departureDate, endDate, user, itinerary, mateCount, additionalNotes);
            trip.setPendingMates(pendingMates);
            trip.setJoinedMates(joinedMates);

            return trip;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}