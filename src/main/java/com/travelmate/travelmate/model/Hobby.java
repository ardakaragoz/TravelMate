package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Hobby {
    private String id;
    private String name;
    private int[] compatibilityScores;

    public Hobby(String id, String name) throws ExecutionException, InterruptedException {
        this.id = id;
        this.name = name;
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot data = db.collection("hobbies").document(id).get().get();
        if (data.exists()){
            this.compatibilityScores = (int[]) data.get("compatibilityScores");
        } else {
            this.compatibilityScores = new int[3];
            Arrays.fill(compatibilityScores, 0);
        }
    }

    public Hobby(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot data = db.collection("hobbies").document(id).get().get();
        if (data.exists()){
            this.name = data.getString("name");
            this.compatibilityScores = (int[]) data.get("compatibilityScores");
        } else {
            this.compatibilityScores = new int[3];
            Arrays.fill(compatibilityScores, 0);
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int[] getCompatibilityScores() { return compatibilityScores; }

    public void setCompatibilityScores(int[] compatibilityScores) {

        this.compatibilityScores = compatibilityScores;
        Firestore db = FirebaseService.getFirestore();
        HashMap<String, Object> data = new HashMap<>();
        data.put("compatibilityScores", compatibilityScores);
        db.collection("cities").document(id).set(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hobby hobby = (Hobby) obj;
        return id.equals(hobby.id);
    }

}