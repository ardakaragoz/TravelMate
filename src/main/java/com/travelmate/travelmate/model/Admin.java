package com.travelmate.travelmate.model;

import java.util.concurrent.ExecutionException;

public class Admin extends User {
    public Admin(String id, String username, String name, String nationality, String email,
                 String password, String gender, int age) throws ExecutionException, InterruptedException {
        super(id, username, name, nationality, email, password, gender, age);
    }

    public Admin(String id) throws ExecutionException, InterruptedException {
        super(id);
    }

    public void acceptRecommendation(Recommendation recommendation) {
        recommendation.setStatus("ACCEPTED");
    }

    public void rejectRecommendation(Recommendation recommendation) {
        recommendation.setStatus("REJECTED");
    }
}
