package com.travelmate.travelmate.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket; // Import this
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient; // Import this
import com.google.cloud.firestore.Firestore;

import java.io.InputStream;

public class FirebaseService {

    public static Firestore firestore;
    private static Bucket storageBucket; // To access storage later

    public static void initialize() {
        try {
            InputStream serviceAccount = FirebaseService.class.getResourceAsStream("/firebase/serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    // --- IMPORTANT: REPLACE WITH YOUR ACTUAL BUCKET NAME ---
                    .setStorageBucket("travelmate-XXXXX.appspot.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            firestore = FirestoreClient.getFirestore();
            // Initialize Storage Bucket
            storageBucket = StorageClient.getInstance().bucket();
            System.out.println("Firebase & Storage connected successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Firestore getFirestore(){
        return firestore;
    }

    // Helper to get the bucket
    public static Bucket getStorageBucket() {
        return storageBucket;
    }
}