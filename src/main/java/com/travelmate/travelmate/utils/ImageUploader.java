package com.travelmate.travelmate.utils;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.travelmate.travelmate.firebase.FirebaseService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class ImageUploader {

    public static String uploadProfilePicture(File localFile, String username) {
        try {
            Bucket bucket = FirebaseService.getStorageBucket();
            if (bucket == null) {
                System.err.println("Storage Bucket is null! Check FirebaseService.");
                return null;
            }

            // Create a unique file name
            String fileName = "profile_pics/" + username + "_" + UUID.randomUUID().toString() + ".jpg";

            // Read local file
            FileInputStream content = new FileInputStream(localFile);

            // Upload to Firebase Storage
            Blob blob = bucket.create(fileName, content, "image/jpeg");

            // Make it public so the app can read it via URL
            blob.createAcl(com.google.cloud.storage.Acl.of(com.google.cloud.storage.Acl.User.ofAllUsers(), com.google.cloud.storage.Acl.Role.READER));

            // Return the web link
            return "https://storage.googleapis.com/" + bucket.getName() + "/" + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}