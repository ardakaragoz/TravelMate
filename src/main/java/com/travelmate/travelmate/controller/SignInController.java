package com.travelmate.travelmate.controller;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

public class SignInController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton; // Ensure your FXML button has fx:id="loginButton"

    @FXML
    public void handleLoginButton(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Please enter email and password.");
            return;
        }

        // Disable button to prevent double-clicking while loading
        if (loginButton != null) loginButton.setDisable(true);
        System.out.println("Logging in...");

        // --- OPTIMIZATION: Run Database Logic in Background Thread ---
        new Thread(() -> {
            try {
                Firestore db = FirebaseService.getFirestore();

                // 1. Fetch the user document (The ONLY network call needed)
                DocumentSnapshot doc = db.collection("users").document(email).get().get();

                // 2. Verify Credentials
                if (doc.exists() && password.equals(doc.getString("password"))) {

                    // 3. CRITICAL OPTIMIZATION:
                    // Pass the 'doc' we just downloaded into the User constructor.
                    // This prevents the User class from re-downloading the same data.
                    User user = new User(doc.getString("email"), doc);

                    // 4. Update Session
                    UserSession.setCurrentUser(user);

                    // 5. Switch Scene (Must be on UI Thread)
                    Platform.runLater(() -> changeScene("/view/Home.fxml", event));
                } else {
                    // Login Failed
                    Platform.runLater(() -> {
                        System.out.println("Login Failed: Incorrect credentials.");
                        if (loginButton != null) loginButton.setDisable(false);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.out.println("Login Error: " + e.getMessage());
                    if (loginButton != null) loginButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    public void handleSignUpLink(ActionEvent event) {
        changeScene("/view/register-page.fxml", event);
    }

    private void changeScene(String fileName, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fileName));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}