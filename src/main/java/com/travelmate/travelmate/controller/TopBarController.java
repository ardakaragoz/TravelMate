package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class TopBarController {

    @FXML private Label userNameLabel;
    @FXML private Label userLevelLabel;

    @FXML
    public void initialize() {
        // Fetch the logged-in user from the session
        User currentUser = UserSession.getCurrentUser();

        if (currentUser != null) {
            // Set Name (Prefer Name, fallback to Username)
            String displayName = currentUser.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getUsername();
            }
            userNameLabel.setText(displayName != null ? displayName : "Unknown");

            // Set Level
            userLevelLabel.setText("Lvl. " + currentUser.getLevel());
        } else {
            // Fallback if no user is logged in (e.g., testing)
            userNameLabel.setText("Guest");
            userLevelLabel.setText("");
        }
    }

    @FXML
    public void handleHomeButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}