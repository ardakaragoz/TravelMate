package com.travelmate.travelmate.model;

public class TripTypes {
    private int id;
    private String name;
    private int[] compatibilityScores;

    public TripTypes(int id, String name) {
        this.id = id;
        this.name = name;
        this.compatibilityScores = new int[10];
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public int[] getCompatibilityScores() { return compatibilityScores; }

    public void setCompatibilityScores(int[] compatibilityScores) {
        this.compatibilityScores = compatibilityScores;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TripTypes tripTypes = (TripTypes) obj;
        return id == tripTypes.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}