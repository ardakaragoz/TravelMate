package com.travelmate.travelmate.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;

import java.io.InputStream;

public class FirebaseService {

    public static Firestore firestore;
    private static Bucket storageBucket;

    public static void initialize() {
        try {
            InputStream serviceAccount = FirebaseService.class.getResourceAsStream("/firebase/serviceAccountKey.json");

            if (serviceAccount == null) {
                System.err.println("CRITICAL: serviceAccountKey.json not found!");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("travelmate-demo.firebasestorage.app")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            firestore = FirestoreClient.getFirestore();

            try {
                storageBucket = StorageClient.getInstance().bucket();
                if (storageBucket != null) {
                    System.out.println("✅ Storage Connected: " + storageBucket.getName());
                } else {
                    System.err.println("❌ Storage Client returned null bucket.");
                }
            } catch (Exception e) {
                System.err.println("❌ Storage Connection Failed: " + e.getMessage());
            }

            System.out.println("Firebase initialized.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Firestore getFirestore() {
        return firestore;
    }

    public static Bucket getStorageBucket() {
        return storageBucket;
    }
}