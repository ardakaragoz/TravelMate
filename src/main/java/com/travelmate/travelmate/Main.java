package com.travelmate.travelmate;

import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.*;
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
        UserList.loadAllUsers();
        CityList.listAllCities();
        HobbyList.listAllHobbies();
        TripTypeList.listAllTripTypes();
        ChannelList.loadAllChannels();
        ChatList.loadAllChats();
        TripList.loadAllTrips();
        Parent root = FXMLLoader.load(getClass().getResource("/view/MyTrips.fxml"));        //StackPane root = new StackPane();
        Scene scene = new Scene(root);
        stage.setTitle("TravelMate");
        stage.setScene(scene);
        stage.show();
        //User user = new User("HuZUKiHoRQg7XRkRx5gq", "ardakaragoz", "Arda", "TR", "ahmetarda2006@hotmail.com.tr", "Arda123", "Male", 19);
        //user.updateUser();
    }

    public static void main(String[] args) {
        launch();
    }
}