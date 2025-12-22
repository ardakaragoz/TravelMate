package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Channel;
import com.travelmate.travelmate.model.Trip;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.ChannelList;
import com.travelmate.travelmate.session.TripList;
import com.travelmate.travelmate.session.UserList;
import com.travelmate.travelmate.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ChannelsController {

    @FXML private SidebarController sidebarController;
    @FXML private BorderPane mainContainer;

    
    @FXML private VBox citySelectionView;
    @FXML private TextField searchField;

    
    @FXML private VBox channelDetailView;
    @FXML private Label channelTitleLabel;
    @FXML private VBox channelTripsContainer;

    
    @FXML private Button channelJoinButton;
    @FXML private Button channelChatButton;
    @FXML private Button channelRecsButton;

    
    @FXML private VBox tripDetailsPopup;
    @FXML private Label detailDestinationLabel, detailDateLabel, detailDescLabel, detailBudgetLabel;
    @FXML private Button popupJoinButton;

    
    @FXML private VBox channelChatPopup;
    @FXML private Label chatPopupTitle;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField chatInputPopup;
    @FXML private ScrollPane chatScrollPane;

    public void initialize() {
        if (sidebarController != null) sidebarController.setActivePage("Channels");

        
        loadCityButtons();
    }

    public void openSpecificChannel(String cityName) {
        
        String fixedName = cityName;
        if (cityName != null && cityName.length() > 1) {
            fixedName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1).toLowerCase();
        }

        
        try {
            openChannel(fixedName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleOpenChatPopup() {
        if (channelChatPopup != null) {
            String currentCity = channelTitleLabel.getText();
            
            if (currentCity == null || currentCity.equals("Select a City")) return;

            if (chatPopupTitle != null) chatPopupTitle.setText(currentCity + " Chat");

            
            if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
            channelChatPopup.setVisible(true);
            channelChatPopup.toFront();

            
            loadDummyMessages();
        }
    }

    @FXML
    public void closeChatPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (channelChatPopup != null) channelChatPopup.setVisible(false);
    }

    @FXML
    public void handleSendPopupMessage() {
        String msg = chatInputPopup.getText();
        if (msg != null && !msg.isEmpty()) {
            addMessageBubble(msg, true); 
            chatInputPopup.clear();
        }
    }

    private void addMessageBubble(String text, boolean isMe) {
        HBox bubble = new HBox();
        bubble.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(350);
        lbl.setPadding(new Insets(10));
        lbl.setFont(Font.font("System", 14));

        if (isMe) {
            lbl.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15 15 0 15; -fx-text-fill: #1E3A5F;");
        } else {
            lbl.setStyle("-fx-background-color: WHITE; -fx-background-radius: 15 15 15 0; -fx-text-fill: black; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        }

        bubble.getChildren().add(lbl);
        chatMessagesContainer.getChildren().add(bubble);
        if(chatScrollPane != null) chatScrollPane.setVvalue(1.0);
    }

    private void loadDummyMessages() {
        if(chatMessagesContainer == null) return;
        chatMessagesContainer.getChildren().clear();
        addMessageBubble("Hey! Is there anyone staying at Covent Garden?", false);
        addMessageBubble("I highly recommend the Gokyuzu restaurant!", false);
    }
    

    private void loadCityButtons() {
        String[] cities = {"London", "Barcelona", "Rio de Janeiro", "Tokyo", "Rome", "New York", "Sydney", "Paris"};
        if(citySelectionView == null) return;
        citySelectionView.getChildren().clear();

        for (String city : cities) {
            Button btn = new Button(city);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPadding(new Insets(15, 20, 15, 20));
            btn.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-cursor: hand;");
            addClickEffect(btn);

            btn.setOnAction(e -> {
                updateActiveCityButton(btn);
                openChannel(city);
            });
            citySelectionView.getChildren().add(btn);
        }
    }

    public void openChannel(String cityName) {
        if (channelTitleLabel != null) channelTitleLabel.setText(cityName);
        if(chatPopupTitle != null) chatPopupTitle.setText(cityName + " Chat");

        loadTripsForCity(cityName);
    }

    private void updateActiveCityButton(Button activeBtn) {
        for (javafx.scene.Node node : citySelectionView.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-cursor: hand;");
            }
        }
        activeBtn.setStyle("-fx-background-color: #FFCB7B; -fx-background-radius: 20; -fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
    }
    

    private void loadTripsForCity(String city) {
        if (channelTripsContainer == null) return;
        channelTripsContainer.getChildren().clear();

        try {
            Channel speChannel = ChannelList.getChannel(city);
            
            if (speChannel == null && city != null) {
                String formattedName = city.substring(0, 1).toUpperCase() + city.substring(1).toLowerCase();
                speChannel = ChannelList.getChannel(formattedName);
            }

            if (speChannel == null) {
                Label emptyLbl = new Label("No trips found for this city yet.");
                emptyLbl.setStyle("-fx-text-fill: #1E3A5F; -fx-font-size: 16px; -fx-font-weight: bold;");
                channelTripsContainer.getChildren().add(emptyLbl);
                return;
            }

            ArrayList<String> reqs = speChannel.getTripRequests();
            if (reqs != null) {
                for (String req : reqs) {
                    Trip t = TripList.getTrip(req);
                    if (t != null && !t.isFinished()) {
                        User u = UserList.getUser(t.getUser());
                        if (u != null) addTripCard(t, u);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addTripCard(Trip trip, User owner) {
        HBox card = new HBox();
        card.setPrefHeight(180); card.setPrefWidth(800);
        card.setStyle("-fx-background-color: #FFE6CC; -fx-background-radius: 20; -fx-border-color: #1E3A5F; -fx-border-width: 3; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(5, Color.web("#00000033")));
        card.setPadding(new Insets(15));

        VBox infoBox = new VBox(10);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        HBox topRow = new HBox(10); topRow.setAlignment(Pos.CENTER_LEFT);
        Circle profilePic = new Circle(25, Color.LIGHTGRAY);
        setCircleImage(profilePic, owner.getProfile() != null ? owner.getProfile().getProfilePictureUrl() : null);

        VBox nameBox = new VBox();
        Label nameLbl = new Label(owner.getUsername());
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label lvlLbl = new Label("Lvl. " + owner.getLevel());
        lvlLbl.setTextFill(Color.GRAY);
        nameBox.getChildren().addAll(nameLbl, lvlLbl);

        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Button viewProfileBtn = new Button("View Profile");
        addClickEffect(viewProfileBtn);
        viewProfileBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-cursor: hand;");
        viewProfileBtn.setOnAction(e -> switchToOtherProfile(e, owner.getId()));

        topRow.getChildren().addAll(profilePic, nameBox, r, viewProfileBtn);
        
        Label detailLbl = new Label("Departuring from: " + trip.getDepartureLocation() + "\nDates: " + trip.getDepartureDate());
        detailLbl.setFont(Font.font(14));
        
        HBox bottomRow = new HBox(20); bottomRow.setAlignment(Pos.CENTER_LEFT);
        int mateCount = trip.getJoinedMates() != null ? trip.getJoinedMates().size() : 0;
        Label mateLbl = new Label(mateCount + "/" + trip.getMateCount() + " mates");
        mateLbl.setFont(Font.font("System", FontWeight.BOLD, 14));

        Region r2 = new Region(); HBox.setHgrow(r2, Priority.ALWAYS);

        Button detailsBtn = new Button("Details");
        addClickEffect(detailsBtn);
        detailsBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 15; -fx-font-weight: bold; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> openTripDetailsPopup(trip));

        bottomRow.getChildren().addAll(mateLbl, r2, detailsBtn);
        infoBox.getChildren().addAll(topRow, detailLbl, bottomRow);
        card.getChildren().add(infoBox);
        channelTripsContainer.getChildren().add(card);
    }

    private void openTripDetailsPopup(Trip trip) {
        if (tripDetailsPopup == null) return;

        if (detailDestinationLabel != null) detailDestinationLabel.setText(trip.getDestinationName());
        if (detailDescLabel != null) detailDescLabel.setText(trip.getAdditionalNotes() != null ? trip.getAdditionalNotes() : "No description.");
        if (detailDateLabel != null) detailDateLabel.setText(trip.getDepartureDate().toString());
        if (detailBudgetLabel != null) detailBudgetLabel.setText(trip.getAverageBudget() + " $");

        if (mainContainer != null) mainContainer.setEffect(new GaussianBlur(10));
        tripDetailsPopup.setVisible(true);
        tripDetailsPopup.toFront();
    }

    @FXML public void closeTripDetailsPopup() {
        if (mainContainer != null) mainContainer.setEffect(null);
        if (tripDetailsPopup != null) tripDetailsPopup.setVisible(false);
    }
    
    private void setCircleImage(Circle targetCircle, String name) {
        try {
            String path = "/images/" + name + ".png";
            if (getClass().getResource(path) == null) path = "/images/logoBlue.png";
            targetCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream(path))));
        } catch (Exception e) {}
    }

    private void switchToOtherProfile(javafx.event.ActionEvent event, String userID) {
        try {
            User currentUser = UserSession.getCurrentUser();
            if (userID.equals(currentUser.getId())){
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Profile.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/OtherProfile.fxml"));
                javafx.scene.Parent root = loader.load();
                OtherProfileController controller = loader.getController();
                javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                javafx.scene.Scene currentScene = source.getScene();
                controller.setProfileData(currentScene, userID);
                javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
                stage.setScene(new javafx.scene.Scene(root));
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addClickEffect(Button button) {
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setOnMousePressed(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(0.95); st.setToY(0.95); st.play();
        });
        button.setOnMouseReleased(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), button);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }
}