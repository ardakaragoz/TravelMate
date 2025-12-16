package com.travelmate.travelmate.model;

import java.util.ArrayList;

public class User {
    private String id, username, name, nationality, email, password, gender;
    private int age, level, allPoints, monthlyPoints;
    private Profile profile;
    private ArrayList<Trip> pastTrips;
    private ArrayList<Trip> currentTrips;
    private ArrayList<Channel> channels;
    private ArrayList<Review> reviews;
    private int reviewCount;
    private int reviewPoints;
    private ArrayList<JoinRequest> joinRequests;
    private ArrayList<Recommendation> recommendations;
    private ArrayList<Message> messages;
    private ArrayList<Trip> tripRequests;
    private ArrayList<ChatRoom> chatRooms;

    public User(String id, String username, String name, String nationality, String email, 
                String password, String gender, int age) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.nationality = nationality;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.age = age;
        this.level = 1;
        this.allPoints = 0;
        this.monthlyPoints = 0;
        this.pastTrips = new ArrayList<>();
        this.currentTrips = new ArrayList<>();
        this.channels = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.reviewCount = 0;
        this.reviewPoints = 0;
    }

    public void addRequest(JoinRequest req) {
        this.joinRequests.add(req);
        req.getTrip().addPendingMate(this);
    }

    public void removeUserRequest(JoinRequest req, User mate) {
        req.getTrip().removePendingMate(mate);
    }

    public void addRecommendation(Recommendation recommendation) {
        this.recommendations.add(recommendation);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addChatRoom(ChatRoom chatRoom) {
        this.chatRooms.add(chatRoom);
    }

    public void addTripRequest(Trip trip) {
        this.currentTrips.add(trip);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        this.reviewCount++;
        this.reviewPoints += review.getOverallPoints();
    }

    public double getAverageReviewScore() {
        if (reviewCount == 0){
            return 0.0;
        }
        return (double) reviewPoints / reviewCount;
    }

    public void addMessageToChatRoom(ChatRoom chatRoom, Message message) {
        if (this.chatRooms.contains(chatRoom)) {
            chatRoom.addMessage(message);
        }
    }

    public boolean isAdmin() {
        return this instanceof Admin;
    }

    public int calculateCompatibility(User otherUser) {
        int score = 0;
        
        // Calculate based on hobbies
        if (this.profile != null && otherUser.profile != null) {
            for (Hobby hobby : this.profile.getHobbies()) {
                if (otherUser.profile.getHobbies().contains(hobby)) {
                    score += 20;
                }
            }
            
            // Calculate based on trip types
            for (TripTypes tripType : this.profile.getFavoriteTripTypes()) {
                if (otherUser.profile.getFavoriteTripTypes().contains(tripType)) {
                    score += 15;
                }
            }
        }
        
        // Age similarity
        int ageDiff = Math.abs(this.age - otherUser.age);
        if (ageDiff <= 5) score += 10;
        else if (ageDiff <= 10) score += 5;
        
        return Math.min(score, 100);
    }

    public int calculateCompatibility(City city) {
        int score = 0;
        
        if (this.profile != null) {
            int[] cityScores = city.getCompatibilityScores();
            
            // Use hobbies and trip types to calculate city compatibility
            for (Hobby hobby : this.profile.getHobbies()) {
                score += hobby.getCompatibilityScores()[0];
            }
            
            for (TripTypes tripType : this.profile.getFavoriteTripTypes()) {
                score += tripType.getCompatibilityScores()[0];
            }
        }
        
        return Math.min(score, 100);
    }

    public void approveRequest(JoinRequest request) {
        request.setStatus("APPROVED");
        Trip trip = request.getTrip();
        User requester = request.getRequester();
        trip.addMate(requester);
        requester.getCurrentTrips().add(trip);
    }

    public void denyRequest(JoinRequest request) {
        request.setStatus("DENIED");
    }

    public void joinChannel(Channel channel) {
        channel.addParticipant(this);
        channels.add(channel);
    }

    public void leaveChannel(Channel channel) {
        channel.removeParticipant(this);
        channels.remove(channel);
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public int getLevel() { return level; }
    public int getAllPoints() { return allPoints; }
    public int getMonthlyPoints() { return monthlyPoints; }
    public Profile getProfile() { return profile; }
    public ArrayList<Trip> getPastTrips() { return pastTrips; }
    public ArrayList<Trip> getCurrentTrips() { return currentTrips; }
    public ArrayList<Channel> getChannels() { return channels; }
    public ArrayList<Review> getReviews() { return reviews; }
    public int getReviewCount() { return reviewCount; }
    public int getReviewPoints() { return reviewPoints; }
    public ArrayList<JoinRequest> getJoinRequests() { return joinRequests; }
    public ArrayList<Recommendation> getRecommendations() { return recommendations; }
    public ArrayList<Message> getMessages() { return messages; }
    public ArrayList<Trip> getTripRequests() { return tripRequests; }
    public ArrayList<ChatRoom> getChatRooms() { return chatRooms; }

    // Setters

    public void setProfile(Profile profile) { this.profile = profile; }
    public void setLevel(int level) { this.level = level; }
    public void setAllPoints(int allPoints) { this.allPoints = allPoints; }
    public void setMonthlyPoints(int monthlyPoints) { this.monthlyPoints = monthlyPoints; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setReviewPoints(int reviewPoints) { this.reviewPoints = reviewPoints; }
}