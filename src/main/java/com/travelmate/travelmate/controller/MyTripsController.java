package com.travelmate.travelmate.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserList;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture; // <--- THIS FIXES THE ERROR
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyTripsController implements Initializable {

    @FXML private FlowPane upcomingTripsContainer;
    @FXML private FlowPane pastTripsContainer;
    @FXML private SidebarController sidebarController;
    @FXML private BorderPane mainContent;

    // --- DETAILS POPUP ---
    @FXML private VBox detailsPopup;
    @FXML private ImageView detailsBannerImage;
    @FXML private Label detailHeaderDest, detailHeaderTitle;
    @FXML private Circle detailCreatorImage;
    @FXML private Label detailCreatorName;
    @FXML private Label detailNotes, detailBudget, detailDate, detailItinerary, detailFriendsLabel;
    @FXML private HBox detailFriendsContainer;

    // --- FORUM POPUP ---
    @FXML private VBox forumPopup;
    @FXML private Label forumTitleDest, forumSubtitle;
    @FXML private VBox forumListContainer;
    @FXML private TextField forumInputField;
    @FXML private ScrollPane forumScrollPane;

    // --- EDIT & REQUESTS POPUPS ---
    @FXML private VBox editPopup, requestsPopup;
    @FXML private TextField editDestination, editDeparture, editBudget, editItinerary;
    @FXML private TextArea editNotes;
    @FXML private Spinner<Integer> editDays, editMates;
    @FXML private DatePicker editDate;
    @FXML private ChoiceBox<String> editCurrency;
    @FXML private VBox requestsListContainer;

    private User currentUser;
    private Trip selectedTrip;
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    // Cache for Forum Messages
    private Map<String, List<Message>> forumCache = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebarController != null) sidebarController.setActivePage("MyTrips");

        // Init Form Elements
        editDays.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 4));
        editMates.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
        editCurrency.getItems().addAll("$", "€", "₺", "£");

        currentUser = UserSession.getCurrentUser();
        if (currentUser != null) loadTrips();

        // Auto-scroll forum to bottom
        forumListContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            forumScrollPane.setVvalue(1.0);
        });
    }

    private void loadTrips() {
        upcomingTripsContainer.getChildren().clear();
        pastTripsContainer.getChildren().clear();
        currentUser = UserList.getUser(currentUser.getId());

        CompletableFuture.runAsync(() -> {
            ArrayList<String> currentIds = currentUser.getCurrentTrips();
            ArrayList<String> pastIds = currentUser.getPastTrips();
            if (currentIds != null) fetchAndRenderTrips(currentIds, upcomingTripsContainer, "No upcoming trips.");
            if (pastIds != null) fetchAndRenderTrips(pastIds, pastTripsContainer, "No past trips.");
        }, networkExecutor);
    }

    private void fetchAndRenderTrips(List<String> tripIds, FlowPane container, String emptyMsg) {
        List<CompletableFuture<Trip>> futures = new ArrayList<>();
        for (String tripId : tripIds) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try { return TripList.getTrip(tripId); } catch (Exception e) { return null; }
            }, networkExecutor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
            List<Trip> loadedTrips = new ArrayList<>();
            for (CompletableFuture<Trip> f : futures) {
                try { if (f.get() != null) loadedTrips.add(f.get()); } catch (Exception e) {}
            }
            Platform.runLater(() -> {
                container.getChildren().clear();
                if (loadedTrips.isEmpty()) container.getChildren().add(new Label(emptyMsg));
                else for (Trip trip : loadedTrips) container.getChildren().add(createTripCard(trip));
            });
        });
    }

    private HBox createTripCard(Trip trip) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(420);
        card.setStyle("-fx-background-color: #FFE7C2; -fx-background-radius: 20; -fx-border-color: #253A63; -fx-border-width: 2; -fx-border-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 4);");

        ImageView cityImage = new ImageView();
        cityImage.setFitWidth(100); cityImage.setFitHeight(90);
        loadImage(cityImage, trip.getDestinationName());
        Rectangle clip = new Rectangle(100, 90); clip.setArcWidth(20); clip.setArcHeight(20);
        cityImage.setClip(clip);

        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        Label destLabel = new Label(trip.getDestinationName());
        destLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #253A63;");
        Label dateLabel = new Label(trip.getDepartureDate() != null ? dateFormat.format(trip.getDepartureDate()) : "TBD");
        dateLabel.setStyle("-fx-text-fill: #253A63;");
        Label budgetLabel = new Label(trip.getAverageBudget() + " " + trip.getCurrency());
        budgetLabel.setStyle("-fx-text-fill: #253A63;");
        infoBox.getChildren().addAll(destLabel, dateLabel, budgetLabel);

        VBox buttonBox = new VBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button detailsBtn = createActionButton("View Details", "#CCFF00");
        detailsBtn.setOnAction(e -> openDetailsPopup(trip));
        buttonBox.getChildren().add(detailsBtn);

        if (currentUser.getId().equals(trip.getUser())) {
            Button editBtn = createActionButton("Edit Trip", "#CCFF00");
            editBtn.setOnAction(e -> openEditPopup(trip));
            Button requestsBtn = createActionButton("Requests", "#CCFF00");
            requestsBtn.setOnAction(e -> openRequestsPopup(trip));
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

    // --- POPUP: DETAILS ---
    private void openDetailsPopup(Trip trip) {
        selectedTrip = trip;
        loadImage(detailsBannerImage, trip.getDestinationName());
        detailHeaderDest.setText(trip.getDestinationName());
        detailHeaderTitle.setText(trip.getDestinationName() + " Trip for " + trip.getDays() + " days");
        detailNotes.setText(trip.getAdditionalNotes());
        detailBudget.setText("BUDGET " + trip.getAverageBudget() + " " + trip.getCurrency());
        detailDate.setText(trip.getDepartureDate() != null ? dateFormat.format(trip.getDepartureDate()) : "");
        detailItinerary.setText(trip.getItinerary());

        CompletableFuture.runAsync(() -> {
            User creator = UserList.getUser(trip.getUser());
            ArrayList<String> mates = trip.getJoinedMates();
            Platform.runLater(() -> {
                if (creator != null) detailCreatorName.setText(creator.getName());

                detailFriendsContainer.getChildren().clear();
                detailFriendsLabel.setText("Friends: " + (mates != null ? mates.size() : 0) + "/" + trip.getMateCount());
                if (mates != null) {
                    for (String uid : mates) {
                        Circle avatar = new Circle(15, Color.LIGHTGRAY);
                        avatar.setStroke(Color.WHITE); avatar.setStrokeWidth(2);
                        detailFriendsContainer.getChildren().add(avatar);
                    }
                }
            });
        });
        showPopup(detailsPopup);
    }

    // --- POPUP: FORUM (Chat with DB) ---
    @FXML
    public void handleOpenForum() {
        detailsPopup.setVisible(false);
        forumPopup.setVisible(true);
        forumPopup.toFront();

        forumTitleDest.setText(selectedTrip.getDestinationName());
        forumSubtitle.setText(selectedTrip.getDestinationName() + " Trip with Friends");
        forumListContainer.getChildren().clear();

        loadForumMessages(selectedTrip);
    }

    private void loadForumMessages(Trip trip) {
        forumListContainer.getChildren().clear();

        Task<List<Message>> loadTask = new Task<>() {
            @Override
            protected List<Message> call() throws Exception {
                String chatRoomId = trip.getId();

                // 1. Get Chat Room Document
                DocumentSnapshot roomDoc = FirebaseService.getFirestore().collection("chatrooms").document(chatRoomId).get().get();
                if (!roomDoc.exists()) {
                    return new ArrayList<>();
                }

                List<String> messageIds = (List<String>) roomDoc.get("messages");
                if (messageIds == null || messageIds.isEmpty()) return new ArrayList<>();

                // 2. Batch Fetch Messages
                int total = messageIds.size();
                int limit = 25;
                int start = Math.max(0, total - limit);
                List<String> recentIds = messageIds.subList(start, total);

                List<DocumentReference> refs = new ArrayList<>();
                for (String msgId : recentIds) {
                    if (msgId != null && !msgId.trim().isEmpty()) {
                        refs.add(FirebaseService.getFirestore().collection("messages").document(msgId));
                    }
                }
                if (refs.isEmpty()) return new ArrayList<>();

                ApiFuture<List<DocumentSnapshot>> future = FirebaseService.getFirestore().getAll(refs.toArray(new DocumentReference[0]));
                List<DocumentSnapshot> snapshots = future.get();

                List<Message> loadedMessages = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots) {
                    if (doc.exists()) {
                        loadedMessages.add(new Message(doc));
                    }
                }

                loadedMessages.sort(Comparator.comparing(m -> m.getCreatedAt() != null ? m.getCreatedAt() : new Date(0)));
                return loadedMessages;
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Message> messages = loadTask.getValue();
            forumCache.put(trip.getId(), messages);

            Platform.runLater(() -> {
                for (Message msg : messages) {
                    boolean isSelf = msg.getSender() != null && msg.getSender().getId().equals(currentUser.getId());
                    String name = (msg.getSender() != null) ? msg.getSender().getName() : "User";
                    addForumBubble(msg.getMessage(), name, isSelf);
                }
            });
        });

        new Thread(loadTask).start();
    }

    @FXML
    public void handleSendForumMessage() {
        String text = forumInputField.getText().trim();
        if (!text.isEmpty()) {
            // 1. Optimistic UI Update
            addForumBubble(text, currentUser.getName(), true);
            forumInputField.clear();

            // 2. Background DB Update
            new Thread(() -> {
                try {
                    String msgId = UUID.randomUUID().toString();
                    Message msg = new Message(msgId, text, currentUser);

                    String chatRoomId = selectedTrip.getId();

                    FirebaseService.getFirestore().collection("chatrooms")
                            .document(chatRoomId)
                            .update("messages", FieldValue.arrayUnion(msgId));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    public void closeForumPopup() {
        forumPopup.setVisible(false);
        detailsPopup.setVisible(true);
    }

    private void addForumBubble(String text, String senderName, boolean isSelf) {
        HBox row = new HBox(10);
        row.setAlignment(isSelf ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(300);
        bubble.setPadding(new Insets(10, 15, 10, 15));

        if (isSelf) {
            bubble.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        } else {
            bubble.setStyle("-fx-background-color: #FFCB7B; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        }

        Label nameLbl = new Label(senderName);
        nameLbl.setFont(Font.font("League Spartan Bold", 12));
        nameLbl.setTextFill(Color.web("#253A63"));

        Label msgLbl = new Label(text);
        msgLbl.setWrapText(true);
        msgLbl.setTextFill(Color.web("#253A63"));
        msgLbl.setFont(Font.font("League Spartan", 14));

        bubble.getChildren().addAll(nameLbl, msgLbl);

        Circle pic = new Circle(18, isSelf ? Color.LIGHTBLUE : Color.LIGHTGRAY);
        pic.setStroke(Color.BLACK);

        if (isSelf) row.getChildren().addAll(bubble, pic);
        else row.getChildren().addAll(pic, bubble);

        forumListContainer.getChildren().add(row);
        forumScrollPane.layout();
        forumScrollPane.setVvalue(1.0);
    }

    // --- OTHER POPUPS ---
    private void openEditPopup(Trip trip) {
        selectedTrip = trip;
        editDestination.setText(trip.getDestinationName());
        editDeparture.setText(trip.getDepartureLocation());
        editBudget.setText(String.valueOf(trip.getAverageBudget()));
        editCurrency.setValue(trip.getCurrency());
        editDays.getValueFactory().setValue(trip.getDays());
        editMates.getValueFactory().setValue(trip.getMateCount());
        editNotes.setText(trip.getAdditionalNotes());
        editItinerary.setText(trip.getItinerary());
        if (trip.getDepartureDate() != null) {
            editDate.setValue(trip.getDepartureDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        showPopup(editPopup);
    }

    @FXML
    public void saveTripEdit() {
        if (selectedTrip != null) {
            try {
                selectedTrip.setDestination(editDestination.getText());
                selectedTrip.setDepartureLocation(editDeparture.getText());
                selectedTrip.setAverageBudget(Integer.parseInt(editBudget.getText()));
                selectedTrip.setCurrency(editCurrency.getValue());
                selectedTrip.setDays(editDays.getValue());
                selectedTrip.setMateCount(editMates.getValue());
                selectedTrip.setAdditionalNotes(editNotes.getText());
                selectedTrip.setItinerary(editItinerary.getText());
                if (editDate.getValue() != null) {
                    selectedTrip.setDepartureDate(Date.from(editDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                selectedTrip.updateTrip();
                closePopups();
                loadTrips();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void openRequestsPopup(Trip trip) {
        selectedTrip = trip;
        requestsListContainer.getChildren().clear();
        requestsListContainer.getChildren().add(new Label("Loading..."));
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
        if (users.isEmpty()) { requestsListContainer.getChildren().add(new Label("No pending requests.")); return; }
        for (User u : users) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #253A63; -fx-border-radius: 10;");
            Label nameLbl = new Label(u.getUsername());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #253A63;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            nameLbl.setMaxWidth(Double.MAX_VALUE);
            Button accept = new Button("✔"); accept.setOnAction(e -> handleApprove(u));
            Button deny = new Button("✖"); deny.setOnAction(e -> handleDeny(u));
            row.getChildren().addAll(nameLbl, accept, deny);
            requestsListContainer.getChildren().add(row);
        }
    }

    private void handleApprove(User user) {
        try { selectedTrip.addMate(user); openRequestsPopup(selectedTrip); } catch (Exception e) {}
    }
    private void handleDeny(User user) {
        try { selectedTrip.removePendingMate(user); openRequestsPopup(selectedTrip); } catch (Exception e) {}
    }

    private void showPopup(VBox popup) {
        mainContent.setEffect(new GaussianBlur(10));
        popup.setVisible(true);
        popup.toFront();
    }

    @FXML
    public void closePopups() {
        mainContent.setEffect(null);
        detailsPopup.setVisible(false);
        requestsPopup.setVisible(false);
        editPopup.setVisible(false);
        forumPopup.setVisible(false);
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
}