package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.*;
import com.travelmate.travelmate.session.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class OtherProfileController {

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
    @FXML private ComboBox hobbyComboBox;
    @FXML private ComboBox tripTypeComboBox;
    @FXML private Button messageButton;

    private Scene previousScene;

    public void setProfileData(Scene prevScene, String userID) throws ExecutionException, InterruptedException {
        this.previousScene = prevScene;

        User user = UserList.getUser(userID);
        fullNameLabel.setText(user.getName());
        usernameLabel.setText("@" + user.getUsername() + ", " + user.getAge());
        levelLabel.setText("Level " + user.getLevel());
        levelProgressBar.setProgress((user.getLevelPoint() % 10) / 10.0);
        Profile userProfile = user.getProfile();

        reviewScoreLabel.setText("Review Score: (9) ★★★★★");
        bioLabel.setText(userProfile.getBiography());
        messageButton.setOnAction(event -> {
            try {
                handleMessageButton(user, event);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            loadProfileImage(user);
        } catch (Exception e) {
            System.out.println("Resim yüklenemedi: " + "");
        }
        ArrayList<String> hobbyList = new ArrayList<>();
        for (Hobby hobby : userProfile.getHobbies()){
            hobbyList.add(hobby.getName());
        }
        ArrayList<String> tripTypesList = new ArrayList<>();
        for (TripTypes tripTypes : userProfile.getFavoriteTripTypes()){
            tripTypesList.add(tripTypes.getId());
        }
        loadDynamicTags(hobbiesContainer, hobbyList);
        loadDynamicTags(tripTypesContainer, tripTypesList);
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

    private void populateComboBoxes() {
        if (com.travelmate.travelmate.session.HobbyList.hobbies.isEmpty()) {
            com.travelmate.travelmate.session.HobbyList.loadAllHobbies();
        }
        if (hobbyComboBox != null) {
            // keySet() direkt String seti döndürür, ArrayList'e çevirmene bile gerek yok
            hobbyComboBox.getItems().setAll(com.travelmate.travelmate.session.HobbyList.hobbies.keySet());
        }
        if (com.travelmate.travelmate.session.TripTypeList.triptypes.isEmpty()) {
            com.travelmate.travelmate.session.TripTypeList.listAllTripTypes();
        }
        if (tripTypeComboBox != null) {
            tripTypeComboBox.getItems().setAll(com.travelmate.travelmate.session.TripTypeList.triptypes.keySet());
        }
    }
    @FXML
    public void handleBackButton(ActionEvent event) {
        try {
            if (previousScene != null) {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(previousScene);
            }
            else {
                System.out.println("Warning: previousScene is null. Defaulting to Home.");
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Home.fxml"));
                javafx.scene.Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error navigating back.");
        }
    }

    @FXML
    public void handleMessageButton(User user, ActionEvent event) throws ExecutionException, InterruptedException {

        User currentUser = UserSession.getCurrentUser();
        boolean oldChat = ChatList.checkDirectMessage(user, currentUser);
        if (oldChat){
            changeScene("/view/Chat.fxml", event);
        } else {
            DirectMessage newMes = new DirectMessage("" + System.currentTimeMillis(), currentUser, user);
            currentUser.addChatRoom(newMes);
            user.addChatRoom(newMes);
            changeScene("/view/Chat.fxml", event);
        }
    }


    private void loadDynamicTags(FlowPane container, List<String> tags) {
        container.getChildren().clear();
        for (String tag : tags) {
            Label tagLabel = new Label(tag);
            tagLabel.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-padding: 5 15 5 15; -fx-border-color: #1E3A5F; -fx-border-radius: 15; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
            container.getChildren().add(tagLabel);
        }
    }
    private void addClickEffect(Button button) {
        button.setCursor(javafx.scene.Cursor.HAND);

        button.setOnMousePressed(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(0.90); // %90'a küçül
            st.setToY(0.90);
            st.play();
        });

        button.setOnMouseReleased(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(1.0); // %100'e geri dön
            st.setToY(1.0);
            st.play();
        });

        button.setOnMouseExited(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void changeScene(String fxmlPath, ActionEvent event) {
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