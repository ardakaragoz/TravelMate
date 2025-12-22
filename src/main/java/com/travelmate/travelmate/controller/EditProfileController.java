package com.travelmate.travelmate.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import com.travelmate.travelmate.utils.ImageUploader; // Ensure this class exists
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class EditProfileController {

    @FXML private Circle profileImageCircle;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextArea bioArea;
    @FXML private ComboBox<String> hobbyComboBox;
    @FXML private ComboBox<String> tripTypeComboBox;

    private File selectedImageFile; // Store the uploaded photo temporarily
    private User currentUser;

    public void initialize() {
        populateComboBoxes();

        try {
            currentUser = UserSession.getCurrentUser();
            if (currentUser != null) {
                // 1. Load Text Data
                if (fullNameField != null) fullNameField.setText(currentUser.getName());
                if (usernameField != null) usernameField.setText(currentUser.getUsername());

                if (currentUser.getProfile() != null) {
                    if (bioArea != null) bioArea.setText(currentUser.getProfile().getBiography());

                    // 2. Load Profile Picture
                    String cloudUrl = currentUser.getProfile().getProfilePictureUrl();
                    loadProfileImage(cloudUrl, currentUser.getUsername());
                } else {
                    if (bioArea != null) bioArea.setText("Hi! I am using TravelMate.");
                    loadProfileImage(null, currentUser.getUsername());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfileImage(String cloudUrl, String username) {
        if (profileImageCircle == null) return;

        Platform.runLater(() -> {
            try {
                if (cloudUrl != null && !cloudUrl.isEmpty() && cloudUrl.startsWith("http")) {
                    // Load from Cloud
                    profileImageCircle.setFill(new ImagePattern(new Image(cloudUrl, true)));
                } else {
                    // Load Local Fallback
                    String path = "/images/" + username.toLowerCase() + ".png";
                    if (getClass().getResource(path) != null) {
                        profileImageCircle.setFill(new ImagePattern(new Image(getClass().getResource(path).toExternalForm())));
                    } else {
                        profileImageCircle.setFill(new ImagePattern(new Image(getClass().getResource("/images/user_icon.png").toExternalForm())));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        });
    }

    // --- PHOTO UPLOAD BUTTON ---
    @FXML
    public void handleUploadPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            this.selectedImageFile = file;
            // Show preview immediately (local file)
            profileImageCircle.setFill(new ImagePattern(new Image(file.toURI().toString())));
        }
    }

    private void populateComboBoxes() {
        Executors.newSingleThreadExecutor().submit(() -> {
            List<String> hobbies = new ArrayList<>();
            List<String> tripTypes = new ArrayList<>();

            try {
                Firestore db = FirebaseService.getFirestore();
                if (db != null) {
                    var hQuery = db.collection("hobbies").get().get();
                    for (QueryDocumentSnapshot doc : hQuery.getDocuments()) hobbies.add(doc.getId());
                    var tQuery = db.collection("trip_types").get().get();
                    for (QueryDocumentSnapshot doc : tQuery.getDocuments()) tripTypes.add(doc.getId());
                }
            } catch (Exception e) {
                System.out.println("⚠️ Firebase Warning: " + e.getMessage());
            }

            // Dummy Data Fallback
            if (hobbies.isEmpty()) {
                hobbies.add("Photography"); hobbies.add("Hiking"); hobbies.add("Gaming");
            }
            if (tripTypes.isEmpty()) {
                tripTypes.add("Cultural"); tripTypes.add("Nature"); tripTypes.add("Adventure");
            }

            final List<String> finalHobbies = hobbies;
            final List<String> finalTripTypes = tripTypes;

            Platform.runLater(() -> {
                if (hobbyComboBox != null) hobbyComboBox.getItems().setAll(finalHobbies);
                if (tripTypeComboBox != null) tripTypeComboBox.getItems().setAll(finalTripTypes);
            });
        });
    }

    @FXML
    public void handleSaveButton(ActionEvent event) {
        if (currentUser == null) return;
        System.out.println("Saving profile...");

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Upload Image (If selected)
                if (selectedImageFile != null) {
                    String cloudUrl = ImageUploader.uploadProfilePicture(selectedImageFile, currentUser.getUsername());
                    if (cloudUrl != null) {
                        // SAVE TO PROFILE (Now safe)
                        currentUser.getProfile().setProfilePicture(cloudUrl);
                    }
                }

                // 2. Save Text Data
                if (bioArea != null) {
                    currentUser.getProfile().setBiography(bioArea.getText());
                }

                // 3. Close & Refresh
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Profile Updated!");
                    alert.showAndWait();
                    changeScene("/view/Profile.fxml", event);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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