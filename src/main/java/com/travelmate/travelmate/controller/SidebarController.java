package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class SidebarController {

    @FXML private Button homeButton;
    @FXML private Button myTripsButton;
    @FXML private Button postTripButton;
    @FXML private Button channelsButton;
    @FXML private Button chatButton;
    @FXML private Button profileButton;
    @FXML private Button adminButton; 

    public void initialize() {
        checkAdminStatus();
    }

    private void checkAdminStatus() {
        if (adminButton == null) return;

        User currentUser = UserSession.getCurrentUser();
        System.out.println(currentUser.isAdmin());
        if (currentUser.isAdmin()){
            adminButton.setVisible(true);

        } else {
            adminButton.setVisible(false);

        }
    }

    public void setActivePage(String pageName) {
        resetStyles();
        checkAdminStatus(); 

        
        String activeStyle = "-fx-background-color: #253A63, #CCFF00; -fx-background-insets: 0, 3; -fx-background-radius: 30, 27; -fx-alignment: CENTER; -fx-text-fill: #253A63; -fx-font-family: 'League Spartan Black'; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        String activeChatStyle = "-fx-background-color: #253A63, #CCFF00; -fx-background-insets: 0, 3; -fx-background-radius: 30, 27; -fx-alignment: CENTER_LEFT; -fx-text-fill: #253A63; -fx-font-family: 'League Spartan Black'; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        switch (pageName) {
            case "Home": if (homeButton != null) homeButton.setStyle(activeStyle); break;
            case "MyTrips": if (myTripsButton != null) myTripsButton.setStyle(activeStyle); break;
            case "PostTrip": if (postTripButton != null) postTripButton.setStyle(activeStyle); break;
            case "Channels": if (channelsButton != null) channelsButton.setStyle(activeStyle); break;
            case "Chat": if (chatButton != null) chatButton.setStyle(activeChatStyle); break;
            case "Profile": if (profileButton != null) profileButton.setStyle(activeStyle); break;
            case "Admin": if (adminButton != null) adminButton.setStyle(activeStyle); break;
        }
    }

    private void resetStyles() {
        String defaultStyle = "-fx-background-color: #253A63, WHITE; -fx-background-insets: 0, 3; -fx-background-radius: 30, 27; -fx-alignment: CENTER; -fx-text-fill: #253A63; -fx-font-family: 'League Spartan Black'; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        String defaultChatStyle = "-fx-background-color: #253A63, WHITE; -fx-background-insets: 0, 3; -fx-background-radius: 30, 27; -fx-alignment: CENTER_LEFT; -fx-text-fill: #253A63; -fx-font-family: 'League Spartan Black'; -fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        if (homeButton != null) homeButton.setStyle(defaultStyle);
        if (myTripsButton != null) myTripsButton.setStyle(defaultStyle);
        if (postTripButton != null) postTripButton.setStyle(defaultStyle);
        if (channelsButton != null) channelsButton.setStyle(defaultStyle);
        if (chatButton != null) chatButton.setStyle(defaultChatStyle);
        if (profileButton != null) profileButton.setStyle(defaultStyle);
        if (adminButton != null) adminButton.setStyle(defaultStyle);
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setTranslateY(4);
            if (btn.getEffect() instanceof DropShadow) ((DropShadow) btn.getEffect()).setOffsetY(3.0);
        }
    }

    @FXML
    private void handleMouseReleased(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setTranslateY(0);
            if (btn.getEffect() instanceof DropShadow) ((DropShadow) btn.getEffect()).setOffsetY(7.0);
        }
    }

    @FXML private void handleHomeButton(ActionEvent event) { switchScene("/view/Home.fxml", event); }
    @FXML private void handleMyTripsButton(ActionEvent event) { switchScene("/view/MyTrips.fxml", event); }
    @FXML private void handlePostTripButton(ActionEvent event) { switchScene("/view/PostTrip.fxml", event); }
    @FXML private void handleChannelsButton(ActionEvent event) { switchScene("/view/Channels.fxml", event); }
    @FXML private void handleChatButton(ActionEvent event) { switchScene("/view/Chat.fxml", event); }
    @FXML private void handleProfileButton(ActionEvent event) { switchScene("/view/Profile.fxml", event); }
    @FXML private void handleAdminButton(ActionEvent event) { switchScene("/view/AdminPage.fxml", event); }
    private void switchScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}