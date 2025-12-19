package com.travelmate.travelmate.controller;

import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField ageField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    //emailField
    //passwordField
    //confirmPasswordField
    @FXML private ComboBox<String> genderComboBox;
    public void initialize() {
        if (genderComboBox != null) {
            genderComboBox.getItems().addAll("Male", "Female", "Prefer not to say");
        }
    }
    @FXML
    public void handleRegisterButton(ActionEvent event) throws ExecutionException, InterruptedException {
        // registration code probably
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String name = nameField.getText();
        String surname = surnameField.getText();
        String age = ageField.getText();
        String gender = genderComboBox.getValue();
        System.out.println(password);
        if (password.equals(confirmPassword)) {
            Firestore db = FirebaseService.getFirestore();
            User user = new User(email, name, name + " " + surname, "", email, password, gender, Integer.parseInt(age));
            UserSession.setCurrentUser(user);
            changeScene("/view/Home.fxml", event);
        } else {
            System.out.println("Register failed.");
        }
    }

    @FXML
    public void handleBackButton(ActionEvent event) {
        changeScene("/view/sign-in-page.fxml", event);
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