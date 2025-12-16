package com.travelmate.travelmate.model;

public class Hobby {
    private int id;
    private String name;
    private int[] compatibilityScores;

    public Hobby(int id, String name) {
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
        Hobby hobby = (Hobby) obj;
        return id == hobby.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}