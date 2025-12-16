package com.travelmate.travelmate.model;

import java.util.ArrayList;

public class Profile {
    private int id;
    private String biography;
    private String nationality;
    private String profilePictureUrl;
    private ArrayList<Hobby> hobbies;
    private ArrayList<TripTypes> favoriteTripTypes;

    public Profile(int id, String nationality) {
        this.id = id;
        this.nationality = nationality;
        this.hobbies = new ArrayList<>();
        this.favoriteTripTypes = new ArrayList<>();
    }

    public void addHobby(Hobby hobby) {
        if (!hobbies.contains(hobby)) {
            hobbies.add(hobby);
        }
    }

    public void removeHobby(Hobby hobby) {
        hobbies.remove(hobby);
    }

    public void addTripType(TripTypes tripType) {
        if (!favoriteTripTypes.contains(tripType)) {
            favoriteTripTypes.add(tripType);
        }
    }

    public void removeTripType(TripTypes tripType) {
        favoriteTripTypes.remove(tripType);
    }

    // Getters
    public int getId() { return id; }
    public String getBiography() { return biography; }
    public String getNationality() { return nationality; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public ArrayList<Hobby> getHobbies() { return hobbies; }
    public ArrayList<TripTypes> getFavoriteTripTypes() { return favoriteTripTypes; }

    // Setters
    public void setBiography(String biography) { this.biography = biography; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}
