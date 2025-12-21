package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.CityList;
import com.travelmate.travelmate.session.UserList;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Trip {
    private String id;
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
    private ArrayList<String> joinedMates;
    private ArrayList<String> pendingMates;
    private TripChat tripChat;
    private Firestore db = FirebaseService.getFirestore();

    // --- CRITICAL FIX: Empty Constructor ---
    public Trip() {
        this.joinedMates = new ArrayList<>();
        this.pendingMates = new ArrayList<>();
    }

    public Trip(String id, String destination, String departureLocation, int days,
                int averageBudget, String currency, Date departureDate, Date endDate, User user, String itinerary, int mateCount, String additionalNotes) throws ExecutionException, InterruptedException {
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
        this.mateCount = mateCount;
        this.tripChat = new TripChat(id, this);
        this.itinerary = itinerary;
        this.additionalNotes = additionalNotes;
    }

    public Trip(String id, String destination, String departureLocation, int days,
                int averageBudget, String currency, LocalDate departureDate, LocalDate endDate, User user, String itinerary, int mateCount, String additionalNotes) throws ExecutionException, InterruptedException {
        this.id = id;
        this.destination = destination;
        this.departureLocation = departureLocation;
        this.days = days;
        this.averageBudget = averageBudget;
        this.currency = currency;

        Date date1 = Date.from(departureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date date2 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        this.departureDate = date1;
        this.endDate = date2;
        this.user = user;
        this.joinedMates = new ArrayList<>();
        this.pendingMates = new ArrayList<>();
        this.mateCount = mateCount;
        this.tripChat = new TripChat(id, this);
        this.itinerary = itinerary;
        this.additionalNotes = additionalNotes;

        // This is safe to keep here as it's part of your creation logic
        user.addTripRequest(this);
        updateTrip();
    }

    public Trip(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        DocumentSnapshot data = db.collection("trips").document(id).get().get();
        if (data.exists()) {
            this.destination = data.getString("destination");
            this.departureLocation = data.getString("departureLocation");
            this.additionalNotes = data.getString("additionalNotes");
            if (data.get("days") != null) this.days = Integer.parseInt(data.get("days").toString());
            if (data.get("averageBudget") != null) this.averageBudget = Integer.parseInt(data.get("averageBudget").toString());
            if (data.get("mateCount") != null) this.mateCount = Integer.parseInt(data.get("mateCount").toString());
            this.currency = data.getString("currency");
            this.itinerary = data.getString("itinerary");
            this.departureDate = data.getDate("departureDate");
            this.endDate = data.getDate("endDate");

            if (data.getString("user") != null) {
                this.user = UserList.getUser(data.getString("user"));
            }

            this.joinedMates = (ArrayList<String>) data.get("joinedMates");
            this.pendingMates = (ArrayList<String>) data.get("pendingMates");

            // Safety checks
            if (this.joinedMates == null) this.joinedMates = new ArrayList<>();
            if (this.pendingMates == null) this.pendingMates = new ArrayList<>();

            if (data.get("tripChat") != null) {
                this.tripChat = new TripChat(data.get("tripChat").toString(), this);
            }
        }
    }

    public void updateTrip() throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id); // Good to store ID in document too
        data.put("destination", destination);
        data.put("departureLocation", departureLocation);
        data.put("days", days);
        data.put("averageBudget", averageBudget);
        data.put("currency", currency);
        data.put("departureDate", departureDate);
        data.put("endDate", endDate);
        data.put("user", user != null ? user.getId() : null); // Store ID, not full object
        data.put("joinedMates", joinedMates);
        data.put("pendingMates", pendingMates);
        data.put("mateCount", mateCount);
        if (tripChat != null) data.put("tripChat", tripChat.getId());
        data.put("itinerary", itinerary);
        data.put("additionalNotes", additionalNotes);

        // Removed .get() to prevent blocking
        db.collection("trips").document(id).set(data);
    }

    public String getDestination() throws ExecutionException, InterruptedException {

        return destination;
    }

    public void addMate(User mate) throws ExecutionException, InterruptedException {
        if (!joinedMates.contains(mate.getId())) {
            joinedMates.add(mate.getId());
            removePendingMate(mate);
            mateCount++;
            updateTrip();
        }
    }

    public void addPendingMate(User mate) throws ExecutionException, InterruptedException {
        if (!pendingMates.contains(mate.getId()) && !joinedMates.contains(mate.getId())) {
            pendingMates.add(mate.getId());
            updateTrip();
        }
    }

    public void removePendingMate(User mate) throws ExecutionException, InterruptedException {
        pendingMates.remove(mate.getId());
        updateTrip();
    }

    public void removeMate(User mate) throws ExecutionException, InterruptedException {
        if (joinedMates.remove(mate.getId())) {
            mateCount--;
            updateTrip();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
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
    public ArrayList<String> getJoinedMates() { return joinedMates; }
    public ArrayList<String> getPendingMates() { return pendingMates; }

    public void setAdditionalNotes(String additionalNotes) throws ExecutionException, InterruptedException {
        this.additionalNotes = additionalNotes;
        updateTrip();
    }
    public void setItinerary(String itinerary) throws ExecutionException, InterruptedException {
        this.itinerary = itinerary;
        updateTrip();
    }
}