package com.travelmate.travelmate.session;

import com.google.cloud.firestore.*;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;

public class RecommendationList {
    public static HashMap<String, Recommendation> recommendations = new HashMap<>();

    public static HashMap<String, Recommendation> getRecommendations() {
        return recommendations;
    }

    public static Recommendation getRecommendation(String id) {
        return recommendations.get(id);
    }

    public static void addRecommendation(Recommendation recommendation) {
        recommendations.put(recommendation.getId(), recommendation);
    }

    public static void loadRecommendations(){
        Firestore db = FirebaseService.getFirestore();
        db.collection("recommendations").addSnapshotListener(new EventListener<QuerySnapshot>() {
            public void onEvent(QuerySnapshot snapshots, FirestoreException e) {
                if (e != null) {
                    System.err.println("Reco dinlenirken hata oluştu: " + e.getMessage());
                    return;
                }

                if (snapshots != null) {
                    Platform.runLater(() -> {
                        recommendations.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Recommendation reco = null;
                            try {
                                String id = doc.getId();
                                String channel = doc.getString("channel");
                                long createdAt = doc.getLong("createdAt");
                                String link = doc.getString("link");
                                String message = doc.getString("message");
                                String sender = doc.getString("sender");
                                String status = doc.getString("status");
                                reco = new Recommendation(id, message, sender, channel, link, status, createdAt);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                            if (reco != null) addRecommendation(reco);
                        }
                        System.out.println("Başarılı! Toplam " + recommendations.size() + " recommendation hafızaya alındı.");
                    });
                }
            }
        });
    }
}
