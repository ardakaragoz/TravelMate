package com.travelmate.travelmate.controller;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class EditProfileController {

    @FXML private Circle profileImageCircle;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextArea bioArea;
    @FXML private ComboBox<String> hobbyComboBox;
    @FXML private ComboBox<String> tripTypeComboBox;

    public void initialize() {
        populateComboBoxes();
        try {
            User currentUser = UserSession.getCurrentUser();
            if (currentUser != null) {
                String name = currentUser.getName() != null ? currentUser.getName() : "";

                if (fullNameField != null) fullNameField.setText(name);
                if (usernameField != null) usernameField.setText(currentUser.getUsername());
                if (bioArea != null) bioArea.setText("Hi! I am using TravelMate.");
            }
        } catch (Exception e) {
        }
    }

    private void populateComboBoxes() {
        Executors.newSingleThreadExecutor().submit(() -> {
            List<String> hobbies = new ArrayList<>();
            List<String> tripTypes = new ArrayList<>();

            try {
                Firestore db = FirebaseService.getFirestore();
                var hQuery = db.collection("hobbies").get().get();
                for (QueryDocumentSnapshot doc : hQuery.getDocuments()) hobbies.add(doc.getId());
                var tQuery = db.collection("trip_types").get().get();
                for (QueryDocumentSnapshot doc : tQuery.getDocuments()) tripTypes.add(doc.getId());

            } catch (Exception e) {
                System.out.println("⚠️ Firebase Hatası (Quota Dolmuş Olabilir): " + e.getMessage());
                System.out.println("👉 Çevrimdışı modda dummy veri kullanılıyor.");
            }
            if (hobbies.isEmpty()) {
                hobbies.add("Photography");
                hobbies.add("Hiking");
                hobbies.add("Gaming");
                hobbies.add("Cooking");
                hobbies.add("Travel");
            }
            if (tripTypes.isEmpty()) {
                tripTypes.add("Cultural");
                tripTypes.add("Nature");
                tripTypes.add("City Break");
                tripTypes.add("Adventure");
                tripTypes.add("Cruise");
            }
            List<String> finalHobbies = hobbies;
            List<String> finalTripTypes = tripTypes;

            Platform.runLater(() -> {
                if (hobbyComboBox != null) hobbyComboBox.getItems().setAll(finalHobbies);
                if (tripTypeComboBox != null) tripTypeComboBox.getItems().setAll(finalTripTypes);
            });
        });
    }

    @FXML
    public void handleSaveButton(ActionEvent event) {
        System.out.println("Kaydet'e basıldı (Firebase kapalı olsa da çalışır).");
        try {
            String newBio = bioArea.getText();
            String selectedHobby = hobbyComboBox.getValue();
            System.out.println("Kaydedilecek Veriler -> Bio: " + newBio + ", Hobi: " + selectedHobby);
        } catch (Exception e) {
            e.printStackTrace();
        }
        changeScene("/view/Profile.fxml", event);
    }

    @FXML
    public void handleCancelButton(ActionEvent event) {
        changeScene("/view/Profile.fxml", event);
    }

    private void changeScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}