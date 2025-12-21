package com.travelmate.travelmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class SidebarController {

    @FXML private Button homeButton;
    @FXML private Button myTripsButton;
    @FXML private Button postTripButton;
    @FXML private Button channelsButton;
    @FXML private Button chatButton;
    @FXML private Button profileButton;

    public void setActivePage(String pageName) {
        resetStyles();

        // Active Style (Green) - With Transparent Focus Fix
        String activeStyle = "-fx-background-color: #CCFF00; -fx-border-color: #253A63; -fx-border-width: 3; -fx-background-radius: 30; -fx-border-radius: 30; -fx-alignment: CENTER; -fx-text-fill: #253A63; -fx-font-weight: bold; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        // Active Chat Style - With Transparent Focus Fix
        String activeChatStyle = "-fx-background-color: #CCFF00; -fx-border-color: #253A63; -fx-border-width: 3; -fx-background-radius: 30; -fx-border-radius: 30; -fx-alignment: CENTER_LEFT; -fx-text-fill: #253A63; -fx-font-weight: bold; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        switch (pageName) {
            case "Home": if (homeButton != null) homeButton.setStyle(activeStyle); break;
            case "MyTrips": if (myTripsButton != null) myTripsButton.setStyle(activeStyle); break;
            case "PostTrip": if (postTripButton != null) postTripButton.setStyle(activeStyle); break;
            case "Channels": if (channelsButton != null) channelsButton.setStyle(activeStyle); break;
            case "Chat": if (chatButton != null) chatButton.setStyle(activeChatStyle); break;
            case "Profile": if (profileButton != null) profileButton.setStyle(activeStyle); break;
        }
    }

    private void resetStyles() {
        // Default Style (White) - With Transparent Focus Fix
        String defaultStyle = "-fx-background-color: WHITE; -fx-border-color: #253A63; -fx-border-width: 3; -fx-background-radius: 30; -fx-border-radius: 30; -fx-alignment: CENTER; -fx-text-fill: #253A63; -fx-font-weight: bold; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        // Default Chat Style - With Transparent Focus Fix
        String defaultChatStyle = "-fx-background-color: WHITE; -fx-border-color: #253A63; -fx-border-width: 3; -fx-background-radius: 30; -fx-border-radius: 30; -fx-alignment: CENTER_LEFT; -fx-text-fill: #253A63; -fx-font-weight: bold; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        if (homeButton != null) homeButton.setStyle(defaultStyle);
        if (myTripsButton != null) myTripsButton.setStyle(defaultStyle);
        if (postTripButton != null) postTripButton.setStyle(defaultStyle);
        if (channelsButton != null) channelsButton.setStyle(defaultStyle);
        if (chatButton != null) chatButton.setStyle(defaultChatStyle);
        if (profileButton != null) profileButton.setStyle(defaultStyle);
    }

    @FXML private void handleHomeButton(ActionEvent event) { switchScene("/view/Home.fxml", event); }
    @FXML private void handleMyTripsButton(ActionEvent event) { switchScene("/view/MyTrips.fxml", event); }
    @FXML private void handlePostTripButton(ActionEvent event) { switchScene("/view/PostTrip.fxml", event); }
    @FXML private void handleChannelsButton(ActionEvent event) { switchScene("/view/Channels.fxml", event); }
    @FXML private void handleChatButton(ActionEvent event) { switchScene("/view/Chat.fxml", event); }
    @FXML private void handleProfileButton(ActionEvent event) { switchScene("/view/Profile.fxml", event); }

    private void switchScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}