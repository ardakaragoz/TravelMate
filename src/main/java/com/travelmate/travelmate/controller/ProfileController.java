package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.*;
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserSession;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/sign-in-page.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
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