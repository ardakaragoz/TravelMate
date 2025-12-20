package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.ChannelChat;
import com.travelmate.travelmate.model.User;

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
        ApiFuture<QuerySnapshot> future = db.collection("users").get();

        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                users.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    User user = null;
                    try {
                        String id = doc.getId();
                        user = new User(id, doc);
                        System.out.println(user.getGender());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (user != null) addUser(user);
                }
                System.out.println("Başarılı! Toplam " + users.size() + " user hafızaya alındı.");
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Şehirler yüklenirken hata oluştu: " + t.getMessage());
            }
        }, Runnable::run);
    }
}
