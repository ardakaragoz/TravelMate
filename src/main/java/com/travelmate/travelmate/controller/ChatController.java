package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.ChatRoom;
import com.travelmate.travelmate.model.DirectMessage;
import com.travelmate.travelmate.model.Message;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.session.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatController implements Initializable {

    // --- FXML ELEMENTS ---
    @FXML private VBox chatListContainer;
    @FXML private VBox messageBubbleContainer;
    @FXML private ScrollPane messageScrollPane;
    @FXML private Label currentChatNameLabel;
    @FXML private TextField messageInput;
    @FXML private Label headerUsernameLabel;
    @FXML private ImageView headerProfileImage;

    // --- DATA ---
    private User currentUser;
    private ChatRoom activeChatRoom;

    // Thread-safe collections for concurrent loading
    private final List<ChatRoom> myChats = new CopyOnWriteArrayList<>();
    private final Map<String, User> chatFriends = new ConcurrentHashMap<>();

    // Styling Constants
    private static final String DEFAULT_STYLE = "-fx-background-color: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-border-color: black; -fx-border-radius: 15; -fx-border-width: 2px;";
    private static final String SELECTED_STYLE = "-fx-background-color: #e0f7fa; -fx-background-radius: 15; -fx-cursor: hand; -fx-border-color: #1E2A47; -fx-border-radius: 15; -fx-border-width: 3px;";

    // Track selected row for animation
    private HBox selectedRow = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(() -> {
            try {
                // 1. Get Logged-in User
                currentUser = UserSession.getCurrentUser(); //

                if (currentUser == null) {
                    System.out.println("DEBUG: UserSession is null. Using Test ID.");
                    currentUser = new User("HuZUKiHoRQg7XRkRx5gq");
                }

                Platform.runLater(() -> {
                    if (headerUsernameLabel != null && currentUser.getUsername() != null) {
                        headerUsernameLabel.setText(currentUser.getUsername());
                    }
                    setProfileImage(headerProfileImage, currentUser);
                    loadChatRoomsFast(); // Using the NEW fast loader
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- OPTIMIZATION: PARALLEL LOADING ---
    private void loadChatRoomsFast() {
        // Clear previous data
        myChats.clear();
        chatFriends.clear();

        //
        ArrayList<String> chatIds = currentUser.getChatRooms();

        if (chatIds == null || chatIds.isEmpty()) {
            System.out.println("DEBUG: No chats found.");
            return;
        }

        // List to hold all background tasks
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String chatId : chatIds) {
            // Create a background task for EACH chat
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 1. Fetch Chat Room (Network Call)
                    DirectMessage room = new DirectMessage(chatId); //
                    myChats.add(room);

                    // 2. Fetch Friend User (Network Call)
                    String friendId = getOtherUserId(room);
                    try {
                        User friendUser = new User(friendId); //
                        chatFriends.put(chatId, friendUser);
                    } catch (Exception e) {
                        System.err.println("Could not fetch friend: " + friendId);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading chat: " + chatId);
                }
            });
            futures.add(future);
        }

        // Wait for ALL tasks to finish, then update UI
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(() -> {
                    renderChatList();
                    // Auto-select first chat if available
                    if (!myChats.isEmpty()) {
                        // We need to find the HBox corresponding to the first chat to select it visually
                        if (!chatListContainer.getChildren().isEmpty()) {
                            Node firstRow = chatListContainer.getChildren().get(0);
                            if (firstRow instanceof HBox) {
                                selectChat(myChats.get(0), (HBox) firstRow);
                            }
                        }
                    }
                }));
    }

    private void renderChatList() {
        chatListContainer.getChildren().clear();

        for (ChatRoom room : myChats) {
            User friend = chatFriends.get(room.getId());
            String displayName = (friend != null && friend.getUsername() != null) ? friend.getUsername() : "Unknown";

            // Row Container
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setPrefHeight(60);
            row.setStyle(DEFAULT_STYLE); // Set default style initially

            // Avatar
            ImageView avatar = new ImageView();
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            setProfileImage(avatar, friend);

            // Name
            Label nameLabel = new Label(displayName);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E2A47;");

            // Arrow
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Label arrow = new Label(">");
            arrow.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            row.getChildren().addAll(avatar, nameLabel, spacer, arrow);

            // Click Action: Pass the 'row' itself to handle animation
            row.setOnMouseClicked(e -> selectChat(room, row));

            chatListContainer.getChildren().add(row);
        }
    }

    // --- ANIMATION & SELECTION LOGIC ---
    private void selectChat(ChatRoom room, HBox clickedRow) {
        this.activeChatRoom = room;

        // 1. Reset old selection
        if (selectedRow != null) {
            selectedRow.setStyle(DEFAULT_STYLE);
        }

        // 2. Highlight new selection
        selectedRow = clickedRow;
        if (selectedRow != null) {
            selectedRow.setStyle(SELECTED_STYLE);
        }

        // 3. Update Header Info
        User friend = chatFriends.get(room.getId());
        if (currentChatNameLabel != null && friend != null) {
            currentChatNameLabel.setText(friend.getUsername());
        }

        // 4. Load Messages
        loadMessages(room);
    }

    private void loadMessages(ChatRoom room) {
        new Thread(() -> {
            try {
                ArrayList<Message> messageObjects = new ArrayList<>();
                if (room.getMessages() != null) {
                    for (String msgId : room.getMessages()) {
                        //
                        messageObjects.add(new Message(msgId));
                    }
                }
                Platform.runLater(() -> renderMessages(messageObjects));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderMessages(ArrayList<Message> messages) {
        messageBubbleContainer.getChildren().clear();
        for (Message msg : messages) {
            HBox bubbleRow = new HBox();
            Label text = new Label(msg.getMessage());
            text.setWrapText(true);
            text.setMaxWidth(350);
            text.setPadding(new Insets(10));

            boolean isMe = msg.getSender() != null && msg.getSender().getId().equals(currentUser.getId());

            if (isMe) {
                bubbleRow.setAlignment(Pos.CENTER_RIGHT);
                text.setStyle("-fx-background-color: #1E2A47; -fx-text-fill: white; -fx-background-radius: 15 15 0 15; -fx-font-size: 14px;");
            } else {
                bubbleRow.setAlignment(Pos.CENTER_LEFT);
                text.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 15 15 15 0; -fx-border-color: #ccc; -fx-border-radius: 15 15 15 0; -fx-font-size: 14px;");
            }
            bubbleRow.getChildren().add(text);
            messageBubbleContainer.getChildren().add(bubbleRow);
        }
        messageScrollPane.applyCss();
        messageScrollPane.layout();
        messageScrollPane.setVvalue(1.0);
    }

    // --- HELPER: Safe Image Loading ---
    private void setProfileImage(ImageView imageView, User user) {
        if (imageView == null) return;

        Image image = null;
        try {
            var resource = getClass().getResourceAsStream("/images/user_icon.png");
            if (resource == null) resource = getClass().getResourceAsStream("/images/logoBlue.png");
            if (resource != null) image = new Image(resource);
        } catch (Exception e) { }

        if (user != null && user.getProfile() != null) {
            String url = user.getProfile().getProfilePictureUrl(); //
            if (url != null && !url.isEmpty() && (url.startsWith("http") || url.startsWith("https"))) {
                try {
                    image = new Image(url, true);
                } catch (Exception e) { }
            }
        }

        if (image != null) {
            imageView.setImage(image);
            if (imageView.getClip() == null) {
                double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
                imageView.setClip(new Circle(radius, radius, radius));
            }
        }
    }

    private String getOtherUserId(ChatRoom room) {
        if (room.getActiveUsers() == null) return "Unknown";
        return room.getActiveUsers().stream()
                .filter(id -> !id.equals(currentUser.getId()))
                .findFirst()
                .orElse("Unknown");
    }

    // --- NAVIGATION ---
    @FXML
    private void goToProfile(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Profile.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML private void handleSendMessage() {
        String text = messageInput.getText();
        if (text.isEmpty() || activeChatRoom == null) return;
        messageInput.clear();
        new Thread(() -> {
            try {
                String msgId = "msg_" + System.currentTimeMillis();
                Message newMessage = new Message(msgId, text, currentUser);
                activeChatRoom.addMessage(newMessage);
                currentUser.addMessage(newMessage);

                // Refresh only the message view, keep the selection style
                Platform.runLater(() -> loadMessages(activeChatRoom));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML private void goToHome(javafx.event.ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}