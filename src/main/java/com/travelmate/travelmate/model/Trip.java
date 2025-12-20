package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.CityList;

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

    public Trip(String id, String destination, String departureLocation, int days,
                int averageBudget, String currency, LocalDate departureDate, LocalDate endDate, User user, String itinerary, int mateCount, String additionalNotes) throws ExecutionException, InterruptedException {
        this.id = id;
        this.destination = destination;
        this.departureLocation = departureLocation;
        this.days = days;
        this.averageBudget = averageBudget;
        this.currency = currency;
        Date date1 = Date.from(
                departureDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        Date date2 = Date.from(
                endDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        this.departureDate = date1;
        this.endDate = date2;
        this.user = user;
        this.joinedMates = new ArrayList<>();
        this.pendingMates = new ArrayList<>();
        this.mateCount = mateCount;
        this.tripChat = new TripChat(id, this);
        this.itinerary = itinerary;
        this.additionalNotes = additionalNotes;
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("destination", destination);
        data.put("departureLocation", departureLocation);
        data.put("days", days);
        data.put("averageBudget", averageBudget);
        data.put("currency", currency);
        data.put("departureDate", date1);
        data.put("endDate", date2);
        data.put("user", user.getId());
        data.put("joinedMates", joinedMates);
        data.put("pendingMates", pendingMates);
        data.put("mateCount", mateCount);
        data.put("tripChat", tripChat.getId());
        data.put("itinerary", itinerary);
        data.put("additionalNotes", additionalNotes);
        user.addCurrentTrip(id);
        db.collection("trips").document(id).set(data).get();
    }

    public Trip(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        DocumentSnapshot data = db.collection("trips").document(id).get().get();
        this.destination = data.getString("destination");
        this.departureLocation = data.getString("departureLocation");
        this.additionalNotes = data.getString("additionalNotes");
        this.days = Integer.parseInt(data.get("days").toString());
        this.averageBudget = Integer.parseInt(data.get("averageBudget").toString());
        this.mateCount = Integer.parseInt(data.get("mateCount").toString());
        this.currency = data.getString("currency");
        this.itinerary = data.getString("itinerary");
        this.departureDate = data.getDate("departureDate");
        this.endDate = data.getDate("endDate");
        this.user = new User(data.getString("user"));
        this.joinedMates = new ArrayList<>();
        this.pendingMates = new ArrayList<>();
        this.joinedMates = (ArrayList) data.get("joinedMates");
        this.pendingMates = (ArrayList) data.get("pendingMates");
        this.tripChat = new TripChat(data.get("tripChat").toString(), this);
        this.itinerary = data.getString("itinerary");
        this.additionalNotes = data.getString("additionalNotes");
    }

    public void updateTrip() throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("destination", destination);
        data.put("departureLocation", departureLocation);
        data.put("days", days);
        data.put("averageBudget", averageBudget);
        data.put("currency", currency);
        data.put("departureDate", departureDate);
        data.put("endDate", endDate);
        data.put("user", user.getId());
        data.put("joinedMates", joinedMates);
        data.put("pendingMates", pendingMates);
        data.put("mateCount", mateCount);
        db.collection("trips").document(id).set(data).get();
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
