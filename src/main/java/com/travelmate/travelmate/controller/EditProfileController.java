package com.travelmate.travelmate.controller;

import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.HobbyList;
import com.travelmate.travelmate.session.TripTypeList;
import com.travelmate.travelmate.session.UserSession;
import com.travelmate.travelmate.model.User;
import com.travelmate.travelmate.model.Hobby;
import com.travelmate.travelmate.model.TripTypes;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;


import javafx.scene.control.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EditProfileController {

    @FXML private Circle profileImageCircle;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextArea bioArea;

    @FXML private ComboBox<String> hobbyComboBox;
    @FXML private ListView<String> selectedHobbiesListView;

    @FXML private ComboBox<String> tripTypeComboBox;
    @FXML private ListView<String> selectedTripTypesListView;
    private File selectedImageFile;
    private User currentUser;

    public void initialize() {
        currentUser = UserSession.getCurrentUser();
        populateComboBoxes();

        try {
            User currentUser = UserSession.getCurrentUser();
            if (currentUser != null) {
                String name = currentUser.getName() != null ? currentUser.getName() : "";

                if (fullNameField != null) fullNameField.setText(name);
                if (usernameField != null) usernameField.setText(currentUser.getUsername());

                if (bioArea != null) bioArea.setText(currentUser.getProfile().getBiography());
                if (currentUser.getProfile() != null) {
                    try {
                        ArrayList<Hobby> myHobbies = currentUser.getProfile().getHobbies();
                        for (Hobby h : myHobbies) {
                            selectedHobbiesListView.getItems().add(h.getName());
                        }
                    } catch (Exception e) { e.printStackTrace(); }

                }


                if (currentUser.getProfile() != null) {
                    ArrayList<TripTypes> myTypes = currentUser.getProfile().getFavoriteTripTypes();
                    for (TripTypes t : myTypes) selectedTripTypesListView.getItems().add(t.getName());
                }
                loadProfileImage(currentUser);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfileImage(User user) {
        if (profileImageCircle == null) return;
        new Thread(() -> {
            Image imageToSet = null;
            try {
                if (user.getProfile() != null) {
                    String rawUrl = user.getProfile().getProfilePictureUrl();
                    String secureUrl = formatToHttps(rawUrl);
                    if (secureUrl != null && !secureUrl.isEmpty()) {
                        imageToSet = new Image(secureUrl, false);
                    }
                }
                if (imageToSet == null || imageToSet.isError()) {
                    var resource = getClass().getResourceAsStream("/images/user_icons/img.png");
                    if (resource != null) imageToSet = new Image(resource);
                }
            } catch (Exception e) { System.out.println("EditProfile Image Error: " + e.getMessage()); }

            if (imageToSet != null) {
                final Image finalImg = imageToSet;
                Platform.runLater(() -> profileImageCircle.setFill(new ImagePattern(finalImg)));
            }
        }).start();
    }

    private String formatToHttps(String gsUrl) {
        if (gsUrl == null || gsUrl.isEmpty()) return null;
        if (gsUrl.startsWith("http")) return gsUrl;
        try {
            if (gsUrl.startsWith("gs://")) {
                String cleanPath = gsUrl.substring(5);
                int bucketSeparator = cleanPath.indexOf('/');
                if (bucketSeparator != -1) {
                    String bucket = cleanPath.substring(0, bucketSeparator);
                    String path = cleanPath.substring(bucketSeparator + 1);
                    String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                    return "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + encodedPath + "?alt=media";
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @FXML
    public void handleUploadPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            this.selectedImageFile = file;

        }
    }
    
    private void populateComboBoxes() {
        if (HobbyList.hobbies.isEmpty()) {
            HobbyList.loadAllHobbies();
        }
        if (hobbyComboBox != null) {
            hobbyComboBox.getItems().setAll(HobbyList.hobbies.keySet());
        }

        if (TripTypeList.triptypes.isEmpty()) {
            TripTypeList.listAllTripTypes();
        }
        if (tripTypeComboBox != null) {
            tripTypeComboBox.getItems().setAll(TripTypeList.triptypes.keySet());
        }
    }

    @FXML
    public void handleAddHobby(ActionEvent event) {
        String selected = hobbyComboBox.getValue();
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

    @FXML
    public void handleSaveButton(ActionEvent event) {
        System.out.println("Kaydet butonuna basıldı...");
        try {
            User currentUser = UserSession.getCurrentUser();
            String username = (String) usernameField.getText();
            String fullName = (String) fullNameField.getText();
            String newBio = bioArea.getText();
            List<String> finalHobbies = new ArrayList<>(selectedHobbiesListView.getItems());
            List<String> finalTripTypes = new ArrayList<>(selectedTripTypesListView.getItems());

            System.out.println("Bio: " + newBio);
            System.out.println("Seçilen Hobiler: " + finalHobbies);
            System.out.println("Seçilen Gezi Türleri: " + finalTripTypes);

            if (currentUser != null && currentUser.getProfile() != null) {
                if (selectedImageFile != null) {
                    uploadImageFixedName(selectedImageFile);
                }
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

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        changeScene("/view/Profile.fxml", event);
    }



    private void uploadImageFixedName(File file) throws IOException {
        Bucket bucket = FirebaseService.getStorageBucket();
        if (bucket == null) {
            System.err.println("Error: Firebase Storage Bucket is null.");
            return;
        }

        String blobName = "profile_images/" + currentUser.getId() + ".png";

        try (FileInputStream fis = new FileInputStream(file)) {
            Blob blob = bucket.create(blobName, fis, "image/png");

            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

            String publicUrl = "https://storage.googleapis.com/" + bucket.getName() + "/" + blobName + "?t=" + System.currentTimeMillis();

            System.out.println("Uploaded & Overwritten: " + publicUrl);

            currentUser.setProfilePicture(publicUrl);
            if (currentUser.getProfile() != null) {
                currentUser.getProfile().setProfilePictureUrl(publicUrl);
            }
        }
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