package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.Channel;
import com.travelmate.travelmate.model.ChannelChat;
import com.travelmate.travelmate.model.City;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ChannelChatList {
    public static HashMap<String, ChannelChat> channels = new HashMap<>();

    public static void addChannel(ChannelChat channelChat) {
        channels.put(channelChat.getId(), channelChat);
    }

    public static ChannelChat getChannel(String name) {
        return channels.get(name);
    }

    public static void loadAllChannels() {
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
                channels.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    ChannelChat channel = null;
                    try {
                        String id = doc.getId();
                        String type = doc.getString("type");
                        if (type.equals("channelChat")){
                            ArrayList<String> activeUsers = (ArrayList<String>) doc.get("activeUsers");
                            ArrayList<String> messages = (ArrayList<String>) doc.get("messages");
                            channel = new ChannelChat(id, messages, activeUsers);
                        }

                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    if (channel != null) addChannel(channel);
                }
                System.out.println("Başarılı! Toplam " + channels.size() + " channel chat hafızaya alındı.");
                    });
                }
            }
        });
    }
}