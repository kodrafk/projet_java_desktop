package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour gérer le calendrier des événements
 * Fournit des méthodes pour organiser et filtrer les événements par date
 */
public class CalendrierService {
    
    private final EvenementService evenementService;
    
    public CalendrierService() {
        this.evenementService = new EvenementService();
    }
    
    /**
     * Récupère tous les événements d'un mois donné
     */
    public List<Evenement> getEvenementsDuMois(YearMonth mois) {
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();
        
        return evenementService.getAll().stream()
            .filter(e -> {
                LocalDate dateEvent = e.getDate_debut().toLocalDate();
                return !dateEvent.isBefore(debut) && !dateEvent.isAfter(fin);
            })
            .sorted(Comparator.comparing(Evenement::getDate_debut))
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les événements d'une date spécifique
     */
    public List<Evenement> getEvenementsDuJour(LocalDate date) {
        return evenementService.getAll().stream()
            .filter(e -> e.getDate_debut().toLocalDate().equals(date))
            .sorted(Comparator.comparing(Evenement::getDate_debut))
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les événements à venir (futurs)
     */
    public List<Evenement> getEvenementsAvenir() {
        LocalDateTime maintenant = LocalDateTime.now();
        return evenementService.getAll().stream()
            .filter(e -> e.getDate_debut().isAfter(maintenant))
            .sorted(Comparator.comparing(Evenement::getDate_debut))
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les événements passés
     */
    public List<Evenement> getEvenementsPasses() {
        LocalDateTime maintenant = LocalDateTime.now();
        return evenementService.getAll().stream()
            .filter(e -> e.getDate_debut().isBefore(maintenant))
            .sorted(Comparator.comparing(Evenement::getDate_debut).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les événements de la semaine courante
     */
    public List<Evenement> getEvenementsSemaine() {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate debutSemaine = aujourdhui.minusDays(aujourdhui.getDayOfWeek().getValue() - 1);
        LocalDate finSemaine = debutSemaine.plusDays(6);
        
        return evenementService.getAll().stream()
            .filter(e -> {
                LocalDate dateEvent = e.getDate_debut().toLocalDate();
                return !dateEvent.isBefore(debutSemaine) && !dateEvent.isAfter(finSemaine);
            })
            .sorted(Comparator.comparing(Evenement::getDate_debut))
            .collect(Collectors.toList());
    }
    
    /**
     * Compte le nombre d'événements par jour pour un mois donné
     * Retourne une Map<LocalDate, Integer>
     */
    public Map<LocalDate, Integer> getCompteurEvenementsMois(YearMonth mois) {
        List<Evenement> evenements = getEvenementsDuMois(mois);
        Map<LocalDate, Integer> compteur = new HashMap<>();
        
        for (Evenement e : evenements) {
            LocalDate date = e.getDate_debut().toLocalDate();
            compteur.put(date, compteur.getOrDefault(date, 0) + 1);
        }
        
        return compteur;
    }
    
    /**
     * Vérifie si une date a des événements
     */
    public boolean aDesEvenements(LocalDate date) {
        return evenementService.getAll().stream()
            .anyMatch(e -> e.getDate_debut().toLocalDate().equals(date));
    }
    
    /**
     * Récupère les événements entre deux dates
     */
    public List<Evenement> getEvenementsEntreDates(LocalDate debut, LocalDate fin) {
        return evenementService.getAll().stream()
            .filter(e -> {
                LocalDate dateEvent = e.getDate_debut().toLocalDate();
                return !dateEvent.isBefore(debut) && !dateEvent.isAfter(fin);
            })
            .sorted(Comparator.comparing(Evenement::getDate_debut))
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les prochains N événements
     */
    public List<Evenement> getProchainsEvenements(int nombre) {
        return getEvenementsAvenir().stream()
            .limit(nombre)
            .collect(Collectors.toList());
    }
    
    /**
     * Recherche des événements par mot-clé dans le mois
     */
    public List<Evenement> rechercherEvenementsMois(YearMonth mois, String motCle) {
        String recherche = motCle.toLowerCase().trim();
        return getEvenementsDuMois(mois).stream()
            .filter(e -> 
                e.getNom().toLowerCase().contains(recherche) ||
                e.getLieu().toLowerCase().contains(recherche) ||
                e.getCoach_name().toLowerCase().contains(recherche) ||
                (e.getDescription() != null && e.getDescription().toLowerCase().contains(recherche))
            )
            .collect(Collectors.toList());
    }
}
