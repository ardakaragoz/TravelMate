package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

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
    private double overallPoints;
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
        calculateOverall();
    }

    public Review(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        DocumentSnapshot data = db.collection("reviews").document(id).get().get();
        this.friendlinessPoint = (int) data.get("friendlinessPoint");
        this.reliabilityPoint = (int) data.get("reliabilityPoint");
        this.communicationPoint = (int) data.get("communicationPoint");
        this.adaptationPoint = (int) data.get("adaptationPoint");
        this.budgetPoint = (int) data.get("budgetPoint");
        this.helpfulnessPoint = (int) data.get("helpfulnessPoint");
        this.comments = data.get("comments").toString();
        this.evaluatedUser = new User(data.get("evaluatedUser").toString());
        this.evaluatorUser = new User(data.get("evaluatorUser").toString());
        this.trip = new Trip(data.get("trip").toString());
        calculateOverall();
    }

    public void calculateOverall() {
        this.overallPoints = (friendlinessPoint + reliabilityPoint + communicationPoint +
                             adaptationPoint + budgetPoint + helpfulnessPoint) / 6.0;
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