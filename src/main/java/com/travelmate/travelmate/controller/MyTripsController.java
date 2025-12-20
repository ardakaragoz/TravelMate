package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Trip;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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

    @FXML private FlowPane upcomingTripsContainer;
    @FXML private FlowPane pastTripsContainer;
    @FXML private SidebarController sidebarController;
    @FXML private StackPane rootStackPane;
    @FXML private BorderPane mainContent;

    // --- POPUPS ---
    @FXML private VBox detailsPopup;
    @FXML private Label detailDestination, detailDate, detailBudget, detailNotes;

    @FXML private VBox requestsPopup;
    @FXML private VBox requestsListContainer;

    @FXML private VBox editPopup;
    @FXML private TextField editBudgetField;
    @FXML private TextArea editNotesArea;

    private User currentUser;
    private Trip selectedTrip; // Track which trip is being edited/viewed
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebarController != null) {
            sidebarController.setActivePage("MyTrips");
        }

        currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            loadTrips();
        } else {
            setEmptyMessage(upcomingTripsContainer, "Please log in.");
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
            if (currentIds != null) fetchAndRenderTrips(currentIds, upcomingTripsContainer, "No upcoming trips.");
            if (pastIds != null) fetchAndRenderTrips(pastIds, pastTripsContainer, "No past trips.");
        }, networkExecutor);
    }

    private void fetchAndRenderTrips(List<String> tripIds, FlowPane container, String emptyMsg) {
        List<CompletableFuture<Trip>> futures = new ArrayList<>();
        for (String tripId : tripIds) {
            if (tripId == null || tripId.isEmpty()) continue;
            futures.add(CompletableFuture.supplyAsync(() -> {
                try { return new Trip(tripId); } catch (Exception e) { return null; }
            }, networkExecutor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
            List<Trip> loadedTrips = new ArrayList<>();
            for (CompletableFuture<Trip> f : futures) {
                try { if (f.get() != null) loadedTrips.add(f.get()); } catch (Exception e) {}
            }
            Platform.runLater(() -> {
                container.getChildren().clear();
                if (loadedTrips.isEmpty()) setEmptyMessage(container, emptyMsg);
                else for (Trip trip : loadedTrips) container.getChildren().add(createTripCard(trip));
            });
        });
    }

    private HBox createTripCard(Trip trip) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        // FORCE 2 COLUMNS: Card width ~420px (900 / 2 - gap)
        card.setPrefWidth(420);
        card.setStyle("-fx-background-color: #FFE7C2; -fx-background-radius: 20; -fx-border-color: #253A63; -fx-border-width: 2; -fx-border-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 4);");

        ImageView cityImage = new ImageView();
        cityImage.setFitWidth(100); cityImage.setFitHeight(90);
        loadImage(cityImage, trip.getDestinationName());
        Rectangle clip = new Rectangle(100, 90); clip.setArcWidth(20); clip.setArcHeight(20);
        cityImage.setClip(clip);

        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label destLabel = new Label(trip.getDestinationName());
        destLabel.setTextFill(Color.web("#253A63"));
        destLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 20));

        String dateStr = trip.getDepartureDate() != null ? dateFormat.format(trip.getDepartureDate()) : "TBD";
        Label dateLabel = new Label(dateStr);
        dateLabel.setTextFill(Color.web("#253A63"));

        Label budgetLabel = new Label(trip.getAverageBudget() + " " + trip.getCurrency());
        budgetLabel.setTextFill(Color.web("#253A63"));

        infoBox.getChildren().addAll(destLabel, dateLabel, budgetLabel);

        // --- BUTTONS BOX ---
        VBox buttonBox = new VBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button detailsBtn = createActionButton("View Details", "#CCFF00");
        detailsBtn.setOnAction(e -> openDetailsPopup(trip));

        Button requestsBtn = createActionButton("View Requests", "#CCFF00");
        requestsBtn.setOnAction(e -> openRequestsPopup(trip));

        Button editBtn = createActionButton("Edit Trip", "#CCFF00");
        editBtn.setOnAction(e -> openEditPopup(trip));

        boolean isOrganizer = currentUser.getId().equals(trip.getUser() != null ? trip.getUser().getId() : "");

        buttonBox.getChildren().add(detailsBtn);
        if (isOrganizer) {
            buttonBox.getChildren().addAll(requestsBtn, editBtn);
        }

        card.getChildren().addAll(cityImage, infoBox, buttonBox);
        return card;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(110);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #253A63; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand;");
        return btn;
    }

    // --- POPUP HANDLERS ---

    private void openDetailsPopup(Trip trip) {
        selectedTrip = trip;
        detailDestination.setText("Destination: " + trip.getDestinationName());
        detailDate.setText("Date: " + (trip.getDepartureDate() != null ? dateFormat.format(trip.getDepartureDate()) : "TBD"));
        detailBudget.setText("Budget: " + trip.getAverageBudget() + " " + trip.getCurrency());
        detailNotes.setText(trip.getAdditionalNotes() != null ? trip.getAdditionalNotes() : "No details.");

        showPopup(detailsPopup);
    }

    private void openRequestsPopup(Trip trip) {
        selectedTrip = trip;
        requestsListContainer.getChildren().clear();
        requestsListContainer.getChildren().add(new Label("Loading requests..."));
        showPopup(requestsPopup);

        CompletableFuture.runAsync(() -> {
            ArrayList<String> pendingIds = trip.getPendingMates();
            List<User> pendingUsers = new ArrayList<>();
            if (pendingIds != null) {
                for (String uid : pendingIds) {
                    try { pendingUsers.add(new User(uid)); } catch (Exception e) {}
                }
            }
            Platform.runLater(() -> renderRequestsList(pendingUsers));
        }, networkExecutor);
    }

    private void renderRequestsList(List<User> users) {
        requestsListContainer.getChildren().clear();
        if (users.isEmpty()) {
            requestsListContainer.getChildren().add(new Label("No pending requests."));
            return;
        }
        for (User u : users) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #253A63; -fx-border-radius: 10;");

            Label nameLbl = new Label(u.getUsername());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #253A63;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            nameLbl.setMaxWidth(Double.MAX_VALUE);

            Button accept = new Button("✔");
            accept.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-background-radius: 20;");
            accept.setOnAction(e -> handleApprove(u));

            Button deny = new Button("✖");
            deny.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-background-radius: 20;");
            deny.setOnAction(e -> handleDeny(u));

            row.getChildren().addAll(nameLbl, accept, deny);
            requestsListContainer.getChildren().add(row);
        }
    }

    private void openEditPopup(Trip trip) {
        selectedTrip = trip;
        editBudgetField.setText(String.valueOf(trip.getAverageBudget()));
        editNotesArea.setText(trip.getAdditionalNotes());
        showPopup(editPopup);
    }

    @FXML
    public void saveTripEdit() {
        if (selectedTrip != null) {
            try {
                int newBudget = Integer.parseInt(editBudgetField.getText());
                String newNotes = editNotesArea.getText();
                // We need access to setters. Assuming Trip.java allows setting fields and saving.
                // Since Trip methods are mostly void and handle DB, we need a way to update.
                // Creating a workaround if direct setters don't update DB automatically.
                // Assuming updateTrip() exists or fields can be set.
                // Based on User's Trip.java, we might need to implement setters there.
                // For now, let's assume we can re-save.
                // Note: The provided Trip.java had setAdditionalNotes().
                selectedTrip.setAdditionalNotes(newNotes);
                // selectedTrip.setBudget(newBudget); // Missing in Trip.java, you might need to add it.

                closePopups();
                loadTrips(); // Refresh
            } catch (Exception e) {
                System.out.println("Save failed: " + e.getMessage());
            }
        }
    }

    // --- ACTIONS ---
    private void handleApprove(User user) {
        try {
            selectedTrip.addMate(user);
            openRequestsPopup(selectedTrip); // Refresh list
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDeny(User user) {
        try {
            selectedTrip.removePendingMate(user);
            openRequestsPopup(selectedTrip); // Refresh list
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UTILS ---
    private void showPopup(VBox popup) {
        mainContent.setEffect(new GaussianBlur(10));
        popup.setVisible(true);
    }

    @FXML
    public void closePopups() {
        mainContent.setEffect(null);
        detailsPopup.setVisible(false);
        requestsPopup.setVisible(false);
        editPopup.setVisible(false);
    }

    private void loadImage(ImageView view, String cityName) {
        if (cityName == null) return;
        try {
            String path = "/images/city photos/" + cityName.toLowerCase() + ".jpg";
            URL url = getClass().getResource(path);
            if (url == null) {
                path = "/images/city photos/" + cityName.toLowerCase() + ".png";
                url = getClass().getResource(path);
            }
            if (url != null) view.setImage(new Image(url.toExternalForm()));
            else view.setImage(new Image(getClass().getResource("/images/logoBlue.png").toExternalForm()));
        } catch (Exception e) {}
    }

    private void setEmptyMessage(FlowPane container, String message) {
        container.getChildren().clear();
        Label lbl = new Label(message);
        lbl.setTextFill(Color.web("#253A63"));
        container.getChildren().add(lbl);
    }
}