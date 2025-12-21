module com.travelmate.travelmate {
    requires javafx.controls;
    requires javafx.fxml;


    requires firebase.admin;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires google.cloud.core;
    requires com.google.api.apicommon;
    requires javafx.graphics;
    requires java.desktop;
    requires com.google.gson;

    opens com.travelmate.travelmate.model to google.cloud.firestore;
    opens com.travelmate.travelmate to javafx.fxml, javafx.graphics, com.google.gson;
    opens com.travelmate.travelmate.controller to javafx.fxml;
    opens com.travelmate.travelmate.session to com.google.gson;

    exports com.travelmate.travelmate;
}