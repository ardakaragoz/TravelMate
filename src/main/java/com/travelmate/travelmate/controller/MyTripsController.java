package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Trip;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyTripsController implements Initializable {

    @FXML private VBox upcomingTripsContainer;
    @FXML private VBox pastTripsContainer;

    // Inject the sidebar controller included in FXML with fx:id="sidebar"
    @FXML private SidebarController sidebarController;

    private User currentUser;
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Highlight Sidebar Button
        if (sidebarController != null) {
            sidebarController.setActivePage("MyTrips");
        }

        // 2. Check User Session
        currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            loadTrips();
        } else {
            setEmptyMessage(upcomingTripsContainer, "Please log in to view your trips.");
        }
    }

    private void loadTrips() {
        upcomingTripsContainer.getChildren().clear();
        pastTripsContainer.getChildren().clear();

        Label loading = new Label("Loading trips...");
        loading.setStyle("-fx-text-fill: #253A63; -fx-font-size: 16px;");
        upcomingTripsContainer.getChildren().add(loading);

        ArrayList<String> currentIds = currentUser.getCurrentTrips();
        ArrayList<String> pastIds = currentUser.getPastTrips();

        CompletableFuture.runAsync(() -> {
            // Load Upcoming
            if (currentIds != null && !currentIds.isEmpty()) {
                fetchAndRenderTrips(currentIds, upcomingTripsContainer, "No upcoming trips planned.");
            } else {
                Platform.runLater(() -> setEmptyMessage(upcomingTripsContainer, "No upcoming trips planned."));
            }

            // Load Past
            if (pastIds != null && !pastIds.isEmpty()) {
                fetchAndRenderTrips(pastIds, pastTripsContainer, "No travel history found.");
            } else {
                Platform.runLater(() -> setEmptyMessage(pastTripsContainer, "No travel history found."));
            }
        }, networkExecutor);
    }

    private void fetchAndRenderTrips(List<String> tripIds, VBox container, String emptyMsg) {
        List<CompletableFuture<Trip>> futures = new ArrayList<>();

        for (String tripId : tripIds) {
            if(tripId == null || tripId.isEmpty()) continue;
            CompletableFuture<Trip> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return new Trip(tripId);
                } catch (Exception e) {
                    System.err.println("Error loading trip " + tripId + ": " + e.getMessage());
                    return null;
                }
            }, networkExecutor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    List<Trip> loadedTrips = new ArrayList<>();
                    for (CompletableFuture<Trip> f : futures) {
                        try {
                            Trip t = f.get();
                            if (t != null) loadedTrips.add(t);
                        } catch (Exception e) { e.printStackTrace(); }
                    }

                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        if (loadedTrips.isEmpty()) {
                            setEmptyMessage(container, emptyMsg);
                        } else {
                            for (Trip trip : loadedTrips) {
                                container.getChildren().add(createTripCard(trip));
                            }
                        }
                    });
                });
    }

    private HBox createTripCard(Trip trip) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);

        // DESIGN: Darker Cream Box (#FFE7C2), Rounded Corners, Navy Border
        card.setStyle(
                "-fx-background-color: #FFE7C2; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #253A63; -fx-border-width: 2; -fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        // 1. City Image
        ImageView cityImage = new ImageView();
        cityImage.setFitWidth(120);
        cityImage.setFitHeight(90);
        loadImage(cityImage, trip.getDestinationName());

        Rectangle clip = new Rectangle(120, 90);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        cityImage.setClip(clip);

        // 2. Info Section
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label destLabel = new Label(trip.getDestinationName());
        destLabel.setTextFill(Color.web("#253A63"));
        destLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 22px;");

        String dateStr = "Date TBD";
        if (trip.getDepartureDate() != null) {
            dateStr = dateFormat.format(trip.getDepartureDate());
            if (trip.getEndDate() != null) {
                dateStr += " - " + dateFormat.format(trip.getEndDate());
            }
        }
        Label dateLabel = new Label(dateStr);
        dateLabel.setTextFill(Color.web("#253A63"));
        dateLabel.setStyle("-fx-font-size: 14px;");

        Label detailsLabel = new Label(String.format("Budget: %d %s  |  Mates: %d",
                trip.getAverageBudget(), trip.getCurrency(), trip.getMateCount()));
        detailsLabel.setTextFill(Color.web("#253A63"));
        detailsLabel.setStyle("-fx-font-size: 14px;");

        infoBox.getChildren().addAll(destLabel, dateLabel, detailsLabel);

        // 3. Action Button (Green #CCFF00)
        Button viewBtn = new Button("View");
        viewBtn.setPrefWidth(80);
        viewBtn.setPrefHeight(35);
        viewBtn.setStyle(
                "-fx-background-color: #CCFF00; " +
                        "-fx-text-fill: #253A63; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #253A63; -fx-border-radius: 20; " +
                        "-fx-cursor: hand;"
        );

        card.getChildren().addAll(cityImage, infoBox, viewBtn);
        return card;
    }

    private void loadImage(ImageView view, String cityName) {
        if(cityName == null) return;
        try {
            String path = "/images/city photos/" + cityName.toLowerCase() + ".jpg";
            URL url = getClass().getResource(path);
            if (url == null) {
                path = "/images/city photos/" + cityName.toLowerCase() + ".png";
                url = getClass().getResource(path);
            }
            if (url != null) {
                view.setImage(new Image(url.toExternalForm()));
            } else {
                view.setImage(new Image(getClass().getResource("/images/logoBlue.png").toExternalForm()));
            }
        } catch (Exception e) { }
    }

    private void setEmptyMessage(VBox container, String message) {
        container.getChildren().clear();
        Label lbl = new Label(message);
        lbl.setTextFill(Color.web("#253A63"));
        lbl.setStyle("-fx-font-size: 16px;");
        container.getChildren().add(lbl);
    }
}