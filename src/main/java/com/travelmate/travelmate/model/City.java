package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class City {
    private String name;
    private String id;
    private String pictureURL;
    private int[] compatibilityScores;

    public City(String id, String name, String pictureURL, int[] compatibilityScores) throws ExecutionException, InterruptedException {
        this.id = id;
        this.name = name;
        this.pictureURL = pictureURL;
        this.compatibilityScores = compatibilityScores;
    }



    // Getters and Setters
    public String getName() { return name; }
    public String getPictureURL() { return pictureURL; }
    public int[] getCompatibilityScores() { return compatibilityScores; }

    public void setCompatibilityScores(int[] compatibilityScores) {
        this.compatibilityScores = compatibilityScores;
        Firestore db = FirebaseService.getFirestore();
        HashMap<String, Object> data = new HashMap<>();
        data.put("funPoint", compatibilityScores[0]);
        data.put("culturePoint", compatibilityScores[1]);
        data.put("chillPoint", compatibilityScores[2]);
        db.collection("cities").document(id).set(data);
    }
}
