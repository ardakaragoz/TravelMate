package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.ChatRoom;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML private VBox chatListContainer;
    @FXML private VBox messageBubbleContainer;
    @FXML private ScrollPane messageScrollPane;
    @FXML private Label currentChatNameLabel;
    @FXML private TextField messageInput;
    @FXML private Label headerUsernameLabel;

    private User currentUser;
    private ChatRoom activeChatRoom;
    private ArrayList<ChatRoom> myChats = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(() -> {
            try {
                // 1. Fetch the Logged-in User
                // Use your specific test ID from the previous prompt
                currentUser = UserSession.getCurrentUser();


                Platform.runLater(() -> {
                    if (headerUsernameLabel != null) headerUsernameLabel.setText(currentUser.getUsername());
                    loadChatRooms();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadChatRooms() {
        new Thread(() -> {
            try {
                myChats.clear();
                // Get List of Strings (IDs) from User
                ArrayList<String> chatIds = currentUser.getChatRooms();

                if (chatIds != null) {
                    for (String chatId : chatIds) {
                        try {
                            // Fetch ChatRoom object using ID
                            myChats.add(new ChatRoom(chatId, "private"));
                        } catch (Exception e) {
                            System.err.println("Skipping invalid chat ID: " + chatId);
                        }
                    }
                }

                Platform.runLater(() -> {
                    renderChatList();
                    if (!myChats.isEmpty()) selectChat(myChats.get(0));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderChatList() {
        chatListContainer.getChildren().clear();

        for (ChatRoom room : myChats) {
            // Logic: Find the ID in 'activeUsers' that is NOT me
            String otherUserId = "Unknown";
            if (room.getActiveUsers() != null) {
                otherUserId = room.getActiveUsers().stream()
                        .filter(id -> !id.equals(currentUser.getId()))
                        .findFirst()
                        .orElse("Unknown");
            }

            // Fetch the Friend's Name
            String displayName = "User";
            try {
                User otherUser = new User(otherUserId);
                if (otherUser.getUsername() != null) displayName = otherUser.getUsername();
            } catch (Exception e) { /* ID might be invalid */ }

            // --- UI Row Setup ---
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setPrefHeight(60);
            row.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-border-color: black; -fx-border-radius: 15; -fx-border-width: 2px;");

            ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/images/user_icon.png")));
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            avatar.setClip(new Circle(20, 20, 20));

            Label nameLabel = new Label(displayName);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E2A47;");

            Label arrow = new Label(">");
            arrow.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            row.getChildren().addAll(avatar, nameLabel, spacer, arrow);
            row.setOnMouseClicked(e -> selectChat(room));
            chatListContainer.getChildren().add(row);
        }
    }

    private void selectChat(ChatRoom room) {
        this.activeChatRoom = room;

        // Update Header
        new Thread(() -> {
            try {
                String otherId = room.getActiveUsers().stream()
                        .filter(id -> !id.equals(currentUser.getId()))
                        .findFirst().orElse("Unknown");
                User u = new User(otherId);
                Platform.runLater(() -> currentChatNameLabel.setText(u.getUsername()));
            } catch (Exception e) {}
        }).start();

        // Load Messages
        new Thread(() -> {
            try {
                ArrayList<Message> messageObjects = new ArrayList<>();
                if (room.getMessages() != null) {
                    for (String msgId : room.getMessages()) {
                        // Fetch Message (which fetches Sender User ID -> Sender User Object)
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

            boolean isMe = false;
            if (msg.getSender() != null) {
                isMe = msg.getSender().getId().equals(currentUser.getId());
            }

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

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText();
        if (text.isEmpty() || activeChatRoom == null) return;
        messageInput.clear();

        new Thread(() -> {
            try {
                String msgId = "msg_" + System.currentTimeMillis();
                // Create Message (Now saves ID correctly)
                Message newMessage = new Message(msgId, text, currentUser);

                // Update Lists
                activeChatRoom.addMessage(newMessage);
                currentUser.addMessage(newMessage);

                Platform.runLater(() -> selectChat(activeChatRoom));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void goToHome(javafx.event.ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}