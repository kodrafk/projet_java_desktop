package tn.esprit.projet.services;

import java.util.Random;

public class SponsorImageService {

    public static String genererImageSponsor(String nomSponsor, String type) {
        return "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=400&h=300&fit=crop&q=80";
    }

    public static String getImageParDefaut() {
        return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=300&fit=crop&q=80";
    }
}
