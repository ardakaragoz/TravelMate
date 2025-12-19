package com.travelmate.travelmate.model;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class User {
    private String id, username, name, nationality, email, password, gender;
    private int age, level, allPoints, monthlyPoints;
    private Profile profile;
    private ArrayList<String> pastTrips;
    private ArrayList<String> currentTrips;
    private ArrayList<String> channels;
    private ArrayList<String> reviews;
    private int reviewCount;
    private int reviewPoints;
    private ArrayList<String> joinRequests;
    private ArrayList<String> recommendations;
    private ArrayList<String> messages;
    private ArrayList<String> tripRequests;
    private ArrayList<String> chatRooms;
    private int levelPoint;
    private ArrayList<String> commitIDs;
    private ArrayList<LevelCommit> levelCommits;

    public User(String id, String username, String name, String nationality, String email, 
                String password, String gender, int age) throws ExecutionException, InterruptedException {
        this.id = id;
        this.profile = new Profile(id);
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
        this.recommendations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.tripRequests = new ArrayList<>();
        this.joinRequests = new ArrayList<>();
        this.chatRooms = new ArrayList<>();
        this.reviewCount = 0;
        this.reviewPoints = 0;
        this.levelPoint = 0;
        this.commitIDs = new ArrayList<>();
        this.levelCommits = new ArrayList<>();
        setCurrentUser();
        updateUser();
    }

    public User(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        setCurrentUser();
    }

    public void setCurrentUser() throws ExecutionException, InterruptedException {

        Firestore db = FirebaseService.getFirestore();

        DocumentSnapshot doc = db.collection("users")
                .document(id)
                .get()
                .get();

        if (!doc.exists()) {
            return;
        }

        id = id;
        username = doc.getString("username");
        name = doc.getString("name");
        email = doc.getString("email");
        gender = doc.getString("gender");
        nationality = doc.getString("nationality");
        profile = new Profile(id);
        age = doc.getLong("age").intValue();
        level = doc.getLong("level").intValue();
        allPoints = doc.getLong("allPoints").intValue();
        monthlyPoints = doc.getLong("monthlyPoints").intValue();
        reviewCount = doc.getLong("reviewCount").intValue();
        reviewPoints = doc.getLong("reviewPoints").intValue();
        password = doc.getString("password");

        currentTrips = (ArrayList<String>) doc.get("currentTrips");
        channels = (ArrayList<String>) doc.get("channels");
        reviews = (ArrayList<String>) doc.get("reviews");
        joinRequests = (ArrayList<String>) doc.get("joinRequests");
        recommendations = (ArrayList<String>) doc.get("recommendations");
        messages = (ArrayList<String>) doc.get("messages");
        tripRequests = (ArrayList<String>) doc.get("tripRequests");
        chatRooms = (ArrayList<String>) doc.get("chatRooms");
        pastTrips = (ArrayList<String>) doc.get("pastTrips");
        levelPoint = doc.getLong("levelPoint").intValue();
        commitIDs = (ArrayList<String>) doc.get("commitIDs");
        for (int i = 0; i < commitIDs.size(); i++) {
            levelCommits.add(new LevelCommit(commitIDs.get(i)));
        }

    }

    public void updateUser() throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("admin", isAdmin());
        data.put("age", age);
        data.put("allPoints", allPoints);
        data.put("channels", channels);
        data.put("gender", gender);
        data.put("currentTrips", currentTrips);
        data.put("pastTrips", pastTrips);
        data.put("email", email);
        data.put("level", level);
        data.put("monthlyPoints", monthlyPoints);
        data.put("name", name);
        data.put("profile", profile.getId());
        data.put("password", password);
        data.put("username", username);
        data.put("reviews", reviews);
        data.put("reviewCount", reviewCount);
        data.put("reviewPoints", reviewPoints);
        data.put("nationality", nationality);
        data.put("recommendations", recommendations);
        data.put("messages", messages);
        data.put("tripRequests", tripRequests);
        data.put("chatRooms", chatRooms);
        data.put("joinRequests", joinRequests);
        data.put("levelPoint", levelPoint);
        data.put("commitIDs", commitIDs);

        Firestore db = FirebaseService.getFirestore();
        db.collection("users")
                .document(this.id)
                .set(data)
                .get();
    }

    public void addRequest(JoinRequest req) throws ExecutionException, InterruptedException {
        this.joinRequests.add(req.getId());
        req.getTrip().addPendingMate(this);
    }

    public void increaseLevel(int point) throws ExecutionException, InterruptedException {
        Random rand = new Random();
        String levelID = "" + rand.nextInt(100000000);
        this.level += point;
        this.levelCommits.add(new LevelCommit(levelID, this, point));
        this.commitIDs.add(levelID);
        updateUser();
    }

    public void removeUserRequest(JoinRequest req, User mate) throws ExecutionException, InterruptedException {
        req.getTrip().removePendingMate(mate);
    }

    public void addRecommendation(Recommendation recommendation) throws ExecutionException, InterruptedException {
        this.recommendations.add(recommendation.getId());
        updateUser();
    }

    public void addMessage(Message message) throws ExecutionException, InterruptedException {
        this.messages.add(message.getId());
        updateUser();
    }

    public void addChatRoom(ChatRoom chatRoom) throws ExecutionException, InterruptedException {
        this.chatRooms.add(chatRoom.getId());
        updateUser();
    }

    public void addTripRequest(Trip trip) throws ExecutionException, InterruptedException {
        this.currentTrips.add(trip.getId());
        updateUser();
    }

    public void addReview(Review review) throws ExecutionException, InterruptedException {
        this.reviews.add(review.getId());
        this.reviewCount++;
        double review_points = review.getOverallPoints();
        this.reviewPoints += (int) review_points;
        if (review_points >= 4.0){
            increaseLevel((int) Math.floor(25 * (review_points - 3.8)));
        }
        updateUser();
    }

    public void sendReview(Review review) throws ExecutionException, InterruptedException {
        review.getEvaluatedUser().addReview(review);
    }

    public double getAverageReviewScore() {
        if (reviewCount == 0){
            return 0.0;
        }
        return (double) reviewPoints / reviewCount;
    }

    public void addMessageToChatRoom(ChatRoom chatRoom, Message message) throws ExecutionException, InterruptedException {
        if (this.chatRooms.contains(chatRoom.getId())) {
            chatRoom.addMessage(message);
        }
    }

    public boolean isAdmin() {
        return this instanceof Admin;
    }

    public int calculateCompatibility(User otherUser) throws ExecutionException, InterruptedException {
        int score = 0;
        
        // Calculate based on hobbies
        if (this.profile != null && otherUser.profile != null) {
            double funPoint = 0;
            double funPoint2 = 0;
            double culturePoint = 0;
            double culturePoint2 = 0;
            double chillPoint = 0;
            double chillPoint2 = 0;
            double user1Count = 0;
            double user2Count = 0;
            for (Hobby hobby : this.profile.getHobbies()) {
                if (otherUser.profile.getHobbies().contains(hobby)) {
                    score += 10;
                }
                funPoint += (hobby.getCompatibilityScores()[0]);
                culturePoint += (hobby.getCompatibilityScores()[1]);
                chillPoint += (hobby.getCompatibilityScores()[2]);
                user1Count++;
            }

            for (Hobby hobby: otherUser.profile.getHobbies()){
                funPoint2 += (hobby.getCompatibilityScores()[0]);
                culturePoint2 += (hobby.getCompatibilityScores()[1]);
                chillPoint2 += (hobby.getCompatibilityScores()[2]);
                user2Count++;
            }
            
            // Calculate based on trip types
            for (TripTypes tripType : this.profile.getFavoriteTripTypes()) {
                if (otherUser.profile.getFavoriteTripTypes().contains(tripType)) {
                    score += 8;
                }
                funPoint += (tripType.getCompatibilityScores()[0]);
                culturePoint += (tripType.getCompatibilityScores()[1]);
                chillPoint += (tripType.getCompatibilityScores()[2]);
                user1Count++;
            }

            for (TripTypes tripTypes: otherUser.profile.getFavoriteTripTypes()){
                funPoint2 += (tripTypes.getCompatibilityScores()[0]);
                culturePoint2 += (tripTypes.getCompatibilityScores()[1]);
                chillPoint2 += (tripTypes.getCompatibilityScores()[2]);
                user2Count++;
            }

            funPoint /= user1Count;
            funPoint2 /= user2Count;
            culturePoint /= user1Count;
            culturePoint2 /= user2Count;
            chillPoint /= user1Count;
            chillPoint2 /= user2Count;
            score += (int) (20 - (2 * Math.abs(funPoint - funPoint2)));
            score += (int) (20 - (2 * Math.abs(chillPoint - chillPoint2)));
            score += (int) (20 - (2 * Math.abs(culturePoint - culturePoint2)));

        }

        if (Math.abs(this.age - otherUser.age) <= 5) score += 4;
        return Math.min(score, 100);
    }

    public int calculateCompatibility(City city) throws ExecutionException, InterruptedException {
        int score = 0;
        
        if (this.profile != null) {
            int[] cityScores = city.getCompatibilityScores();
            double funPoint = 0;
            double funPoint2 = cityScores[0];
            double culturePoint = 0;
            double culturePoint2 = cityScores[1];
            double chillPoint = 0;
            double chillPoint2 = cityScores[2];
            int count = 0;

            for (Hobby hobby : this.profile.getHobbies()) {
                funPoint += (hobby.getCompatibilityScores()[0]);
                culturePoint += (hobby.getCompatibilityScores()[1]);
                chillPoint += (hobby.getCompatibilityScores()[2]);
                count++;
            }
            
            for (TripTypes tripType : this.profile.getFavoriteTripTypes()) {
                funPoint += (tripType.getCompatibilityScores()[0]);
                culturePoint += (tripType.getCompatibilityScores()[1]);
                chillPoint += (tripType.getCompatibilityScores()[2]);
                count++;
            }

            funPoint /= count;
            culturePoint /= count;
            chillPoint /= count;
            score += (int) (35 - (2 * Math.abs(funPoint - funPoint2)));
            score += (int) (35 - (2 * Math.abs(chillPoint - chillPoint2)));
            score += (int) (35 - (2 * Math.abs(culturePoint -culturePoint2)));

        }
        
        return Math.min(score, 100);
    }

    public void approveRequest(JoinRequest request) throws ExecutionException, InterruptedException {
        request.setStatus("APPROVED");
        Trip trip = request.getTrip();
        User requester = request.getRequester();
        trip.addMate(requester);
        requester.addCurrentTrip(trip.getId());
    }

    public void addCurrentTrip(String id) throws ExecutionException, InterruptedException {
        currentTrips.add(id);
        updateUser();
    }

    public void denyRequest(JoinRequest request) throws ExecutionException, InterruptedException {
        request.setStatus("DENIED");
    }

    public void joinChannel(Channel channel) throws ExecutionException, InterruptedException {
        channel.addParticipant(this);
        channels.add(channel.getId());
        updateUser();
    }

    public void leaveChannel(Channel channel) throws ExecutionException, InterruptedException {
        channel.removeParticipant(this);
        channels.remove(id);
        updateUser();
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
    public ArrayList<String> getPastTrips() { return pastTrips; }
    public ArrayList<String> getCurrentTrips() { return currentTrips; }
    public ArrayList<String> getChannels() { return channels; }
    public ArrayList<String> getReviews() { return reviews; }
    public int getReviewCount() { return reviewCount; }
    public int getReviewPoints() { return reviewPoints; }
    public ArrayList<String> getJoinRequests() { return joinRequests; }
    public ArrayList<String> getRecommendations() { return recommendations; }
    public ArrayList<String> getMessages() { return messages; }
    public ArrayList<String> getTripRequests() { return tripRequests; }
    public ArrayList<String> getChatRooms() { return chatRooms; }

    // Setters

    public void setProfile(Profile profile) { this.profile = profile; }
    public void setLevel(int level) { this.level = level; }
    public void setAllPoints(int allPoints) { this.allPoints = allPoints; }
    public void setMonthlyPoints(int monthlyPoints) { this.monthlyPoints = monthlyPoints; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setReviewPoints(int reviewPoints) { this.reviewPoints = reviewPoints; }
}