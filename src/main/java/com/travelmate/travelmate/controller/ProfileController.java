package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.*;
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ProfileController {

    @FXML private BorderPane mainContainer;
    @FXML private Circle profileImageCircle;
    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label levelLabel;
    @FXML private ProgressBar levelProgressBar;
    @FXML private Label reviewScoreLabel;
    @FXML private Label bioLabel;
    @FXML private FlowPane hobbiesContainer;
    @FXML private FlowPane tripTypesContainer;
    @FXML private Label pastTripsLabel;
    @FXML private VBox helpPopup;
    private User currentUser;
    public void initialize() throws ExecutionException, InterruptedException {
        currentUser = UserSession.getCurrentUser();
        loadUserData();
        loadProfileImage(currentUser);
    }

    private void loadUserData() throws ExecutionException, InterruptedException {
        User user = UserSession.getCurrentUser();
        Profile profile = user.getProfile();

        fullNameLabel.setText(user.getName());
        usernameLabel.setText("@" + user.getUsername() + ", " + user.getAge());
        levelLabel.setText("Level " + user.getLevel());

        if (levelProgressBar != null) {
            double progress = 0.1 * (user.getLevelPoint() % 10);
            levelProgressBar.setProgress(progress);
        }

        String str_ = "Review Score: (" + user.getReviews().size() + ") ";
        for (int i = 1; i < (user.getReviewPoints() / user.getReviewCount()); i++) {
            str_ += "★";
        }
        reviewScoreLabel.setText(str_);
        bioLabel.setText(profile.getBiography());

        String rawUrl = profile.getProfilePictureUrl();
        String secureUrl = formatToHttps(rawUrl);
        System.out.println("DEBUG - Trying to load image from: " + rawUrl); // <--- Add this
        // 2. Load Image
        if (secureUrl != null && !secureUrl.isEmpty()) {
            try {
                // 'true' allows background loading
                Image img = new Image(secureUrl, true);
                profileImageCircle.setFill(new ImagePattern(img));
            } catch (Exception e) {
                loadDefaultImage();
            }
        } else {
            loadDefaultImage();
        }
        List<String> citiesList = new ArrayList<>();
        for (String id : user.getTrips()){
            Trip trip = TripList.getTrip(id);
            if (trip.isFinished() && !(citiesList.contains(trip.getDestinationName()))){
                citiesList.add(trip.getDestinationName());
            }
        }
        String str = "";
        for (String city : citiesList){
            str += city + ", ";
        }
        if (str.length() > 0){ str = str.substring(0, str.length() - 2); }
        pastTripsLabel.setText(str);

        for (Hobby hobby : profile.getHobbies()){
            addTag(hobbiesContainer, hobby.getName(), "#CCFF00");
        }

        for (TripTypes triptype : profile.getFavoriteTripTypes()) {
            addTag(tripTypesContainer, triptype.getId(), "#a4c2f2");
        }
    }
    private void loadDefaultImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/images/user_icons/img.png");
            if (stream != null) {
                profileImageCircle.setFill(new ImagePattern(new Image(stream)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- NEW METHOD: Loads image in background to prevent crashes ---




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
    public void logoutButton(ActionEvent event) throws IOException {
        UserSession.setCurrentUser(null);
        changeScene(event, "/view/sign-in-page.fxml");
    }

    @FXML
    public void handleEditProfileButton(ActionEvent event) {
        changeScene(event, "/view/EditProfile.fxml");
    }

    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
    @FXML
    public void handleReviewClick(javafx.scene.input.MouseEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Review.fxml"));
            javafx.scene.Parent root = loader.load();
            ReviewController controller = loader.getController();
            javafx.scene.Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();
            String myName = fullNameLabel.getText();
            controller.setReviewsContext(currentScene, currentUser);

            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            stage.setScene(new javafx.scene.Scene(root));

        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadProfileImage(User user) {
        if (profileImageCircle == null) return;
        profileImageCircle.setFill(javafx.scene.paint.Color.LIGHTGRAY);

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
                System.out.println("Profile Image Error: " + e.getMessage());
            }

            if (imageToSet != null) {
                final Image finalImg = imageToSet;
                Platform.runLater(() -> profileImageCircle.setFill(new ImagePattern(finalImg)));
            }
        }).start();
    }

    private String formatToHttps(String gsUrl) {
        if (gsUrl == null || gsUrl.isEmpty()) return null;
        if (gsUrl.startsWith("http")) return gsUrl;
        try {
            if (gsUrl.startsWith("gs://")) {
                String cleanPath = gsUrl.substring(5);
                int bucketSeparator = cleanPath.indexOf('/');
                if (bucketSeparator != -1) {
                    String bucket = cleanPath.substring(0, bucketSeparator);
                    String path = cleanPath.substring(bucketSeparator + 1);
                    String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                    return "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + encodedPath + "?alt=media";
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @FXML
    public void handleEditProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProfile.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) { e.printStackTrace(); }
    }
}