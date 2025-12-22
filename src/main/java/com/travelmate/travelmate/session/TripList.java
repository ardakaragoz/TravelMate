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

    public static Trip getTrip(String name) {
        return trips.get(name);
    }

    public static void loadAllTrips() {
        Firestore db = FirebaseService.getFirestore();
        db.collection("trips").addSnapshotListener(new EventListener<QuerySnapshot>() {
            public void onEvent(QuerySnapshot snapshots, FirestoreException e) {
                if (e != null) {
                    System.err.println("Trip dinlenirken hata oluştu: " + e.getMessage());
                    return;
                }

                if (snapshots != null) {
                    Platform.runLater(() -> {
                        trips.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Trip trip = null;
                            try {
                                String id = doc.getId();
                                String user = doc.getString("user");

                                // --- FIX 1: Read lists correctly ---
                                ArrayList<String> pendingMates = (ArrayList<String>) doc.get("pendingMates");
                                // --- FIX 2: Fixed Typo (was reading "pendingMates" again) ---
                                ArrayList<String> joinedMates = (ArrayList<String>) doc.get("joinedMates");

                                String additionalNotes = (String) doc.get("additionalNotes");
                                int averageBudget =  doc.getLong("averageBudget").intValue();
                                String currency = (String) doc.get("currency");
                                int days = doc.getLong("days").intValue();
                                Date departureDate = doc.getDate("departureDate");
                                String destination = (String) doc.get("destination");
                                int mateCount = doc.getLong("mateCount").intValue();
                                String itinerary = (String) doc.get("itinerary");
                                String departureLocation = (String) doc.get("departureLocation");
                                Date endDate = doc.getDate("endDate");

                                // This constructor creates a NEW empty list, erasing your data
                                trip = new Trip(id, destination, departureLocation, days, averageBudget, currency, departureDate, endDate, user, itinerary, mateCount, additionalNotes);

                                // --- FIX 3: PUT THE DATA BACK IN ---
                                trip.setPendingMates(pendingMates);
                                trip.setJoinedMates(joinedMates);

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            if (trip != null) addTrip(trip);
                        }
                        System.out.println("Başarılı! Toplam " + trips.size() + " trip hafızaya alındı.");
                    });
                }
            }
        });
    }
}