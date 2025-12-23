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
        // --- 1. LOAD ALL CUSTOM FONTS ---
        // Note: The folder name in your project is 'LeaugeSpartan' (with the typo)
        loadCustomFonts();

        // --- 2. INITIALIZE SERVICES ---
        FirebaseService.initialize();
        UserList.loadAllUsers();
        CityList.loadAllCities();
        HobbyList.loadAllHobbies();
        TripTypeList.listAllTripTypes();
        ChannelList.loadAllChannels();
        ChatList.loadAllChats();
        TripList.loadAllTrips();
        RecommendationList.loadRecommendations();
        // --- 3. LOAD SCENE ---
        // Ensuring we use the correct path to your sign-in page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/sign-in-page.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("TravelMate");
        stage.setScene(scene);
        stage.show();
    }

    private void loadCustomFonts() {
        try {
            // BE CAREFUL: Your folder is named 'LeaugeSpartan' (typo), not 'LeagueSpartan'
            String fontPath = "/fonts/LeaugeSpartan/";

            // Load specific weights
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Thin.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-ExtraLight.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Light.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Regular.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Medium.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-SemiBold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Bold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-ExtraBold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream(fontPath + "LeagueSpartan-Black.ttf"), 12);

            // DEBUG: Print names so you know what to put in FXML
            // System.out.println("Loaded Fonts: " + Font.getFontNames("League Spartan"));

        } catch (Exception e) {
            System.err.println("Failed to load fonts: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}