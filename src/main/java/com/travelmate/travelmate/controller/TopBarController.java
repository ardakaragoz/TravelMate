package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;

public class TopBarController {

    @FXML private Label userNameLabel;
    @FXML private Label userLevelLabel;
    @FXML private Circle profileImageCircle;

    @FXML
    public void initialize() {
        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            String displayName = currentUser.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getUsername();
            }
            userNameLabel.setText(displayName != null ? displayName : "Unknown");
            userLevelLabel.setText("Lvl. " + currentUser.getLevel());
            loadProfileImage(currentUser);
        } else {
            userNameLabel.setText("Guest");
            userLevelLabel.setText("");
        }
    }

    private void loadProfileImage(User user) {
        if (profileImageCircle == null) return;

        new Thread(() -> {
            Image imageToSet = null;
            try {
                if (user.getProfile() != null) {
                    String url = user.getProfile().getProfilePictureUrl();
                    if (url != null && !url.isEmpty() && url.startsWith("http")) {
                        imageToSet = new Image(url, true);
                    }
                }
                if (imageToSet == null) {
                    var resource = getClass().getResourceAsStream("/images/user_icon.png");
                    if (resource == null) resource = getClass().getResourceAsStream("/images/logoBlue.png");
                    if (resource != null) imageToSet = new Image(resource);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (imageToSet != null) {
                final Image finalImg = imageToSet;
                Platform.runLater(() -> profileImageCircle.setFill(new ImagePattern(finalImg)));
            }
        }).start();
    }

    public void handleProfileClick(MouseEvent event) {
        switchPage(event, "/view/Profile.fxml");
    }
    @FXML
    public void handleHomeButton(ActionEvent event){
        switchPage(event, "/view/Home.fxml");
    }

    private void switchPage(Event event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Sayfa açılamadı: " + fxmlPath);
            e.printStackTrace();
        }
    }
}