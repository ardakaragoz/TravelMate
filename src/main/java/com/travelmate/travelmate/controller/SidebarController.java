package com.travelmate.travelmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SidebarController {

    @FXML
    public void handleChannelsButton(ActionEvent event) {
        switchScene("/view/Channels.fxml", event);
    }

    @FXML
    public void handleMyTripsButton(ActionEvent event) {
        switchScene("/view/MyTrips.fxml", event);
    }

    @FXML
    public void handlePostTripButton(ActionEvent event) {
        switchScene("/view/PostTrip.fxml", event);
    }

    @FXML
    public void handleChatButton(ActionEvent event) {
        switchScene("/view/Chat.fxml", event);
    }

    private void switchScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("HATA: " + fxmlPath + " dosyası bulunamadı! Resources/view klasörünü kontrol et.");
        }
    }
}