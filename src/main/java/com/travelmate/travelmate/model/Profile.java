package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
        this.hobbies = new ArrayList<>();
        this.favoriteTripTypes = new ArrayList<>();
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot doc = db.collection("profiles").document(id).get().get();
        if (doc.exists()) {
            nationality = doc.getString("nationality");
            biography = doc.getString("biography");
            profilePictureUrl = doc.getString("profilePictureUrl");
            hobbies = (ArrayList<String>) doc.get("hobbies");
            favoriteTripTypes = (ArrayList<String>) doc.get("favoriteTripTypes");

        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("biography", biography);
            data.put("nationality", nationality);
            data.put("profilePictureUrl", profilePictureUrl);
            data.put("hobbies", hobbies);
            data.put("favoriteTripTypes", favoriteTripTypes);
            db.collection("profiles").document(id).set(data).get();
        }
    }

    public void addHobby(Hobby hobby) {
        if (!hobbies.contains(hobby.getId())) {
            hobbies.add(hobby.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("hobbies", hobbies);
            Firestore db = FirebaseService.getFirestore();
            db.collection("profiles").document(id).update("hobbies", data);
        }
    }

    public void removeHobby(Hobby hobby) {
        hobbies.remove(hobby.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("hobbies", hobbies);
        Firestore db = FirebaseService.getFirestore();
        db.collection("profiles").document(id).update("hobbies", data);
    }

    public void addTripType(TripTypes tripType) {
        if (!favoriteTripTypes.contains(tripType.getId())) {
            favoriteTripTypes.add(tripType.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("favoriteTripTypes", hobbies);
            Firestore db = FirebaseService.getFirestore();
            db.collection("profiles").document(id).update("favoriteTripTypes", data);
        }
    }

    public void removeTripType(TripTypes tripType) {
        favoriteTripTypes.remove(tripType.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("favoriteTripTypes", hobbies);
        Firestore db = FirebaseService.getFirestore();
        db.collection("profiles").document(id).update("favoriteTripTypes", data);
    }

    // Getters
    public String getId() { return id; }
    public String getBiography() { return biography; }
    public String getNationality() { return nationality; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public ArrayList<Hobby> getHobbies() throws ExecutionException, InterruptedException {
        ArrayList<Hobby> hobbiesList = new ArrayList<>();
        for (String hobbyId : hobbies) {
            hobbiesList.add(new Hobby(hobbyId));
        }
        return hobbiesList;
    }
    public ArrayList<TripTypes> getFavoriteTripTypes() throws ExecutionException, InterruptedException {
        ArrayList<TripTypes> triptypeList = new ArrayList<>();
        for (String typeID : favoriteTripTypes) {
            triptypeList.add(new TripTypes(typeID));
        }

        return triptypeList;
    }

    // Setters
    public void setBiography(String biography) {
        this.biography = biography;
        Map<String, Object> data = new HashMap<>();
        data.put("biography", hobbies);
        Firestore db = FirebaseService.getFirestore();
        db.collection("profiles").document(id).update("biography", data);
    }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
        Map<String, Object> data = new HashMap<>();
        data.put("profilePictureUrl", hobbies);
        Firestore db = FirebaseService.getFirestore();
        db.collection("profiles").document(id).update("profilePictureUrl", data);
    }
}
