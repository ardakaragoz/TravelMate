package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Profile;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.TripTypeList;
import com.travelmate.travelmate.session.UserList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public void setProfileData(Scene prevScene, String userID) {
        this.previousScene = prevScene;

        User user = UserList.getUser(userID);
        fullNameLabel.setText(user.getName());
        usernameLabel.setText("@" + user.getUsername() + ", " + user.getAge());
        levelLabel.setText("Level " + user.getLevel());
        levelProgressBar.setProgress((user.getLevelPoint() % 10) / 10.0);
        Profile userProfile = user.getProfile();

        reviewScoreLabel.setText("Review Score: (9) ★★★★★");
        bioLabel.setText(userProfile.getBiography());


        try {
            String cleanName = userProfile.getId().toLowerCase().replaceAll("\\s+", "");
            String path = "/images/" + cleanName + ".jpg";
            if (getClass().getResource(path) == null) {
                path = "/images/" + cleanName + ".png";
            }
            if (getClass().getResource(path) != null) {
                profileImageCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream(path))));
            } else {
                profileImageCircle.setFill(Color.LIGHTGRAY);
            }
        } catch (Exception e) {
            System.out.println("Resim yüklenemedi: " + "");
        }
        loadDynamicTags(hobbiesContainer, Arrays.asList("Photography", "Hiking", "Museums"));
        loadDynamicTags(tripTypesContainer, Arrays.asList("Cultural", "City Break", "Nature"));
        pastTripsLabel.setText("London, Paris, Berlin, Tokyo");
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
    public void handleMessageButton(ActionEvent event) {

        System.out.println("Mesaj gönder...");
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
}