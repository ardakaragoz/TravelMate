module com.travelmate.travelmate {
    requires javafx.controls;
    requires javafx.fxml;


    requires firebase.admin;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires google.cloud.core;
    requires com.google.api.apicommon;
    requires java.sql;
    requires javafx.graphics;

    opens com.travelmate.travelmate.model to google.cloud.firestore;
    opens com.travelmate.travelmate to javafx.fxml, javafx.graphics;
    opens com.travelmate.travelmate.controller to javafx.fxml;

    exports com.travelmate.travelmate;
}