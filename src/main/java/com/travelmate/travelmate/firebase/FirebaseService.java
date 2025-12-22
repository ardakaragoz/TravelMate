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
                    // --- FIXED: Used your project ID from the logs ---
                    .setStorageBucket("travelmate-demo.firebasestorage.app")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            firestore = FirestoreClient.getFirestore();

            // --- SAFE BUCKET INITIALIZATION ---
            // This prevents the app from crashing on startup if internet is down
            try {
                storageBucket = StorageClient.getInstance().bucket();
                System.out.println("Firebase Storage connected successfully.");
            } catch (Exception e) {
                System.err.println("WARNING: Could not connect to Storage. Image uploads will fail.");
                System.err.println("Reason: " + e.getMessage());
            }

            System.out.println("Firebase Firestore connected.");

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