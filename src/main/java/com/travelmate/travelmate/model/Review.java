package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Review {
    private String id;
    private int friendlinessPoint;
    private int reliabilityPoint;
    private int communicationPoint;
    private int adaptationPoint;
    private int budgetPoint;
    private int helpfulnessPoint;
    private double overallPoints = 0;
    private String comments;
    private User evaluatedUser;
    private User evaluatorUser;
    private Trip trip;
    private Firestore db = FirebaseService.getFirestore();


    public Review(String id, int friendlinessPoint, int reliabilityPoint, int communicationPoint,
                  int adaptationPoint, int budgetPoint, int helpfulnessPoint,
                  String comments, User evaluatedUser, User evaluatorUser, Trip trip) {
        this.id = id;
        this.friendlinessPoint = friendlinessPoint;
        this.reliabilityPoint = reliabilityPoint;
        this.communicationPoint = communicationPoint;
        this.adaptationPoint = adaptationPoint;
        this.budgetPoint = budgetPoint;
        this.helpfulnessPoint = helpfulnessPoint;
        this.comments = comments;
        this.evaluatedUser = evaluatedUser;
        this.evaluatorUser = evaluatorUser;
        this.trip = trip;
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("friendlinessPoint", friendlinessPoint);
        data.put("reliabilityPoint", reliabilityPoint);
        data.put("communicationPoint", communicationPoint);
        data.put("adaptationPoint", adaptationPoint);
        data.put("budgetPoint", budgetPoint);
        data.put("helpfulnessPoint", helpfulnessPoint);
        data.put("comments", comments);
        data.put("evaluatedUser", evaluatedUser.getId());
        data.put("evaluatorUser", evaluatorUser.getId());
        data.put("trip", trip.getId());
        db.collection("reviews").document(id).set(data);
        calculateOverall();
    }

    public Review(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        DocumentSnapshot data = db.collection("reviews").document(id).get().get();
        this.friendlinessPoint = data.getLong("friendlinessPoint").intValue();
        this.reliabilityPoint = data.getLong("reliabilityPoint").intValue();
        this.communicationPoint = data.getLong("communicationPoint").intValue();
        this.adaptationPoint = data.getLong("adaptationPoint").intValue();
        this.budgetPoint = data.getLong("budgetPoint").intValue();
        this.helpfulnessPoint = data.getLong("helpfulnessPoint").intValue();
        this.comments = data.get("comments").toString();
        this.evaluatedUser = UserList.getUser(data.get("evaluatedUser").toString());
        this.evaluatorUser = UserList.getUser(data.get("evaluatorUser").toString());
        this.trip = TripList.getTrip(data.get("trip").toString());
        calculateOverall();
    }

    public void calculateOverall() {
        this.overallPoints = (friendlinessPoint + reliabilityPoint + communicationPoint +
                             adaptationPoint + budgetPoint + helpfulnessPoint) / 6.0;
    }



    public Trip getTrip() {
        return trip;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public int getFriendlinessPoint() { return friendlinessPoint; }
    public int getReliabilityPoint() { return reliabilityPoint; }
    public int getCommunicationPoint() { return communicationPoint; }
    public int getAdaptationPoint() { return adaptationPoint; }
    public int getBudgetPoint() { return budgetPoint; }
    public int getHelpfulnessPoint() { return helpfulnessPoint; }
    public double getOverallPoints() { return overallPoints; }
    public String getComments() { return comments; }
    public User getEvaluatedUser() { return evaluatedUser; }
    public User getEvaluatorUser() { return evaluatorUser; }
}