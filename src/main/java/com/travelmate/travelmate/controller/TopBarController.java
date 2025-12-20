package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;

public class TopBarController {

    @FXML private Label userNameLabel;
    @FXML private Label userLevelLabel;
    @FXML private Circle profileImageCircle; // Corresponds to fx:id in FXML

    @FXML
    public void initialize() {
        // Fetch the logged-in user
        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            // 1. Set Name
            String displayName = currentUser.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getUsername();
            }
            userNameLabel.setText(displayName != null ? displayName : "Unknown");

            // 2. Set Level
            userLevelLabel.setText("Lvl. " + currentUser.getLevel());

            // 3. Load Profile Picture
            loadProfileImage(currentUser);

        } else {
            // Fallback for guest/testing
            userNameLabel.setText("Guest");
            userLevelLabel.setText("");
        }
    }

    private void loadProfileImage(User user) {
        if (profileImageCircle == null) return;

        // Run in background to avoid UI freezing if image loading takes time
        new Thread(() -> {
            Image imageToSet = null;
            try {
                // Check if user has a profile and a picture URL
                if (user.getProfile() != null) {
                    String url = user.getProfile().getProfilePictureUrl();
                    if (url != null && !url.isEmpty() && url.startsWith("http")) {
                        imageToSet = new Image(url, true); // true = load in background
                    }
                }

                // Fallback: Try to load default image from resources if no URL
                if (imageToSet == null) {
                    var resource = getClass().getResourceAsStream("/images/user_icon.png");
                    // If user_icon doesn't exist, try logo as fallback
                    if (resource == null) resource = getClass().getResourceAsStream("/images/logoBlue.png");

                    if (resource != null) {
                        imageToSet = new Image(resource);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Update UI on JavaFX thread
            if (imageToSet != null) {
                final Image finalImg = imageToSet;
                Platform.runLater(() -> profileImageCircle.setFill(new ImagePattern(finalImg)));
            }
        }).start();
    }

    @FXML
    public void handleHomeButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}