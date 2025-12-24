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
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

public class SignInController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    @FXML
    public void handleLoginButton(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("Please enter email and password.");
                statusLabel.setTextFill(Color.RED);
                statusLabel.setVisible(true);
            }
            return;
        }
        
        if (loginButton != null) loginButton.setDisable(true);
        if (statusLabel != null) {
            statusLabel.setText("Logging in...");
            statusLabel.setTextFill(Color.BLACK);
            statusLabel.setVisible(true);
        }

        new Thread(() -> {
            try {
                Firestore db = FirebaseService.getFirestore();

                DocumentSnapshot doc = db.collection("users").document(email).get().get();

                if (doc.exists() && password.equals(doc.getString("password"))) {

                    
                    User user = new User(doc.getString("email"), doc);

                    
                    UserSession.setCurrentUser(user);

                    
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText("Login Successful! Redirecting...");
                            statusLabel.setTextFill(Color.GREEN);
                            statusLabel.setVisible(true);
                        }
                        changeScene("/view/Home.fxml", event);
                    });
                } else {
                    
                    Platform.runLater(() -> {
                        System.out.println("Login Failed: Incorrect credentials.");
                        if (statusLabel != null) {
                            statusLabel.setText("Login Failed: Incorrect credentials.");
                            statusLabel.setTextFill(Color.RED);
                            statusLabel.setVisible(true);
                        }
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