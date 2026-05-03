package tn.esprit.projet.services;

import tn.esprit.projet.models.User;
import java.time.LocalDate;

public class InactiveUserNotification {
    private User user;
    private LocalDate inactiveSince;
    private String lastObjectiveTitle;

    public InactiveUserNotification(User user, LocalDate inactiveSince, String lastObjectiveTitle) {
        this.user = user;
        this.inactiveSince = inactiveSince;
        this.lastObjectiveTitle = lastObjectiveTitle;
    }

    public User getUser() { return user; }
    public LocalDate getInactiveSince() { return inactiveSince; }
    public String getLastObjectiveTitle() { return lastObjectiveTitle; }
}
