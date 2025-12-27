package com.travelmate.travelmate;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.ChannelChat;
import com.travelmate.travelmate.session.CityList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UtilDatabase {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FirebaseService.initialize();
        Firestore db = FirebaseService.getFirestore();
        System.out.println("Creating initial values!");
        if (CityList.cities.isEmpty()) {
            CityList.loadAllCities();
        }
        for (String cityName : CityList.cities.keySet()) {

            String fixedCityName = cityName;

            DocumentReference channelRef = db.collection("channels").document(fixedCityName);

            try {
                DocumentSnapshot doc = channelRef.get().get();

                if (!doc.exists()) {
                    System.out.println(fixedCityName + " is emerging!");

                    String chatID = fixedCityName;
                    HashMap<String, Object> channelchatdata = new HashMap<>();
                    channelchatdata.put("activeUsers", new ArrayList<String>());
                    channelchatdata.put("messages", new ArrayList<String>());
                    channelchatdata.put("type", "channelChat");

                    db.collection("chatrooms").document(chatID).set(channelchatdata).get();
                    HashMap<String, Object> channeldata = new HashMap<>();
                    channeldata.put("channelChat", chatID);
                    channeldata.put("members", new ArrayList<String>());
                    channeldata.put("name", chatID);
                    channeldata.put("recommendations", new ArrayList<String>());
                    channeldata.put("tripRequests", new ArrayList<String>());
                    channelRef.set(channeldata).get();

                    System.out.println("✅ " + fixedCityName + " channel and chat created");
                }

            } catch (InterruptedException | ExecutionException e) {
                System.err.println("ERROR: (" + fixedCityName + "): " + e.getMessage());
            }
        }
        System.out.println("--- COMPLETED ---");
    }
}