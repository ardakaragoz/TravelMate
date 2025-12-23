package com.travelmate.travelmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;

public class ReviewController {

    @FXML private SidebarController sidebarController;
    @FXML private VBox commentsContainer;

    @FXML private HBox starRatingContainer;
    @FXML private Label averageRatingLabel;
    @FXML private Label reviewsTitleLabel;

    
    @FXML private HBox friendlinessStars;
    @FXML private HBox reliabilityStars;
    @FXML private HBox communicationStars;
    @FXML private HBox adaptationStars;
    @FXML private HBox budgetStars;
    @FXML private HBox helpfulnessStars;

    
    @FXML private Label friendlinessVal;
    @FXML private Label reliabilityVal;
    @FXML private Label communicationVal;
    @FXML private Label adaptationVal;
    @FXML private Label budgetVal;
    @FXML private Label helpfulnessVal;

    private Scene previousScene;

    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActivePage("Reviews");
        }
    }

    public void setReviewsContext(Scene prevScene, String username) {
        this.previousScene = prevScene;
        if (reviewsTitleLabel != null) {
            reviewsTitleLabel.setText("REVIEWS OF " + username.toUpperCase());
        }

        
        renderSummaryStars(4.7, 4.2, 3.8, 4.5, 2.5, 4.9);
        loadReviews();
    }

    
    private void renderSummaryStars(double f, double r, double c, double a, double b, double h) {
        
        fillStars(friendlinessStars, f);
        fillStars(reliabilityStars, r);
        fillStars(communicationStars, c);
        fillStars(adaptationStars, a);
        fillStars(budgetStars, b);
        fillStars(helpfulnessStars, h);

        
        if(friendlinessVal != null) friendlinessVal.setText(String.format("%.1f", f));
        if(reliabilityVal != null) reliabilityVal.setText(String.format("%.1f", r));
        if(communicationVal != null) communicationVal.setText(String.format("%.1f", c));
        if(adaptationVal != null) adaptationVal.setText(String.format("%.1f", a));
        if(budgetVal != null) budgetVal.setText(String.format("%.1f", b));
        if(helpfulnessVal != null) helpfulnessVal.setText(String.format("%.1f", h));

        
        double overall = (f + r + c + a + b + h) / 6.0;
        if(averageRatingLabel != null) averageRatingLabel.setText(String.format("%.1f", overall));
        if(starRatingContainer != null) fillStars(starRatingContainer, overall);
    }

    
    private void fillStars(HBox container, double score) {
        if (container == null) return;
        container.getChildren().clear();

        for (int i = 1; i <= 5; i++) {
            double fillAmount = 0.0;
            if (score >= i) {
                fillAmount = 1.0; 
            } else if (score > i - 1) {
                fillAmount = score - (i - 1); 
            }
            container.getChildren().add(createStarNode(fillAmount));
        }
    }

    
    private StackPane createStarNode(double fillPercentage) {
        StackPane stack = new StackPane();
        stack.setPrefSize(24, 24);

        
        Label grayStar = new Label("★");
        grayStar.setFont(Font.font("Arial", FontWeight.BOLD, 22)); 
        grayStar.setStyle("-fx-text-fill: #BDC3C7;"); 

        
        Label goldStar = new Label("★");
        goldStar.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        goldStar.setStyle("-fx-text-fill: #FFD700;"); 

        
        Rectangle clip = new Rectangle(0, 0, 0, 24);
        clip.setWidth(24 * fillPercentage); 
        goldStar.setClip(clip);

        stack.getChildren().addAll(grayStar, goldStar);
        return stack;
    }

    @FXML
    public void handleBackButton(ActionEvent event) {
        try {
            if (previousScene != null) {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(previousScene);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadReviews() {
        if (commentsContainer == null) return;
        commentsContainer.getChildren().clear();

        
        addReviewCard("Arda Karagöz", "Travelled Dubai", "05-05-2024", "Great trip experience!", 4.5, new double[]{5, 4, 5, 4, 3, 5});
        addReviewCard("Placide Zigira", "Travelled Istanbul", "23-06-2025", "Very reliable mate.", 4.0, new double[]{4, 5, 4, 3, 4, 4});
    }

    private void addReviewCard(String name, String subInfo, String date, String comment, double userScore, double[] details) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: #1E3A5F; -fx-border-width: 1; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setEffect(new DropShadow(5, Color.rgb(0,0,0,0.1)));

        
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle profilePic = new Circle(25, Color.LIGHTGRAY);
        setCircleImage(profilePic, name);

        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(name); nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16)); nameLbl.setTextFill(Color.web("#1E3A5F"));
        Label subLbl = new Label(subInfo); subLbl.setTextFill(Color.GRAY); subLbl.setFont(Font.font("System", 12));
        nameBox.getChildren().addAll(nameLbl, subLbl);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        
        HBox scoreBox = new HBox(8);
        scoreBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        
        Label scoreLbl = new Label(String.valueOf(userScore));
        scoreLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        scoreLbl.setTextFill(Color.web("#1E3A5F"));

        
        Label singleStar = new Label("★");
        singleStar.setFont(Font.font("System", FontWeight.BOLD, 20));
        singleStar.setStyle("-fx-text-fill: #FFD700;"); 

        
        Button infoBtn = new Button("i");
        infoBtn.setStyle("-fx-background-color: #CCFF00; -fx-background-radius: 50; -fx-min-width: 25px; -fx-min-height: 25px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #000000; -fx-border-radius: 50;");

        scoreBox.getChildren().addAll(scoreLbl, singleStar, infoBtn);
        header.getChildren().addAll(profilePic, nameBox, spacer, scoreBox);

        
        Label commentLbl = new Label(comment); commentLbl.setWrapText(true); commentLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Label dateLbl = new Label("Travel Date: " + date); dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 12px;");

        
        VBox detailBox = new VBox(5);
        detailBox.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10; -fx-padding: 10;");
        detailBox.setVisible(false);
        detailBox.setManaged(false);

        
        String[] criteria = {"Friendliness", "Reliability", "Communication", "Adaptation", "Budget", "Helpfulness"};
        for (int i = 0; i < criteria.length; i++) {
            HBox row = new HBox(10);
            Label cLbl = new Label(criteria[i]); cLbl.setPrefWidth(100); cLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            HBox stars = new HBox(1);
            fillStars(stars, details[i]);
            stars.setScaleX(0.7); stars.setScaleY(0.7);

            row.getChildren().addAll(cLbl, stars);
            detailBox.getChildren().add(row);
        }

        
        infoBtn.setOnAction(e -> {
            boolean isVisible = detailBox.isVisible();
            detailBox.setVisible(!isVisible);
            detailBox.setManaged(!isVisible);
        });

        card.getChildren().addAll(header, commentLbl, dateLbl, detailBox);
        commentsContainer.getChildren().add(card);
    }

    private void setCircleImage(Circle targetCircle, String name) {
        try {
            String cleanName = name.split(" ")[0].toLowerCase();
            String path = "/images/" + cleanName + ".png";
            if (getClass().getResource(path) == null) path = "/images/user_icon.png";
            if (getClass().getResource(path) != null) {
                targetCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream(path))));
            }
        } catch (Exception e) {}
    }
}