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
                    System.err.println("Channel dinlenirken hata oluştu: " + e.getMessage());
                    return;
                }

                if (snapshots != null) {
                    // JavaFX arayüzü ile çakışmaması için güncellemeyi JavaFX Thread'ine alıyoruz
                    Platform.runLater(() -> {
                        trips.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Trip trip = null;
                            try {
                                String id = doc.getId();
                                String user = doc.getString("user");
                                String tripChat = doc.getString("tripChat");
                                ArrayList<String> pendingMates = (ArrayList<String>) doc.get("pendingMates");
                                ArrayList<String> joinedMates = (ArrayList<String>) doc.get("pendingMates");
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
                                trip = new Trip(id, destination, departureLocation, days, averageBudget, currency, departureDate, endDate, user, itinerary, mateCount, additionalNotes);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }

                            addTrip(trip);
                        }
                        System.out.println("Başarılı! Toplam " + trips.size() + " trip hafızaya alındı/güncellendi.");
                    });
                }
            }
        });
    }
}
