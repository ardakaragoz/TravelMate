package com.travelmate.travelmate;

import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.Recommendation;
import com.travelmate.travelmate.session.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException, ExecutionException, InterruptedException {
        
        loadCustomFonts();

        FirebaseService.initialize();
        UserList.loadAllUsers();
        CityList.loadAllCities();
        HobbyList.loadAllHobbies();
        TripTypeList.listAllTripTypes();
        ChannelList.loadAllChannels();
        ChatList.loadAllChats();
        TripList.loadAllTrips();
        RecommendationList.loadRecommendations();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/sign-in-page.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("TravelMate");
        stage.setScene(scene);
        stage.show();
    }

    private void loadCustomFonts() {
        try {
            
            String fontPath = "/fonts/LeaugeSpartan/";

            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Thin.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-ExtraLight.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Light.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Regular.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Medium.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-SemiBold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Bold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-ExtraBold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Black.ttf"), 12);

            
            

        } catch (Exception e) {
            System.err.println("Failed to load fonts: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}