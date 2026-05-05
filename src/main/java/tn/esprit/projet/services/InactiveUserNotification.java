package tn.esprit.projet.services;

import tn.esprit.projet.models.User;
import java.time.LocalDate;

/** Holds one notification entry for the bell panel. */
public record InactiveUserNotification(User user, LocalDate inactiveSince, String lastObjectiveTitle) {}
