package com.travelmate.travelmate.session;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.City;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class CityList {
    public static HashMap<String, City> cities = new HashMap<>();

    public static void addCity(City city) {
        cities.put(city.getName(), city);
    }

    public static City getCity(String name) {
        return cities.get(name);
    }

    public static void listAllCities(){
        Firestore db = FirebaseService.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("cities").get();

        ApiFutures.addCallback(future, new ApiFutureCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                cities.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    City city = null;
                    try {
                        int[] compatabilityScores = new int[3];
                        compatabilityScores[0] = doc.getLong("funPoint").intValue();
                        compatabilityScores[1] = doc.getLong("culturePoint").intValue();
                        compatabilityScores[2] = doc.getLong("chillPoint").intValue();
                        city = new City(doc.getId(), doc.getId(), doc.getId(), compatabilityScores);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    addCity(city);
                }
                System.out.println("Başarılı! Toplam " + cities.size() + " şehir hafızaya alındı.");
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Şehirler yüklenirken hata oluştu: " + t.getMessage());
            }
        }, Runnable::run);
    }
    }

