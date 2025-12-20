package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.City;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.travelmate.travelmate.model.Hobby;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class HobbyList {
    public static HashMap<String, Hobby> hobbies = new HashMap<>();

    public static void addHobby(Hobby hobby) {
        hobbies.put(hobby.getName(), hobby);
    }

    public static Hobby getHobby(String name) {
        return hobbies.get(name);
    }

    public static void listAllHobbies(){
        Firestore db = FirebaseService.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("hobbies").get();

        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                hobbies.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Hobby hobby = null;
                    try {
                        int[] compatabilityScores = new int[3];
                        compatabilityScores[0] = doc.getLong("funPoint").intValue();
                        compatabilityScores[1] = doc.getLong("culturePoint").intValue();
                        compatabilityScores[2] = doc.getLong("chillPoint").intValue();
                        hobby = new Hobby(doc.getId(), doc.getId(), compatabilityScores);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    addHobby(hobby);
                }
                System.out.println("Başarılı! Toplam " + hobbies.size() + " hobi hafızaya alındı.");
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Hobiler yüklenirken hata oluştu: " + t.getMessage());
            }
        }, Runnable::run);
    }
}

