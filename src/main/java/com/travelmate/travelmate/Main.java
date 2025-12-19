package com.travelmate.travelmate;

import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException, ExecutionException, InterruptedException {
        FirebaseService.initialize();
        Parent root = FXMLLoader.load(getClass().getResource("/view/sign-in-page.fxml"));        //StackPane root = new StackPane();
        Scene scene = new Scene(root);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}