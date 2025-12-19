package com.travelmate.travelmate.session;

import com.travelmate.travelmate.model.User;

public class UserSession {

    private static User currentUser = null;

    public static void setCurrentUser(User user){
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearCurrentUser(){
        currentUser = null;
    }
}
