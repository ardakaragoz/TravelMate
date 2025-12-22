package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.session.HobbyList;
import com.travelmate.travelmate.session.TripTypeList;
import com.travelmate.travelmate.session.UserSession;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.model.Hobby;
import com.travelmate.travelmate.model.TripTypes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public void handleSaveButton(ActionEvent event) {
        System.out.println("Kaydet butonuna basıldı...");
        try {
            User currentUser = UserSession.getCurrentUser();
            String username = (String) usernameField.getText();
            String fullName = (String) fullNameField.getText();
            // 1. Yeni verileri al
            String newBio = bioArea.getText();
            List<String> finalHobbies = new ArrayList<>(selectedHobbiesListView.getItems());
            List<String> finalTripTypes = new ArrayList<>(selectedTripTypesListView.getItems());

            System.out.println("Bio: " + newBio);
            System.out.println("Seçilen Hobiler: " + finalHobbies);
            System.out.println("Seçilen Gezi Türleri: " + finalTripTypes);

            if (currentUser != null && currentUser.getProfile() != null) {
                // 2. Profil nesnesini güncelle
                currentUser.getProfile().setBiography(newBio);
                currentUser.setName(fullName);
                currentUser.setUsername(username);
                currentUser.updateUser();
                currentUser.getProfile().resetHobby();
                currentUser.getProfile().resetTripType();
                for (String finalHobby : finalHobbies){
                    currentUser.getProfile().addHobby(HobbyList.getHobby(finalHobby));
                }
                for (String finalTripType : finalTripTypes){
                    currentUser.getProfile().addTripType(TripTypeList.getTripType(finalTripType));
                }
                currentUser.getProfile().updateHobby_TripType();

                // NOT: Profile.java içinde setHobbies(List<String> names) gibi bir metodun olmalı.
                // Eğer yoksa, Hobby nesnelerine çevirip eklemen gerekebilir.
                // Örn: currentUser.getProfile().updateHobbiesFromNames(finalHobbies);

                // Şimdilik konsola bastık, User/Profile sınıflarında ilgili setter'ları açmalısın.
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // İşlem bitince Profil sayfasına dön
        changeScene("/view/Profile.fxml", event);
    }

    @FXML
    public void handleCancelButton(ActionEvent event) {
        changeScene("/view/Profile.fxml", event);
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