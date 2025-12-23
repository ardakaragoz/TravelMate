package com.travelmate.travelmate.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
        createApprovalCard(
                "Paris",
                "Atakan Polat",
                "If you're visiting France, be sure to add the Musée d'Orsay to your list. Housed in a stunning former railway station, the museum offers an unforgettable collection of Impressionist art."
        );
        createApprovalCard(
                "Tokyo",
                "Placide Zigira",
                "Don't miss the Shibuya Crossing at night! It's chaotic but beautiful."
        );
    }
    private void createApprovalCard(String channelName, String recommenderName, String content) {
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

        
        approveBtn.setOnAction(e -> {
            System.out.println("Approved recommendation for " + channelName);
            approvalsContainer.getChildren().remove(card); 
        });

        denyBtn.setOnAction(e -> {
            System.out.println("Denied recommendation for " + channelName);
            approvalsContainer.getChildren().remove(card); 
        });

        buttonBox.getChildren().addAll(viewProfileBtn, approveBtn, denyBtn);

        card.getChildren().addAll(contentBox, buttonBox);

        
        approvalsContainer.getChildren().add(card);
    }
}