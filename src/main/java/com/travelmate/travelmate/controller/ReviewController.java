package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.model.Review;
import com.travelmate.travelmate.model.User;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

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
    @FXML private Label commentCount;
    private Scene previousScene;

    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActivePage("Reviews");
        }
    }

    public void setReviewsContext(Scene prevScene, User user) throws ExecutionException, InterruptedException {
        this.previousScene = prevScene;
        if (reviewsTitleLabel != null) {
            reviewsTitleLabel.setText("REVIEWS OF " + user.getUsername().toUpperCase());
        }

        double point1 = 0;
        double point2 = 0;
        double point3 = 0;
        double point4 = 0;
        double point5 = 0;
        double point6 = 0;
        ArrayList<Review> reviewsList = new ArrayList<>();
        for (String reviewNo : user.getReviews()){
            Review review = new Review(reviewNo);
            point1 += review.getFriendlinessPoint();
            point2 += review.getReliabilityPoint();
            point3 += review.getCommunicationPoint();
            point4 += review.getAdaptationPoint();
            point5 += review.getBudgetPoint();
            point6 += review.getHelpfulnessPoint();
            reviewsList.add(review);
        }
        point1 /= reviewsList.size();
        point2 /= reviewsList.size();
        point3 /= reviewsList.size();
        point4 /= reviewsList.size();
        point5 /= reviewsList.size();
        point6 /= reviewsList.size();
        commentCount.setText("View Comments (" + reviewsList.size() + ")");
        renderSummaryStars(point1, point2, point3, point4, point5, point6);
        loadReviews(reviewsList);
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

    private void loadReviews(ArrayList<Review> reviews) throws ExecutionException, InterruptedException {
        if (commentsContainer == null) return;
        commentsContainer.getChildren().clear();

        for (Review review : reviews) {
            addReviewCard(review.getEvaluatorUser().getName(), "Travelled " + review.getTrip().getDestination(), review.getTrip().getDepartureDate().toString(), review.getComments(), review.getOverallPoints(), new double[]{review.getFriendlinessPoint(), review.getReliabilityPoint(), review.getCommunicationPoint(), review.getAdaptationPoint(), review.getBudgetPoint(), review.getHelpfulnessPoint()});
        }
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