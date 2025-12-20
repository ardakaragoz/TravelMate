package com.travelmate.travelmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class EditProfileController {

    @FXML private Circle profileImageCircle;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextArea bioArea;
    @FXML private TextField hobbiesField;
    @FXML private TextField tripTypesField;

    public void initialize() {
        fullNameField.setText("Berken Keni");
        usernameField.setText("@berkenkenny");
        bioArea.setText("Hi, I want to travel all around the world with new friends!");
        hobbiesField.setText("Parties, Festivals, Fine Dining");
        tripTypesField.setText("Cruise, Beach, Cultural");

        setProfileImage("user1");
    }

    @FXML
    public void handleSaveButton(ActionEvent event) {
        System.out.println("Değişiklikler kaydediliyor...");
        System.out.println("Yeni İsim: " + fullNameField.getText());
        // TODO: Veritabanı güncelleme kodu buraya gelecek

        // Kaydettikten sonra Profile sayfasına dön
        switchPage(event, "Profile");
    }

    @FXML
    public void handleCancelButton(ActionEvent event) {
        switchPage(event, "Profile");
    }
    @FXML
    public void handleChangePhoto(ActionEvent event) {
    }

    private void setProfileImage(String imageName) {
        try {
            String path = "/images/" + imageName + ".png";
            if (getClass().getResource(path) != null) {
                profileImageCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream(path))));
            }
        } catch (Exception e) { }
    }

    private void switchPage(ActionEvent event, String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlName + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}