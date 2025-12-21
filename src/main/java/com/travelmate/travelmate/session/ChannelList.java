package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.Channel;
import com.travelmate.travelmate.model.City;

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
        ApiFuture<QuerySnapshot> future = db.collection("channels").get();

        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

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
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    addChannel(channel);
                }
                System.out.println("Başarılı! Toplam " + channels.size() + " kanal hafızaya alındı.");
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Şehirler yüklenirken hata oluştu: " + t.getMessage());
            }
        }, Runnable::run);

    }
}
