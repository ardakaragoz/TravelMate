package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.Channel;
import com.travelmate.travelmate.model.City;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ChannelList {
    public static HashMap<String, Channel> channels = new HashMap<>();

    public static void addChannel(Channel channel) {
        channels.put(channel.getName(), channel);
    }

    public static Channel getChannel(String name) {
        return channels.get(name);
    }

    public static void loadAllChannels() {
        Firestore db = FirebaseService.getFirestore();
        db.collection("channels").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                    Channel channel = null;
                    try {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        ArrayList<String> members = (ArrayList<String>) doc.get("members");
                        ArrayList<String> tripRequests = (ArrayList<String>) doc.get("tripRequests");
                        ArrayList<String> recommendations = (ArrayList<String>) doc.get("recommendations");
                        String channelChatID = doc.getString("channelChat");
                        channel = new Channel(id, name, members, tripRequests, recommendations, channelChatID);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }

                    addChannel(channel);
                }
                System.out.println("Başarılı! Toplam " + channels.size() + " kanal hafızaya alındı/güncellendi.");
                    });
                }
            }
        });
    }
}
