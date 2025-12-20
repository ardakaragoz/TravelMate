package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TripTypes {
    private String id;
    private String name;
    private int[] compatibilityScores;

    public TripTypes(String id, String name, int[] compatibilityScores) throws ExecutionException, InterruptedException {
        this.id = id;
        this.name = name;
        this.compatibilityScores = compatibilityScores;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int[] getCompatibilityScores() { return compatibilityScores; }

    public void setCompatibilityScores(int[] compatibilityScores) {

        this.compatibilityScores = compatibilityScores;
        Firestore db = FirebaseService.getFirestore();
        HashMap<String, Object> data = new HashMap<>();
        data.put("funPoint", compatibilityScores[0]);
        data.put("culturePoint", compatibilityScores[1]);
        data.put("chillPoint", compatibilityScores[2]);
        db.collection("trip_types").document(id).set(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TripTypes tripTypes = (TripTypes) obj;
        return id.equals(tripTypes.id);
    }

}