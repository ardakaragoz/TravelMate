package com.travelmate.travelmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class SignInController {
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    // email textField id: emailField
    // password textField id: passwordField
    @FXML
    public void handleLoginButton(ActionEvent event) {
        changeScene("/view/Home.fxml", event);
    }

    @FXML
    public void handleSignUpLink(ActionEvent event) {
        changeScene("/view/register-page.fxml", event);
    }

    private void changeScene(String fileName, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fileName));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}