package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.session.HobbyList;
import com.travelmate.travelmate.session.TripTypeList;
import com.travelmate.travelmate.session.UserSession;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.model.Hobby;
import com.travelmate.travelmate.model.TripTypes;

import com.travelmate.travelmate.utils.ImageLoader;
import com.travelmate.travelmate.utils.ImageUploader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EditProfileController {

    @FXML private Circle profileImageCircle;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextArea bioArea;

    // --- HOBİ SEÇİM ALANLARI ---
    @FXML private ComboBox<String> hobbyComboBox;       // Tüm hobilerin olduğu kutu
    @FXML private ListView<String> selectedHobbiesListView; // Seçilenlerin listelendiği yer

    // --- GEZİ TÜRÜ SEÇİM ALANLARI ---
    @FXML private ComboBox<String> tripTypeComboBox;    // Tüm gezi türlerinin olduğu kutu
    @FXML private ListView<String> selectedTripTypesListView; // Seçilenlerin listelendiği yer
    private File selectedImageFile;
    private User currentUser;

    public void initialize() {
        // 1. Kutuları doldur (JSON veya DB'den gelen verilerle)
        populateComboBoxes();

        // 2. Mevcut kullanıcı verilerini ekrana bas
        try {
            User currentUser = UserSession.getCurrentUser();
            if (currentUser != null) {
                String name = currentUser.getName() != null ? currentUser.getName() : "";

                if (fullNameField != null) fullNameField.setText(name);
                if (usernameField != null) usernameField.setText(currentUser.getUsername());

                // Eğer User modelinde 'getBiography' varsa:
                // if (bioArea != null) bioArea.setText(currentUser.getProfile().getBiography());
                if (bioArea != null) bioArea.setText(currentUser.getProfile().getBiography()); // Placeholder

                ImageLoader.loadForUser(currentUser, profileImageCircle);

                // --- MEVCUT HOBİLERİ YÜKLE ---
                // Kullanıcının daha önce kaydettiği hobileri varsa ListView'e ekle
                if (currentUser.getProfile() != null) {
                    // Not: Profil sınıfındaki getHobbies() metodun ArrayList<Hobby> dönüyorsa isimlerini almalısın
                    // Örnek kullanım:

                    try {
                        ArrayList<Hobby> myHobbies = currentUser.getProfile().getHobbies();
                        for (Hobby h : myHobbies) {
                            selectedHobbiesListView.getItems().add(h.getName());
                        }
                    } catch (Exception e) { e.printStackTrace(); }

                }

                // --- MEVCUT GEZİ TÜRLERİNİ YÜKLE ---

                if (currentUser.getProfile() != null) {
                    ArrayList<TripTypes> myTypes = currentUser.getProfile().getFavoriteTripTypes();
                    for (TripTypes t : myTypes) selectedTripTypesListView.getItems().add(t.getName());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void populateComboBoxes() {
        // --- HOBİLERİ YÜKLE ---
        if (HobbyList.hobbies.isEmpty()) {
            HobbyList.loadAllHobbies();
        }
        if (hobbyComboBox != null) {
            hobbyComboBox.getItems().setAll(HobbyList.hobbies.keySet());
        }

        // --- GEZİ TÜRLERİNİ YÜKLE ---
        if (TripTypeList.triptypes.isEmpty()) {
            TripTypeList.listAllTripTypes();
        }
        if (tripTypeComboBox != null) {
            tripTypeComboBox.getItems().setAll(TripTypeList.triptypes.keySet());
        }
    }

    // =========================================================
    // HOBİ EKLEME / ÇIKARMA İŞLEMLERİ
    // =========================================================

    @FXML
    public void handleAddHobby(ActionEvent event) {
        String selected = hobbyComboBox.getValue();
        // Null değilse ve listede zaten yoksa ekle
        if (selected != null && !selectedHobbiesListView.getItems().contains(selected)) {
            selectedHobbiesListView.getItems().add(selected);
        }
    }

    @FXML
    public void handleRemoveHobby(ActionEvent event) {
        String selectedToRemove = selectedHobbiesListView.getSelectionModel().getSelectedItem();
        if (selectedToRemove != null) {
            selectedHobbiesListView.getItems().remove(selectedToRemove);
        }
    }

    // =========================================================
    // GEZİ TÜRÜ EKLEME / ÇIKARMA İŞLEMLERİ
    // =========================================================

    @FXML
    public void handleAddTripType(ActionEvent event) {
        String selected = tripTypeComboBox.getValue();
        if (selected != null && !selectedTripTypesListView.getItems().contains(selected)) {
            selectedTripTypesListView.getItems().add(selected);
        }
    }

    @FXML
    public void handleRemoveTripType(ActionEvent event) {
        String selectedToRemove = selectedTripTypesListView.getSelectionModel().getSelectedItem();
        if (selectedToRemove != null) {
            selectedTripTypesListView.getItems().remove(selectedToRemove);
        }
    }

    // =========================================================
    // KAYDETME VE ÇIKIŞ İŞLEMLERİ
    // =========================================================

    @FXML
    public void handleUploadPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            this.selectedImageFile = file;
            // Preview locally immediately
            profileImageCircle.setFill(new ImagePattern(new Image(file.toURI().toString())));
        }
    }

    @FXML
    public void handleSaveButton(ActionEvent event) {
        if (currentUser == null) return;
        System.out.println("Saving profile...");

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Upload Image
                if (selectedImageFile != null) {
                    String cloudUrl = ImageUploader.uploadProfilePicture(selectedImageFile, currentUser.getUsername());
                    if (cloudUrl != null) {
                        // Update User (Memory + DB)
                        //currentUser.setProfilePicture(cloudUrl);
                        // Update Profile (Memory + DB)
                        currentUser.getProfile().setProfilePictureUrl(cloudUrl);
                        // Update Cache
                        ImageLoader.loadForUser(currentUser, profileImageCircle);
                    }
                }

                // 2. Save Text
                if (bioArea != null) {
                    currentUser.getProfile().setBiography(bioArea.getText());
                }

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Profile Updated!");
                    alert.showAndWait();
                    handleCancelButton(event);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void handleCancelButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Profile.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}