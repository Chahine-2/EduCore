package utils;

import models.Utilisateur;

/**
 * Holds the authenticated user for the current JavaFX session.
 * Set after login; cleared on logout.
 */
public final class UserSession {

    private static Utilisateur currentUser;

    private UserSession() {}

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
