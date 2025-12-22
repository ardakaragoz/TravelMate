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
                System.err.println("Storage Bucket is null. Cannot upload.");
                return null;
            }

            // Generate clean filename
            String fileName = "profile_pics/" + username + "_" + UUID.randomUUID() + ".jpg";
            FileInputStream content = new FileInputStream(localFile);

            // Upload
            Blob blob = bucket.create(fileName, content, "image/jpeg");

            // Make Public
            blob.createAcl(com.google.cloud.storage.Acl.of(com.google.cloud.storage.Acl.User.ofAllUsers(), com.google.cloud.storage.Acl.Role.READER));

            // Return HTTPS URL
            return "https://storage.googleapis.com/" + bucket.getName() + "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}