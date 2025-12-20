package com.travelmate.travelmate.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.effect.GaussianBlur;
import javafx.event.ActionEvent;
import java.io.IOException;

public class ProfileController {

    @FXML private BorderPane mainContainer;
    @FXML private Circle profileImageCircle;
    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label levelLabel;
    @FXML private ProgressBar levelProgressBar; // YENİ EKLENDİ
    @FXML private Label reviewScoreLabel;
    @FXML private Label bioLabel;
    @FXML private FlowPane hobbiesContainer;
    @FXML private FlowPane tripTypesContainer;
    @FXML private Label pastTripsLabel;

    // Help Popup
    @FXML private VBox helpPopup;

    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        fullNameLabel.setText("Berken Keni");
        usernameLabel.setText("@berkenkenny, 20");
        levelLabel.setText("Level 35");

        // Level Barı Ayarla (%75 dolu)
        if (levelProgressBar != null) {
            levelProgressBar.setProgress(0.75);
        }

        reviewScoreLabel.setText("Review Score: (9)");
        bioLabel.setText("Hi, I want to travel all around the world with new friends!");
        pastTripsLabel.setText("Budapest, Rome, Maldives, New York");
        setProfileImage("user1");

        addTag(hobbiesContainer, "Parties", "#CCFF00");
        addTag(hobbiesContainer, "Festivals", "#CCFF00");
        addTag(hobbiesContainer, "Fine Dining", "#CCFF00");

        addTag(tripTypesContainer, "Cruise", "#a4c2f2");
        addTag(tripTypesContainer, "Beach", "#a4c2f2");
        addTag(tripTypesContainer, "Cultural", "#a4c2f2");
    }

    // --- POPUP İŞLEMLERİ ---
    @FXML
    public void handleHelpButton() {
        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        if (helpPopup != null) helpPopup.setVisible(true);
    }

    @FXML
    public void closeHelpPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (helpPopup != null) helpPopup.setVisible(false);
    }

    @FXML
    public void handleEditProfileButton(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/EditProfile.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTag(FlowPane container, String text, String colorHex) {
        Label tag = new Label(text);
        tag.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 15; -fx-padding: 5 15 5 15; -fx-border-color: black; -fx-border-radius: 15;");
        tag.setFont(Font.font("System", FontWeight.BOLD, 14));
        container.getChildren().add(tag);
    }

    private void setProfileImage(String imageName) {
        try {
            String path = "/images/" + imageName + ".png";
            if (getClass().getResource(path) != null) {
                Image img = new Image(getClass().getResourceAsStream(path));
                profileImageCircle.setFill(new ImagePattern(img));
            }
        } catch (Exception e) {}
    }
}