package com.travelmate.travelmate.session;

import com.google.cloud.firestore.*;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatList {
    public static HashMap<String, ChatRoom> chats = new HashMap<>();
    public static HashMap<String, DirectMessage> directMessages = new HashMap<>();
    public static void addChat(ChatRoom room) {
        chats.put(room.getId(), room);
    }

    public static boolean checkDirectMessage(User user1, User user2) {
        boolean found = false;
        for (DirectMessage directMessage : directMessages.values()) {
            if (directMessage.getActiveUsers().contains(user1.getId()) && directMessage.getActiveUsers().contains(user2.getId())) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static ChatRoom getChat(String name) {
        return chats.get(name);
    }

    public static void loadAllChats() {
        Firestore db = FirebaseService.getFirestore();
        db.collection("chatrooms").addSnapshotListener(new EventListener<QuerySnapshot>() {
            public void onEvent(QuerySnapshot snapshots, FirestoreException e) {
                if (e != null) {
                    System.err.println("Channel dinlenirken hata oluştu: " + e.getMessage());
                    return;
                }

                if (snapshots != null) {
                    // JavaFX arayüzü ile çakışmaması için güncellemeyi JavaFX Thread'ine alıyoruz
                    Platform.runLater(() -> {
                chats.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    ChatRoom chat = null;
                    try {
                        String id = doc.getId();
                        String type = doc.getString("type");
                        ArrayList<String> activeUsers = (ArrayList<String>) doc.get("activeUsers");
                        ArrayList<String> messages = (ArrayList<String>) doc.get("messages");
                        if (type.equals("channelChat")){

                            chat = new ChannelChat(id, messages, activeUsers);
                        } else if (type.equals("Direct")){
                            chat = new DirectMessage(id, activeUsers, messages);
                            directMessages.put(id, (DirectMessage) chat);
                        } else {
                            chat = new TripChat(id, activeUsers, messages);
                        }

                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    if (chat != null) addChat(chat);
                }
                System.out.println("Başarılı! Toplam " + chats.size() + " chat hafızaya alındı.");
                    });
                }
            }
        });
    }
}