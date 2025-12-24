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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    @FXML
    private void handleMousePressed(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setTranslateY(4);
            if (btn.getEffect() instanceof DropShadow) {
                ((DropShadow) btn.getEffect()).setOffsetY(2.0);
            }
        }
    }

    @FXML
    private void handleMouseReleased(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setTranslateY(0);
            if (btn.getEffect() instanceof DropShadow) {
                ((DropShadow) btn.getEffect()).setOffsetY(7.0);
            }
        }
    }

    private void loadProfileImage(User user) {
        if (profileImageCircle == null) return;

        new Thread(() -> {
            Image imageToSet = null;
            try {
                if (user.getProfile() != null) {
                    String rawUrl = user.getProfile().getProfilePictureUrl();

                    String secureUrl = formatToHttps(rawUrl);

                    if (secureUrl != null && !secureUrl.isEmpty()) {
                        imageToSet = new Image(secureUrl, false);
                    }
                }

                if (imageToSet == null || imageToSet.isError()) {
                    var resource = getClass().getResourceAsStream("/images/user_icons/img.png");
                    if (resource != null) imageToSet = new Image(resource);
                }
            } catch (Exception e) {
                System.out.println("TopBar Image Error: " + e.getMessage());
            }

            if (imageToSet != null) {
                final Image finalImg = imageToSet;
                Platform.runLater(() -> profileImageCircle.setFill(new ImagePattern(finalImg)));
            }
        }).start();
    }

    private String formatToHttps(String url) {
        if (url == null || !url.startsWith("gs://")) return url;
        try {
            String temp = url.substring(5);
            int firstSlash = temp.indexOf("/");
            if (firstSlash > 0) {
                String bucket = temp.substring(0, firstSlash);
                String path = temp.substring(firstSlash + 1);
                String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                return "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + encodedPath + "?alt=media";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    @FXML
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
            e.printStackTrace();
        }
    }
}