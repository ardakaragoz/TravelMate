# TravelMate
CS102 Project aims to help finding travel mates and facilitate travel experiences for travel enthusiasts

## About Project

### Collaborators: 
- Ahmet Arda Karagöz (22403308)
- İbrahim Karakuş (22403627)
- Atakan Polat (22403521)
- Berken Keni (22402686)
---
### Key Features:
- Login and register screens (frontend in progress)
- Creating trip requests (model-level design completed)
- City-based channel structure
- Messaging and recommendation systems
- Review system for users
- Basic Firebase Firestore connection for storing data
---

### Architecture

The project follows the **Model–View–Controller (MVC)** pattern.

- **Model:** Core data classes such as `User`, `Channel`, `Trip`, `Message`, `Review`, etc.
- **View:** Graphical user interface implemented using JavaFX (FXML + CSS)
- **Controller:** Handles user interactions and connects the UI to the model and database

Firestore communication is handled through simple Java service classes.

---
### Technologies Used

- Java
- JavaFX
- Maven
- Firebase Firestore (Cloud NoSQL Database)
- JSON (for storing city, hobby, trip type values)

---

### How to Run

1. Clone or download the project.
2. Open it using IntelliJ IDEA.
3. Add Firestore API Key in src/main/resources/firebase/ as serviceAccountKey.json
4. Wait for Maven dependencies to be downloaded.
5. Run UtilDatabase.java ONLY ONCE when initializing application with your Firebase API Key.
6. Run the Main.java in IntelliJ IDEA.
---
### Task Distribution

- <b>Ahmet Arda Karagöz:</b> He will be responsible for everything related to Firebase and the database. He will handle the controller layer and most of the application algorithms. Additionally, he will design and implement the formula required for calculating the compatibility score between users. 
- <b>İbrahim Karakuş:</b> He will design the JavaFX user interface using SceneBuilder in accordance with the UI Design Report. He will create the required components and UI interactions on the design side (without backend integration). 
- <b>Atakan Polat:</b> He will assist İbrahim on the front-end side and will be responsible for implementing scene transitions and organizing the layout of the scenes. Additionally, he will fully develop the front-end part of the login–register screens (excluding the database integration).
- <b>Berken Keni:</b> He will collect city images and other necessary media/data. He will generate test user data for development and testing purposes. He will also be responsible for implementing the core model classes. 
