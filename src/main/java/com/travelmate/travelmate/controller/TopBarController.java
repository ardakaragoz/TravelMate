package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class TopBarController {

    @FXML private Label userNameLabel;
    @FXML private Label userLevelLabel;

    public void initialize() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName());
            userLevelLabel.setText("Lvl. " + currentUser.getLevel());
        } else {
            userNameLabel.setText("Guest User");
            userLevelLabel.setText("Lvl. 0");
        }
    }
    @FXML
    public void handleHomeButton(ActionEvent event) {
        switchPage(event, "Home");
    }
    @FXML
    public void handleProfileClick(MouseEvent event) {
        switchPage(event, "Profile");
    }
    private void switchPage(Object eventSource, String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlName + ".fxml"));
            Parent root = loader.load();
            Stage stage;
            if (eventSource instanceof MouseEvent) {
                stage = (Stage) ((Node) ((MouseEvent) eventSource).getSource()).getScene().getWindow();
            } else {
                stage = (Stage) ((Node) ((ActionEvent) eventSource).getSource()).getScene().getWindow();
            }
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}