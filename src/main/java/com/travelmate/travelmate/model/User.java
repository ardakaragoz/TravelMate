package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.ChannelList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class User {
    private String id, username, name, nationality, email, password, gender;
    private int age, level, allPoints, monthlyPoints;
    private Profile profile;

    // Initialize lists immediately to avoid NullPointerException
    private ArrayList<String> pastTrips = new ArrayList<>();
    private ArrayList<String> currentTrips = new ArrayList<>();
    private ArrayList<String> channels = new ArrayList<>();
    private ArrayList<String> reviews = new ArrayList<>();
    private ArrayList<String> joinRequests = new ArrayList<>();
    private ArrayList<String> recommendations = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<String> tripRequests = new ArrayList<>();
    private ArrayList<String> chatRooms = new ArrayList<>();
    private ArrayList<String> commitIDs = new ArrayList<>();
    private ArrayList<LevelCommit> levelCommits = new ArrayList<>();

    private int reviewCount;
    private int reviewPoints;
    private int levelPoint;

    // --- CRITICAL FIX: Empty Constructor for Firestore ---
    public User() {
        // Firestore needs this to create the object before filling fields
    }

    // --- Constructor 1: Create New User (Registration) ---
    public User(String id, String username, String name, String nationality, String email,
                String password, String gender, int age) throws ExecutionException, InterruptedException {
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
        this.reviewCount = 0;
        this.reviewPoints = 0;
        this.levelPoint = 0;

        // For new users, create profile immediately (one-time cost)
        this.profile = new Profile(id);

        updateUser();
    }

    // --- Constructor 2: Optimized Loader (Login) ---
    // This receives the data directly from SignInController
    public User(String id, DocumentSnapshot doc) {
        this.id = id;
        // Load data directly from the doc we already have!
        loadFromDoc(doc);
    }

    // --- Constructor 3: Legacy Loader (Slow) ---
    public User(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        setCurrentUser();
    }

    // Helper to parse data (Used by both Optimized and Legacy loaders)
    private void loadFromDoc(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        this.username = doc.getString("username");
        this.name = doc.getString("name");
        this.email = doc.getString("email");
        this.gender = doc.getString("gender");
        this.nationality = doc.getString("nationality");
        this.password = doc.getString("password");

        // Safe unboxing for numbers
        if (doc.getLong("age") != null) this.age = doc.getLong("age").intValue();
        if (doc.getLong("level") != null) this.level = doc.getLong("level").intValue();
        if (doc.getLong("allPoints") != null) this.allPoints = doc.getLong("allPoints").intValue();
        if (doc.getLong("monthlyPoints") != null) this.monthlyPoints = doc.getLong("monthlyPoints").intValue();
        if (doc.getLong("reviewCount") != null) this.reviewCount = doc.getLong("reviewCount").intValue();
        if (doc.getLong("reviewPoints") != null) this.reviewPoints = doc.getLong("reviewPoints").intValue();
        if (doc.getLong("levelPoint") != null) this.levelPoint = doc.getLong("levelPoint").intValue();

        // Load Lists Safely
        if (doc.get("currentTrips") != null) this.currentTrips = (ArrayList<String>) doc.get("currentTrips");
        if (doc.get("channels") != null) this.channels = (ArrayList<String>) doc.get("channels");
        if (doc.get("reviews") != null) this.reviews = (ArrayList<String>) doc.get("reviews");
        if (doc.get("joinRequests") != null) this.joinRequests = (ArrayList<String>) doc.get("joinRequests");
        if (doc.get("recommendations") != null) this.recommendations = (ArrayList<String>) doc.get("recommendations");
        if (doc.get("messages") != null) this.messages = (ArrayList<String>) doc.get("messages");
        if (doc.get("tripRequests") != null) this.tripRequests = (ArrayList<String>) doc.get("tripRequests");
        if (doc.get("chatRooms") != null) this.chatRooms = (ArrayList<String>) doc.get("chatRooms");
        if (doc.get("pastTrips") != null) this.pastTrips = (ArrayList<String>) doc.get("pastTrips");
        if (doc.get("commitIDs") != null) this.commitIDs = (ArrayList<String>) doc.get("commitIDs");

        // --- SPEED FIX: PROFILE LOADING REMOVED ---
        // We do NOT load 'new Profile(id)' here because it freezes the login.
        // It will be loaded lazily in getProfile() or by specific controllers.
    }

    public void setCurrentUser() throws ExecutionException, InterruptedException {
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot doc = db.collection("users").document(id).get().get(); // Still blocks, but used less often
        loadFromDoc(doc);
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
        data.put("levelPoint", levelPoint);
        data.put("commitIDs", commitIDs);

        Firestore db = FirebaseService.getFirestore();
        // Optimization: removed .get() to avoid blocking if just saving
        db.collection("users").document(this.id).set(data);
    }

    // --- LAZY LOADING PROFILE GETTER ---
    public Profile getProfile() {
        if (this.profile == null) {
            try {
                // If the profile is requested and null, fetch it now.
                // This might cause a slight delay when opening the Profile page,
                // but it keeps the Login page instant.
                this.profile = new Profile(this.id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.profile;
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
        if (!this.chatRooms.contains(chatRoom.getId())){
            this.chatRooms.add(chatRoom.getId());
            updateUser();
        }
    }

    public void addTripRequest(Trip trip) throws ExecutionException, InterruptedException {
        this.currentTrips.add(trip.getId());
        ChannelList.getChannel(trip.getDestinationName()).addTripRequest(trip);
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
        if (reviewCount == 0) return 0.0;
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

        // Ensure profiles are loaded before calculation
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
    public int getLevel() { return level; }
    public int getAllPoints() { return allPoints; }
    public int getMonthlyPoints() { return monthlyPoints; }
    // Note: getProfile is defined above as a lazy loader
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

    public void setProfile(Profile profile) { this.profile = profile; }
    public void setLevel(int level) { this.level = level; }
    public void setAllPoints(int allPoints) { this.allPoints = allPoints; }
    public void setMonthlyPoints(int monthlyPoints) { this.monthlyPoints = monthlyPoints; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setReviewPoints(int reviewPoints) { this.reviewPoints = reviewPoints; }
}