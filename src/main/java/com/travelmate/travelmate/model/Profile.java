package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.HobbyList;
import com.travelmate.travelmate.session.TripTypeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Profile {
    private String id;
    private String biography;
    private String nationality;
    private String profilePictureUrl;
    private ArrayList<String> hobbies;
    private ArrayList<String> favoriteTripTypes;

    public Profile(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        this.nationality = "";
        this.biography = "";
        this.profilePictureUrl = "";
        this.hobbies = new ArrayList<>();
        this.favoriteTripTypes = new ArrayList<>();

        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot doc = db.collection("profiles").document(id).get().get();

        if (doc.exists()) {
            if (doc.getString("nationality") != null) nationality = doc.getString("nationality");
            if (doc.getString("biography") != null) biography = doc.getString("biography");

            // Safe Load
            if (doc.get("profilePictureUrl") instanceof String) {
                profilePictureUrl = doc.getString("profilePictureUrl");
            }

            if (doc.get("hobbies") != null) hobbies = (ArrayList<String>) doc.get("hobbies");
            if (doc.get("favoriteTripTypes") != null) favoriteTripTypes = (ArrayList<String>) doc.get("favoriteTripTypes");
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("biography", biography);
            data.put("nationality", nationality);
            data.put("profilePictureUrl", profilePictureUrl);
            data.put("hobbies", hobbies);
            data.put("favoriteTripTypes", favoriteTripTypes);
            db.collection("profiles").document(id).set(data);
        }
    }

    // --- FIXED: METHOD NAME & DATABASE KEYS ---
    public void setProfilePicture(String url) {
        this.profilePictureUrl = url;
        System.out.println("[DEBUG] Saving Picture URL to DB: " + url);

        CompletableFuture.runAsync(() -> {
            Firestore db = FirebaseService.getFirestore();
            // 1. Update Profile Collection (Using "profilePictureUrl")
            db.collection("profiles").document(id).update("profilePictureUrl", url);

            // 2. Update Users Collection (FIXED: Now using "profilePictureUrl" to match User.java)
            db.collection("users").document(id).update("profilePictureUrl", url);
        });
    }

    public void addHobby(Hobby hobby) {
        if (!hobbies.contains(hobby.getId())) {
            hobbies.add(hobby.getId());
            CompletableFuture.runAsync(() -> FirebaseService.getFirestore().collection("profiles").document(id).update("hobbies", hobbies));
        }
    }
    public void removeHobby(Hobby hobby) {
        if (hobbies.remove(hobby.getId())) {
            CompletableFuture.runAsync(() -> FirebaseService.getFirestore().collection("profiles").document(id).update("hobbies", hobbies));
        }
    }
    public void addTripType(TripTypes tripType) {
        if (!favoriteTripTypes.contains(tripType.getId())) {
            favoriteTripTypes.add(tripType.getId());
            CompletableFuture.runAsync(() -> FirebaseService.getFirestore().collection("profiles").document(id).update("favoriteTripTypes", favoriteTripTypes));
        }
    }
    public void removeTripType(TripTypes tripType) {
        if (favoriteTripTypes.remove(tripType.getId())) {
            CompletableFuture.runAsync(() -> FirebaseService.getFirestore().collection("profiles").document(id).update("favoriteTripTypes", favoriteTripTypes));
        }
    }
    public String getId() { return id; }
    public String getBiography() { return biography; }
    public String getNationality() { return nationality; }

    // Getter name kept as getProfilePictureUrl for compatibility with JSON mapping if needed, or you can rename to getProfilePicture
    public String getProfilePictureUrl() { return profilePictureUrl; }

    public ArrayList<Hobby> getHobbies() throws ExecutionException, InterruptedException {
        ArrayList<Hobby> list = new ArrayList<>();
        for (String id : hobbies) list.add(HobbyList.getHobby(id));
        return list;
    }
    public ArrayList<TripTypes> getFavoriteTripTypes() throws ExecutionException, InterruptedException {
        ArrayList<TripTypes> list = new ArrayList<>();
        for (String id : favoriteTripTypes) list.add(TripTypeList.getTripType(id));
        return list;
    }
    public void setBiography(String biography) {
        this.biography = biography;
        CompletableFuture.runAsync(() -> FirebaseService.getFirestore().collection("profiles").document(id).update("biography", biography));
    }
    public void setNationality(String nationality) {
        this.nationality = nationality;
        CompletableFuture.runAsync(() -> FirebaseService.getFirestore().collection("profiles").document(id).update("nationality", nationality));
    }
}