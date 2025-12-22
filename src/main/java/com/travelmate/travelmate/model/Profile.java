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
            // --- SAFE LOADING (Prevents crashes from bad data) ---
            if (doc.getString("nationality") != null) nationality = doc.getString("nationality");
            if (doc.getString("biography") != null) biography = doc.getString("biography");

            // 1. Check if the field exists and IS A STRING (Fixes the "List" bug)
            Object picObj = doc.get("profilePictureUrl");
            if (picObj instanceof String) {
                profilePictureUrl = (String) picObj;
                System.out.println("[DEBUG] Profile Loaded Picture: " + profilePictureUrl);
            } else if (picObj != null) {
                System.err.println("[ERROR] Corrupted Profile Picture Data Found! Type: " + picObj.getClass().getSimpleName());
            }

            if (doc.get("hobbies") != null) hobbies = (ArrayList<String>) doc.get("hobbies");
            if (doc.get("favoriteTripTypes") != null) favoriteTripTypes = (ArrayList<String>) doc.get("favoriteTripTypes");
        } else {
            // Create default profile
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

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
        System.out.println("[DEBUG] Saving Picture URL: " + profilePictureUrl);

        CompletableFuture.runAsync(() -> {
            Firestore db = FirebaseService.getFirestore();

            // 1. Update Profile (Detailed info)
            db.collection("profiles").document(id).update("profilePictureUrl", profilePictureUrl);

            // 2. Update User (For Chat/Home access)
            db.collection("users").document(id).update("profilePicture", profilePictureUrl);
        });
    }

    public void addHobby(Hobby hobby) {
        if (!hobbies.contains(hobby.getId())) {
            hobbies.add(hobby.getId());
            CompletableFuture.runAsync(() ->
                    FirebaseService.getFirestore().collection("profiles").document(id).update("hobbies", hobbies)
            );
        }
    }

    public void removeHobby(Hobby hobby) {
        if (hobbies.remove(hobby.getId())) {
            CompletableFuture.runAsync(() ->
                    FirebaseService.getFirestore().collection("profiles").document(id).update("hobbies", hobbies)
            );
        }
    }

    public void addTripType(TripTypes tripType) {
        if (!favoriteTripTypes.contains(tripType.getId())) {
            favoriteTripTypes.add(tripType.getId());
            CompletableFuture.runAsync(() ->
                    FirebaseService.getFirestore().collection("profiles").document(id).update("favoriteTripTypes", favoriteTripTypes)
            );
        }
    }

    public void removeTripType(TripTypes tripType) {
        if (favoriteTripTypes.remove(tripType.getId())) {
            CompletableFuture.runAsync(() ->
                    FirebaseService.getFirestore().collection("profiles").document(id).update("favoriteTripTypes", favoriteTripTypes)
            );
        }
    }

    public void setBiography(String biography) {
        this.biography = biography;
        CompletableFuture.runAsync(() ->
                FirebaseService.getFirestore().collection("profiles").document(id).update("biography", biography)
        );
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
        CompletableFuture.runAsync(() ->
                FirebaseService.getFirestore().collection("profiles").document(id).update("nationality", nationality)
        );
    }

    // Getters
    public String getId() { return id; }
    public String getBiography() { return biography; }
    public String getNationality() { return nationality; }
    public String getProfilePictureUrl() { return profilePictureUrl; }

    public ArrayList<Hobby> getHobbies() throws ExecutionException, InterruptedException {
        ArrayList<Hobby> hobbiesList = new ArrayList<>();
        for (String hobbyId : hobbies) {
            hobbiesList.add(HobbyList.getHobby(hobbyId));
        }
        return hobbiesList;
    }

    public ArrayList<TripTypes> getFavoriteTripTypes() throws ExecutionException, InterruptedException {
        ArrayList<TripTypes> triptypeList = new ArrayList<>();
        for (String typeID : favoriteTripTypes) {
            triptypeList.add(TripTypeList.getTripType(typeID));
        }
        return triptypeList;
    }
}