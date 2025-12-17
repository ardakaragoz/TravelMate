package com.travelmate.travelmate.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.io.InputStream;

public class FirebaseService {

    public static Firestore firestore;
    public static void initialize() {
        try {
            InputStream serviceAccount =
                    FirebaseService.class.getResourceAsStream("/firebase/serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            firestore = FirestoreClient.getFirestore();
            System.out.println("Firebase connected successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Firestore getFirestore(){
        return firestore;
    }
}
