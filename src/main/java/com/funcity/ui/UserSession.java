package com.funcity.ui;

import com.funcity.model.User;

public class UserSession {

    private static volatile User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isStaff() {
        return currentUser != null && currentUser.isStaff();
    }

    public static boolean isTechnician() {
        return currentUser != null && currentUser.isTechnician();
    }
}

