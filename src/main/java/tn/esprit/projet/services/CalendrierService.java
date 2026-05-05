package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class CalendrierService {
    
    private final EvenementService evenementService;
    
    public CalendrierService() {
        this.evenementService = new EvenementService();
    }
    
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
    
    public List<Evenement> getEvenementsDuJour(LocalDate date) {
        return evenementService.getAll().stream()
            .filter(e -> e.getDate_debut().toLocalDate().equals(date))
            .sorted(Comparator.comparing(Evenement::getDate_debut))
            .collect(Collectors.toList());
    }
    
    public List<Evenement> getEvenementsAvenir() {
        LocalDateTime maintenant = LocalDateTime.now();
        return evenementService.getAll().stream()
            .filter(e -> e.getDate_debut().isAfter(maintenant))
            .sorted(Comparator.comparing(Evenement::getDate_debut))
            .collect(Collectors.toList());
    }
}
