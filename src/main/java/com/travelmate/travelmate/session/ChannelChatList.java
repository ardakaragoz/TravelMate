package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.Channel;
import com.travelmate.travelmate.model.ChannelChat;
import com.travelmate.travelmate.model.City;

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
        ApiFuture<QuerySnapshot> future = db.collection("chatrooms").get();

        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

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

                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (channel != null) addChannel(channel);
                }
                System.out.println("Başarılı! Toplam " + channels.size() + " channel chat hafızaya alındı.");
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Şehirler yüklenirken hata oluştu: " + t.getMessage());
            }
        }, Runnable::run);

    }
}
