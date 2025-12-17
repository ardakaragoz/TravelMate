package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChatRoom {
    private String id;
    private ArrayList<String> messages;
    private ArrayList<String> activeUsers;
    private String type;

    public ChatRoom(String id, String type) throws ExecutionException, InterruptedException {
        this.id = id;
        Firestore db = FirebaseService.getFirestore();

        DocumentSnapshot doc = db.collection("chatrooms")
                .document(id)
                .get()
                .get();

        if (doc.exists()) {
            this.messages = (ArrayList<String>) doc.get("messages");
            this.activeUsers = (ArrayList<String>) doc.get("activeUsers");
            this.type = (String) doc.get("type");

        } else {
            this.type = type;
            this.messages = new ArrayList<>();
            this.activeUsers = new ArrayList<>();
            updateChatRoom();
        }

    }

    public void updateChatRoom() throws ExecutionException, InterruptedException {
        Firestore db = FirebaseService.getFirestore();
        DocumentReference docRef = db.collection("chatrooms").document(id);
        Map<String, Object>  data = new HashMap<>();
        data.put("messages", messages);
        data.put("activeUsers", activeUsers);
        data.put("type", type);
        db.collection("chatrooms").document(id).set(data).get();
    }

    public void addMessage(Message message) throws ExecutionException, InterruptedException {

        messages.add(message.getId());
        updateChatRoom();
    }

    public void addUser(User user) throws ExecutionException, InterruptedException {
        if (!activeUsers.contains(user.getId())) {
            activeUsers.add(user.getId());
            updateChatRoom();
        }
    }

    public void removeUser(User user) throws ExecutionException, InterruptedException {
        activeUsers.remove(user);
        updateChatRoom();
    }

    // Getters and Setters
    public String getId() { return id; }
    public ArrayList<String> getMessages() { return messages; }
    public ArrayList<String> getActiveUsers() { return activeUsers; }
    public String getType() { return type; }
}