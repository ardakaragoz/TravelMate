package com.travelmate.travelmate.model;

import com.travelmate.travelmate.session.UserList;

import java.util.concurrent.ExecutionException;

public class Admin extends User {
    public Admin(String id, String username, String name, String nationality, String email,
                 String password, String gender, int age) throws ExecutionException, InterruptedException {
        super(id, username, name, nationality, email, password, gender, age);
    }

    public Admin(String id) throws ExecutionException, InterruptedException {
        super(id);
    }

    public void acceptRecommendation(Recommendation recommendation) throws ExecutionException, InterruptedException {

        recommendation.setStatus("ACCEPTED");
        UserList.getUser(recommendation.getSender()).increaseLevel(10);
    }

    public void rejectRecommendation(Recommendation recommendation) throws ExecutionException, InterruptedException {
        recommendation.setStatus("REJECTED");
    }
}
