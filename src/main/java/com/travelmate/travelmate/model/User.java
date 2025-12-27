package com.travelmate.travelmate.model;
//27.12.2025 Small note: loadFromDoc gets exact data from firebase and integrate in into instances directly.
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.ChannelList;
import com.travelmate.travelmate.session.UserList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class User {
    private String id, username, name, nationality, email, password, gender;
    private int age, levelPoint, monthlyPoints;
    private Profile profile;
    private String profilePictureUrl;

    private ArrayList<String> trips = new ArrayList<>();
    private ArrayList<String> channels = new ArrayList<>();
    private ArrayList<String> reviews = new ArrayList<>();
    private ArrayList<String> joinRequests = new ArrayList<>();
    private ArrayList<String> recommendations = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<String> tripRequests = new ArrayList<>();
    private ArrayList<String> chatRooms = new ArrayList<>();
    private boolean admin;
    private int reviewCount;
    private double reviewPoints;

    public User() {
    }

    public User(String id, String username, String name, String nationality, String email,
                String password, String gender, int age, boolean admin) throws ExecutionException, InterruptedException {
        this.id = id;
        this.username = username;
        this.name = name;
        this.nationality = nationality;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.age = age;
        this.levelPoint = 0;
        this.monthlyPoints = 0;
        this.reviewCount = 0;
        this.reviewPoints = 0;
        this.admin = admin;
        this.profile = new Profile(id);

        updateUser();
    }

    public User(String id, DocumentSnapshot doc) {
        this.id = id;
        loadFromDoc(doc);
    }

    public User(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        setCurrentUser();
    }

    private void loadFromDoc(DocumentSnapshot doc) {
        if (!doc.exists()) return;
        this.username = doc.getString("username");
        this.name = doc.getString("name");
        this.email = doc.getString("email");
        this.gender = doc.getString("gender");
        this.nationality = doc.getString("nationality");
        this.password = doc.getString("password");
        this.admin = Boolean.TRUE.equals(doc.getBoolean("admin"));
        if (doc.getLong("age") != null) this.age = doc.getLong("age").intValue();
        if (doc.getLong("levelPoint") != null) this.levelPoint = doc.getLong("levelPoint").intValue();
        if (doc.getLong("monthlyPoints") != null) this.monthlyPoints = doc.getLong("monthlyPoints").intValue();
        if (doc.getLong("reviewCount") != null) this.reviewCount = doc.getLong("reviewCount").intValue();
        if (doc.getLong("reviewPoints") != null) this.reviewPoints = doc.getLong("reviewPoints").doubleValue();

        if (doc.get("trips") != null) this.trips = (ArrayList<String>) doc.get("trips");
        if (doc.get("channels") != null) this.channels = (ArrayList<String>) doc.get("channels");
        if (doc.get("reviews") != null) this.reviews = (ArrayList<String>) doc.get("reviews");
        if (doc.get("joinRequests") != null) this.joinRequests = (ArrayList<String>) doc.get("joinRequests");
        if (doc.get("recommendations") != null) this.recommendations = (ArrayList<String>) doc.get("recommendations");
        if (doc.get("messages") != null) this.messages = (ArrayList<String>) doc.get("messages");
        if (doc.get("tripRequests") != null) this.tripRequests = (ArrayList<String>) doc.get("tripRequests");
        if (doc.get("chatRooms") != null) this.chatRooms = (ArrayList<String>) doc.get("chatRooms");
    }

    public void setCurrentUser() throws ExecutionException, InterruptedException {
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot doc = db.collection("users").document(id).get().get();
        loadFromDoc(doc);
    }

    public void updateBasicInfo() {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> data = new HashMap<>();
            data.put("name", this.name);
            data.put("username", this.username);
            FirebaseService.getFirestore().collection("users").document(this.id).update(data);
        });
    }

    public void updateUser() {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> data = new HashMap<>();
            data.put("admin", isAdmin());
            data.put("age", age);
            data.put("levelPoint", levelPoint);
            data.put("channels", channels);
            data.put("gender", gender);
            data.put("trips", trips);
            data.put("email", email);
            data.put("monthlyPoints", monthlyPoints);
            data.put("name", name);
            if (profile != null) data.put("profile", profile.getId());
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

            FirebaseService.getFirestore().collection("users").document(this.id).set(data);
        });
    }

    public Profile getProfile() {
        if (this.profile == null) {
            try { this.profile = new Profile(this.id); } catch (Exception e) { e.printStackTrace(); }
        }
        return this.profile;
    }

    public void addRequest(JoinRequest req) throws ExecutionException, InterruptedException {
        this.joinRequests.add(req.getId());
        updateUser();
        req.getTrip().addPendingMate(this);
    }

    public void increaseLevel(int point) throws ExecutionException, InterruptedException {
        this.levelPoint += point;
        updateUser();
    }

    public void removeUserRequest(JoinRequest req, User mate) throws ExecutionException, InterruptedException {
        req.getTrip().removePendingMate(mate);
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!this.chatRooms.contains(chatRoom.getId())){
            this.chatRooms.add(chatRoom.getId());
            updateUser();
        }
    }

    public void addTripRequest(Trip trip) throws ExecutionException, InterruptedException {
        this.trips.add(trip.getId());
        ChannelList.getChannel(trip.getDestinationName()).addTripRequest(trip);
        updateUser();
    }

    public void addReview(Review review) throws ExecutionException, InterruptedException {
        this.reviews.add(review.getId());
        this.reviewCount++;
        double review_points = review.getOverallPoints();
        this.reviewPoints += review_points;
        if (review_points >= 4.0){
            increaseLevel((int) Math.floor(25 * (review_points - 3.4)));
        }
        updateUser();
    }

    public void sendReview(Review review) throws ExecutionException, InterruptedException {
        increaseLevel(15);
        review.getEvaluatedUser().addReview(review);
    }

    public double getAverageReviewScore() {
        if (reviewCount == 0) return 0.0;
        return (double) reviewPoints / reviewCount;
    }

    public void addMessageToChatRoom(ChatRoom chatRoom, Message message) throws ExecutionException, InterruptedException {
        if (this.chatRooms.contains(chatRoom.getId())) {
            chatRoom.addMessage(message);
        }
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public int calculateCompatibility(User otherUser) throws ExecutionException, InterruptedException {
        int score = 0;

        if (this.getProfile() != null && otherUser.getProfile() != null) {
            double funPoint = 0;
            double funPoint2 = 0;
            double culturePoint = 0;
            double culturePoint2 = 0;
            double chillPoint = 0;
            double chillPoint2 = 0;
            double user1Count = 0;
            double user2Count = 0;

            for (Hobby hobby : this.profile.getHobbies()) {
                if (otherUser.profile.getHobbies().contains(hobby)) score += 10;
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

            for (TripTypes tripType : this.profile.getFavoriteTripTypes()) {
                if (otherUser.profile.getFavoriteTripTypes().contains(tripType)) score += 8;
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

            if (user1Count > 0) { funPoint /= user1Count; culturePoint /= user1Count; chillPoint /= user1Count; }
            if (user2Count > 0) { funPoint2 /= user2Count; culturePoint2 /= user2Count; chillPoint2 /= user2Count; }

            score += (int) (20 - (2 * Math.abs(funPoint - funPoint2)));
            score += (int) (20 - (2 * Math.abs(chillPoint - chillPoint2)));
            score += (int) (20 - (2 * Math.abs(culturePoint - culturePoint2)));
        }

        if (Math.abs(this.age - otherUser.age) <= 5) score += 4;
        return Math.min(score, 100);
    }

    public int calculateCompatibility(City city) throws ExecutionException, InterruptedException {
        int score = 0;

        if (this.getProfile() != null) {
            int[] cityScores = city.getCompatibilityScores();
            double funPoint = 0, funPoint2 = cityScores[0];
            double culturePoint = 0, culturePoint2 = cityScores[1];
            double chillPoint = 0, chillPoint2 = cityScores[2];
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

            if(count > 0) { funPoint /= count; culturePoint /= count; chillPoint /= count; }

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
        trips.add(id);
        increaseLevel(20);
    }

    public void denyRequest(JoinRequest request) throws ExecutionException, InterruptedException {
        request.setStatus("DENIED");
    }

    public void joinChannel(Channel channel) throws ExecutionException, InterruptedException {
        channels.add(channel.getId());
        CompletableFuture.runAsync(() -> {
            FirebaseService.getFirestore().collection("users").document(this.id)
                    .update("channels", FieldValue.arrayUnion(channel.getId()));
        });
    }

    public void leaveChannel(Channel channel) throws ExecutionException, InterruptedException {
        channels.remove(channel.getId());
        CompletableFuture.runAsync(() -> {
            FirebaseService.getFirestore().collection("users").document(this.id)
                    .update("channels", FieldValue.arrayRemove(channel.getId()));
        });
    }

    public String getProfilePicture() {
        return profilePictureUrl;
    }

    public void setProfilePicture(String url) {
        this.profilePictureUrl = url;
        updateUser();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public int getLevel() { return  1 + levelPoint / 10; }
    public int getLevelPoint() { return levelPoint; }
    public int getMonthlyPoints() { return monthlyPoints; }
    public ArrayList<String> getTrips() { return trips; }
    public ArrayList<String> getChannels() { return channels; }
    public ArrayList<String> getReviews() { return reviews; }
    public int getReviewCount() { return reviewCount; }
    public double getReviewPoints() { return reviewPoints; }
    public ArrayList<String> getJoinRequests() { return joinRequests; }
    public ArrayList<String> getRecommendations() { return recommendations; }
    public ArrayList<String> getMessages() { return messages; }
    public ArrayList<String> getTripRequests() { return tripRequests; }
    public ArrayList<String> getChatRooms() { return chatRooms; }

    public void setProfile(Profile profile) { this.profile = profile; }
    public void setLevelPoint(int allPoints) { this.levelPoint = allPoints; }
    public void setMonthlyPoints(int monthlyPoints) { this.monthlyPoints = monthlyPoints; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setReviewPoints(double reviewPoints) { this.reviewPoints = reviewPoints; }
}