package com.travelmate.travelmate.model;

public class Review {
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

    public Review(int friendlinessPoint, int reliabilityPoint, int communicationPoint,
                  int adaptationPoint, int budgetPoint, int helpfulnessPoint,
                  String comments, User evaluatedUser, User evaluatorUser) {
        this.friendlinessPoint = friendlinessPoint;
        this.reliabilityPoint = reliabilityPoint;
        this.communicationPoint = communicationPoint;
        this.adaptationPoint = adaptationPoint;
        this.budgetPoint = budgetPoint;
        this.helpfulnessPoint = helpfulnessPoint;
        this.comments = comments;
        this.evaluatedUser = evaluatedUser;
        this.evaluatorUser = evaluatorUser;
        calculateOverall();
    }

    public void calculateOverall() {
        this.overallPoints = (friendlinessPoint + reliabilityPoint + communicationPoint +
                             adaptationPoint + budgetPoint + helpfulnessPoint) / 6.0;
    }

    // Getters and Setters
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