package com.travelmate.travelmate.model;

public class City {
    private String name;
    private String country;
    private String pictureURL;
    private int[] compatibilityScores;

    public City(String name, String country, String pictureURL) {
        this.name = name;
        this.country = country;
        this.pictureURL = pictureURL;
        this.compatibilityScores = new int[10];
    }

    // Getters and Setters
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getPictureURL() { return pictureURL; }
    public int[] getCompatibilityScores() { return compatibilityScores; }

    public void setCompatibilityScores(int[] compatibilityScores) {
        this.compatibilityScores = compatibilityScores;
    }
}
