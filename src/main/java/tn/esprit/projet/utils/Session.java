package tn.esprit.projet.utils;

import tn.esprit.projet.models.User;

/**
 * Static session holder for the currently logged-in user.
 */
public class Session {

    private static User currentUser = null;

    public static void login(User user)  { currentUser = user; }
    public static void logout()          { currentUser = null; }
    public static User getCurrentUser()  { return currentUser; }
    public static boolean isLoggedIn()   { return currentUser != null; }
    public static boolean isAdmin()      { return currentUser != null && currentUser.isAdmin(); }
}
