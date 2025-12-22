package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import com.travelmate.travelmate.utils.ImageLoader;
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
    @FXML private Circle topBarProfileImage;
    @FXML private Label topBarUsernameLabel;

    @FXML
    public void initialize() {
        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            String displayName = currentUser.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getUsername();
            }
            ImageLoader.loadForUser(currentUser, topBarProfileImage);
            userNameLabel.setText(displayName != null ? displayName : "Unknown");
            userLevelLabel.setText("Lvl. " + currentUser.getLevel());
            ImageLoader.loadForUser(currentUser, topBarProfileImage);        } else {
            userNameLabel.setText("Guest");
            userLevelLabel.setText("");
        }
    }

    // --- BUTTON ANIMATIONS ---
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

                    // CONVERSION STEP: Fix the gs:// link
                    String secureUrl = formatToHttps(rawUrl);

                    if (secureUrl != null && !secureUrl.isEmpty()) {
                        // Load in background (false = synchronous in this thread)
                        imageToSet = new Image(secureUrl, false);
                    }
                }

                // Fallback to local default if URL failed
                if (imageToSet == null || imageToSet.isError()) {
                    // Using the file we know exists in your project
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

    // COPY THIS HELPER METHOD INTO THE CLASS
    private String formatToHttps(String url) {
        if (url == null || !url.startsWith("gs://")) return url;
        try {
            // Remove "gs://"
            String temp = url.substring(5);
            // Find the first slash separating bucket from path
            int firstSlash = temp.indexOf("/");
            if (firstSlash > 0) {
                String bucket = temp.substring(0, firstSlash);
                String path = temp.substring(firstSlash + 1);
                // Encode the path (fixes spaces and @ symbols)
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