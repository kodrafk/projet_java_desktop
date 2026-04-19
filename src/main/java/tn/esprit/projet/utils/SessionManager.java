package tn.esprit.projet.utils;

import tn.esprit.projet.models.User;

/**
 * Manages the currently logged-in user.
 * Supports both static access (SessionManager.getCurrentUser())
 * and legacy instance access (SessionManager.getInstance().getCurrentUser()).
 */
public class SessionManager {

    // ── Static state ───────────────────────────────────────────────────────────
    private static User currentUser;

    // ── Static API (preferred) ─────────────────────────────────────────────────
    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser()          { return currentUser; }
    public static boolean isLoggedIn()           { return currentUser != null; }
    public static void logout()                  { currentUser = null; }
    public static boolean isAdmin() {
        return currentUser != null && "ROLE_ADMIN".equals(currentUser.getRoles());
    }

    // ── Legacy singleton API (for backward compatibility) ──────────────────────
    private static final SessionManager INSTANCE = new SessionManager();
    private SessionManager() {}

    public static SessionManager getInstance() { return INSTANCE; }

    // Instance methods delegate to static state
    public User getUser()                        { return currentUser; }
    public void setUser(User user)               { currentUser = user; }
    public boolean loggedIn()                    { return currentUser != null; }
    public boolean admin()                       { return isAdmin(); }
    public void logOut()                         { currentUser = null; }
}
