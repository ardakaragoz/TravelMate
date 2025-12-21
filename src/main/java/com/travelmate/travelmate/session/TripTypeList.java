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
import com.travelmate.travelmate.model.TripTypes;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TripTypeList {
    public static HashMap<String, TripTypes> triptypes = new HashMap<>();

    public static void addTripType(TripTypes type) {
        triptypes.put(type.getName(), type);
    }

    public static TripTypes getTripType(String name) {
        return triptypes.get(name);
    }

    public static void listAllTripTypes(){
        Firestore db = FirebaseService.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("trip_types").get();

        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                triptypes.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    TripTypes triptype = null;
                    try {
                        int[] compatabilityScores = new int[3];
                        compatabilityScores[0] = doc.getLong("funPoint").intValue();
                        compatabilityScores[1] = doc.getLong("culturePoint").intValue();
                        compatabilityScores[2] = doc.getLong("chillPoint").intValue();
                        triptype = new TripTypes(doc.getId(), doc.getId(), compatabilityScores);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    addTripType(triptype);
                }
                System.out.println("Başarılı! Toplam " + triptypes.size() + " trip type hafızaya alındı.");
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Hobiler yüklenirken hata oluştu: " + t.getMessage());
            }
        }, Runnable::run);
    }
}

