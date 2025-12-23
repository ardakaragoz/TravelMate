package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Admin;
import com.travelmate.travelmate.model.Recommendation;
import com.travelmate.travelmate.session.RecommendationList;
import com.travelmate.travelmate.session.UserList;
import com.travelmate.travelmate.session.UserSession;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class AdminPageController {
    @FXML private SidebarController sidebarController;
    @FXML private VBox approvalsContainer;
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActivePage("Admin");
        }
        loadPendingApprovals();
    }
    private void loadPendingApprovals() {
        if (approvalsContainer == null) return;
        for (String recoID : RecommendationList.recommendations.keySet()){
            Recommendation rec = RecommendationList.recommendations.get(recoID);
            if (Objects.equals(rec.getStatus(), "PENDING")){
                createApprovalCard(
                        rec,
                        rec.getChannel(),
                        UserList.getUser(rec.getSender()).getUsername(),
                        rec.getMessage()
                );
            }
        }

    }
    private void createApprovalCard(Recommendation rec, String channelName, String recommenderName, String content) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: #1E3A5F; -fx-border-width: 2; -fx-border-radius: 15;");
        card.setPadding(new Insets(20));
        card.setEffect(new DropShadow(5, Color.rgb(0,0,0,0.1)));
        card.setAlignment(Pos.CENTER_LEFT);

        
        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label headerLbl = new Label("for " + channelName + " Channel");
        headerLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        headerLbl.setTextFill(Color.GRAY);

        Label userLbl = new Label(recommenderName + " Recommended This:");
        userLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        userLbl.setTextFill(Color.web("#1E3A5F"));

        Label msgLbl = new Label(content);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        contentBox.getChildren().addAll(headerLbl, userLbl, msgLbl);

        
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMinWidth(120);

        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.setStyle("-fx-background-color: #253A63; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;");
        viewProfileBtn.setMaxWidth(Double.MAX_VALUE);

        Button approveBtn = new Button("APPROVE");
        approveBtn.setStyle("-fx-background-color: #CCFF00; -fx-text-fill: #1E3A5F; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #000000; -fx-border-radius: 10;");
        approveBtn.setMaxWidth(Double.MAX_VALUE);

        Button denyBtn = new Button("DENY");
        denyBtn.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #000000; -fx-border-radius: 10;");
        denyBtn.setMaxWidth(Double.MAX_VALUE);

        viewProfileBtn.setOnAction(e -> {
            switchToOtherProfile(e, rec.getSender());
        });

        approveBtn.setOnAction(e -> {
            System.out.println("Approved recommendation for " + channelName);
            try {
                Admin user = new Admin(UserSession.getCurrentUser().getId());
                user.acceptRecommendation(rec);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            approvalsContainer.getChildren().remove(card); 
        });

        denyBtn.setOnAction(e -> {
            System.out.println("Denied recommendation for " + channelName);
            try {
                Admin user = new Admin(UserSession.getCurrentUser().getId());
                user.rejectRecommendation(rec);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            approvalsContainer.getChildren().remove(card); 
        });

        buttonBox.getChildren().addAll(viewProfileBtn, approveBtn, denyBtn);

        card.getChildren().addAll(contentBox, buttonBox);

        
        approvalsContainer.getChildren().add(card);
    }
    private void switchToOtherProfile(javafx.event.ActionEvent event, String userID) {
        try {
            if (userID.equals(UserSession.getCurrentUser().getId())){
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

        } catch (java.io.IOException e) { e.printStackTrace(); } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}