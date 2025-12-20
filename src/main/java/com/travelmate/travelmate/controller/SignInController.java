package com.travelmate.travelmate.controller;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SignInController {
    @FXML private Label statusLabel;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    // email textField id: emailField
    // password textField id: passwordField
    @FXML
    public void handleLoginButton(ActionEvent event) {
        if (!emailField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            statusLabel.setText("Login Successful! Redirecting...");
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setVisible(true);
        } else {
            statusLabel.setText("Login Failed! Please check credentials.");
            statusLabel.setTextFill(Color.RED);
            statusLabel.setVisible(true);
        }
    }
    public User checkLogin() throws ExecutionException, InterruptedException {
        String email = emailField.getText();
        String password = passwordField.getText();
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot data = db.collection("users").document(email).get().get();
        if (data.exists()){
            if (!(password.equals(data.get("password").toString()))){
                return null;
            } else {
                return new User(data.getString("email"));
            }
        } else {
            return null;
        }
    }

    @FXML
    public void handleSignUpLink(ActionEvent event) {
        changeScene("/view/Home.fxml", event);
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