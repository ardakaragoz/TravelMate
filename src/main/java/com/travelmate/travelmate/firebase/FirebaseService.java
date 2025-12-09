package com.travelmate.travelmate.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.InputStream;

public class FirebaseService {

    public static void initialize() {
        try {
            // resources içindeki JSON dosyasını okur
            InputStream serviceAccount =
                    FirebaseService.class.getResourceAsStream("/firebase/serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase connected successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
