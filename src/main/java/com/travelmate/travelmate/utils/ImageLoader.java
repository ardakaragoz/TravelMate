package com.travelmate.travelmate.utils;

import com.travelmate.travelmate.model.User;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    // Cache to store loaded images in RAM
    private static final Map<String, Image> imageCache = Collections.synchronizedMap(new HashMap<>());
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Loads a user's profile picture into a Circle (Safe).
     */
    public static void loadForUser(User user, Circle target) {
        if (target == null) return;

        // 1. Set Placeholder immediately (Gray circle)
        target.setFill(Color.LIGHTGRAY);

        if (user == null) return;

        executor.submit(() -> {
            try {
                // 2. Fetch Image (Background Thread)
                Image image = getOrLoadImage(user);

                // 3. Update UI (JavaFX Thread)
                Platform.runLater(() -> {
                    // CRITICAL FIX: Check for null BEFORE creating ImagePattern
                    if (image != null && !image.isError()) {
                        target.setFill(new ImagePattern(image));
                    }
                });
            } catch (Exception e) {
                System.err.println("Safe Catch: Failed to render image for user " + user.getUsername());
            }
        });
    }

    /**
     * Loads a user's profile picture into an ImageView (Safe).
     */
    public static void loadForUser(User user, ImageView target) {
        if (target == null || user == null) return;

        executor.submit(() -> {
            Image image = getOrLoadImage(user);
            Platform.runLater(() -> {
                if (image != null && !image.isError()) {
                    target.setImage(image);
                }
            });
        });
    }

    private static Image getOrLoadImage(User user) {
        String cloudUrl = user.getProfilePicture();
        String username = user.getUsername();

        // 1. Try Cloud URL (Only if valid HTTP link)
        if (cloudUrl != null && !cloudUrl.trim().isEmpty()) {
            if (cloudUrl.startsWith("http")) {
                if (imageCache.containsKey(cloudUrl)) return imageCache.get(cloudUrl);
                try {
                    Image img = new Image(cloudUrl, true); // Load in background
                    imageCache.put(cloudUrl, img);
                    return img;
                } catch (Exception e) {
                    System.err.println("Invalid Cloud URL: " + cloudUrl);
                }
            } else if (cloudUrl.startsWith("gs://")) {
                System.err.println("Warning: Skipping 'gs://' URL. ImageUploader should save 'https://' URLs instead.");
            }
        }

        // 2. Try Local File (Fallback)
        String localKey = "local_" + username;
        if (imageCache.containsKey(localKey)) return imageCache.get(localKey);

        try {
            String cleanName = (username != null) ? username.toLowerCase().replace("ı", "i") : "user_icon";
            String path = "/images/" + cleanName + ".png";
            URL url = ImageLoader.class.getResource(path);

            // Try specific user image
            if (url != null) {
                Image img = new Image(url.toExternalForm());
                imageCache.put(localKey, img);
                return img;
            }
        } catch (Exception ignored) {}

        // 3. Default Icon (Last Resort)
        if (imageCache.containsKey("default")) return imageCache.get("default");

        try {
            URL defaultUrl = ImageLoader.class.getResource("/images/user_icon.png");
            if (defaultUrl != null) {
                Image img = new Image(defaultUrl.toExternalForm());
                imageCache.put("default", img);
                return img;
            }
        } catch (Exception e) {
            System.err.println("CRITICAL: user_icon.png not found in resources!");
        }

        return null; // Safe to return null, loadForUser handles it.
    }
}