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

    public void initialize() throws ExecutionException, InterruptedException {
        loadUserData();
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

        reviewScoreLabel.setText("Review Score: (9)");
        bioLabel.setText(profile.getBiography());
        pastTripsLabel.setText("Budapest, Rome, Maldives, New York");

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
        setProfileImage("user1");

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
}