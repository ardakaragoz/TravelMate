package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.*;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.ChannelChat;
import com.travelmate.travelmate.model.User;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class UserList {
    public static HashMap<String, User> users = new HashMap<String, User>();
    public static void addUser(User user){
        users.put(user.getId(), user);
    }

    public static User getUser(String id){
        return users.get(id);
    }

    public static void loadAllUsers(){
        Firestore db = FirebaseService.getFirestore();
        db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            public void onEvent(QuerySnapshot snapshots, FirestoreException e) {
                if (e != null) {
                    System.err.println("UserList dinlenirken hata oluştu: " + e.getMessage());
                    return;
                }

                if (snapshots != null) {
                    // JavaFX arayüzü ile çakışmaması için güncellemeyi JavaFX Thread'ine alıyoruz
                    Platform.runLater(() -> {
                        // Listeyi temizle (Silinenler gitsin, yeniler gelsin)
                        users.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            try {
                                // User.java'daki 'public User(DocumentSnapshot doc)' constructor'ını kullanıyoruz
                                User user = new User(doc.getId(), doc);

                                // HashMap'e ekle
                                addUser(user);

                            } catch (Exception ex) {
                                System.err.println("Kullanıcı yüklenirken hata (" + doc.getId() + "): " + ex.getMessage());
                            }
                        }
                        System.out.println("UserList güncellendi! Anlık kullanıcı sayısı: " + users.size());
                    });
                }
            }
        });
    }
}
