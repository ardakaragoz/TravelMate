package com.travelmate.travelmate.model;

public class Admin extends User {
    public Admin(String id, String username, String name, String nationality, String email,
                 String password, String gender, int age) {
        super(id, username, name, nationality, email, password, gender, age);
    }

    public void acceptRecommendation(Recommendation recommendation) {
        recommendation.setStatus("ACCEPTED");
    }

    public void rejectRecommendation(Recommendation recommendation) {
        recommendation.setStatus("REJECTED");
    }
}
