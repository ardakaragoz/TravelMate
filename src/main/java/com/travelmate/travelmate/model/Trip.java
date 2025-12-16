package com.travelmate.travelmate.model;

import java.util.ArrayList;
import java.util.Date;

public class Trip {
    private int id;
    private String destination;
    private String departureLocation;
    private String additionalNotes;
    private int days;
    private int averageBudget;
    private int mateCount;
    private String currency;
    private String itinerary;
    private Date departureDate;
    private Date endDate;
    private User user;
    private ArrayList<User> joinedMates;
    private ArrayList<User> pendingMates;

    public Trip(int id, String destination, String departureLocation, int days,
                int averageBudget, String currency, Date departureDate, Date endDate, User user) {
        this.id = id;
        this.destination = destination;
        this.departureLocation = departureLocation;
        this.days = days;
        this.averageBudget = averageBudget;
        this.currency = currency;
        this.departureDate = departureDate;
        this.endDate = endDate;
        this.user = user;
        this.joinedMates = new ArrayList<>();
        this.pendingMates = new ArrayList<>();
        this.mateCount = 0;
    }

    public City getDestination() {
        return new City(destination, "", "");
    }

    public void addMate(User mate) {
        if (!joinedMates.contains(mate)) {
            joinedMates.add(mate);
            removePendingMate(mate);
            mateCount++;
        }
    }

    public void addPendingMate(User mate) {
        if (!pendingMates.contains(mate) && !joinedMates.contains(mate)) {
            pendingMates.add(mate);
        }
    }
    public void removePendingMate(User mate) {
        pendingMates.remove(mate);
    }

    public void removeMate(User mate) {
        if (joinedMates.remove(mate)) {
            mateCount--;
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getDestinationName() { return destination; }
    public String getDepartureLocation() { return departureLocation; }
    public String getAdditionalNotes() { return additionalNotes; }
    public int getDays() { return days; }
    public int getAverageBudget() { return averageBudget; }
    public int getMateCount() { return mateCount; }
    public String getCurrency() { return currency; }
    public String getItinerary() { return itinerary; }
    public Date getDepartureDate() { return departureDate; }
    public Date getEndDate() { return endDate; }
    public User getUser() { return user; }
    public ArrayList<User> getJoinedMates() { return joinedMates; }
    public ArrayList<User> getPendingMates() { return pendingMates; }

    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    public void setItinerary(String itinerary) { this.itinerary = itinerary; }
}
