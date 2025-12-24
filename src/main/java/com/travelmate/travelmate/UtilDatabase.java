package com.travelmate.travelmate;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UtilDatabase {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FirebaseService.initialize();
        Firestore db = FirebaseService.getFirestore();

        System.out.println("Şehirler çekiliyor, lütfen bekleyiniz...");
        
        ApiFuture<QuerySnapshot> future = db.collection("cities").get();

        QuerySnapshot snapshots = future.get();

        System.out.println("Toplam " + snapshots.size() + " şehir bulundu. İşlem başlıyor...");

        for (QueryDocumentSnapshot doc : snapshots) {
            String cityId = doc.getId();

            Map<String, Object> data = new HashMap<>();
            data.put("name", cityId); 
            data.put("members", new ArrayList<String>());
            data.put("tripRequests", new ArrayList<String>());
            data.put("recommendations", new ArrayList<String>());

            
            data.put("channelChat", cityId);

            
            ApiFuture<WriteResult> writeFuture = db.collection("channels").document(cityId).set(data);
            writeFuture.get();

            System.out.println("Channel oluşturuldu: " + cityId);
        }

        System.out.println("Tüm işlemler başarıyla tamamlandı!");
    }
}