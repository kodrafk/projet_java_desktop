package tn.esprit.projet.services;

import java.util.Random;

/**
 * Service intelligent pour générer des images de sponsors
 * Utilise source.unsplash.com avec mots-cles specifiques a chaque marque/secteur
 * Format: https://source.unsplash.com/400x300/?keyword1,keyword2
 */
public class SponsorImageService {

    /**
     * Genere intelligemment une URL d image pour un sponsor
     * selon son nom de marque et son type de partenariat
     */
    public static String genererImageSponsor(String nomSponsor, String type) {
        if (nomSponsor == null || nomSponsor.trim().isEmpty()) {
            return getImageParDefaut();
        }

        String nom = nomSponsor.toLowerCase().trim();
        System.out.println("Recherche image pour: " + nomSponsor + " (type: " + type + ")");

        // ═══ MARQUES SPORTIVES ═══
        if (nom.contains("adidas")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?adidas,sneakers",
                "https://source.unsplash.com/400x300/?adidas,sport,shoes",
                "https://source.unsplash.com/400x300/?adidas,athletic,wear",
                "https://source.unsplash.com/400x300/?adidas,running,shoes",
                "https://source.unsplash.com/400x300/?adidas,football,sport"
            }, nomSponsor);
        }
        if (nom.contains("nike")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?nike,sneakers",
                "https://source.unsplash.com/400x300/?nike,sport,shoes",
                "https://source.unsplash.com/400x300/?nike,running,athlete",
                "https://source.unsplash.com/400x300/?nike,basketball,shoes",
                "https://source.unsplash.com/400x300/?nike,athletic,training"
            }, nomSponsor);
        }
        if (nom.contains("puma")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?puma,sport,shoes",
                "https://source.unsplash.com/400x300/?puma,athletic,sneakers",
                "https://source.unsplash.com/400x300/?sport,running,shoes",
                "https://source.unsplash.com/400x300/?athletic,footwear,sport",
                "https://source.unsplash.com/400x300/?sport,training,shoes"
            }, nomSponsor);
        }
        if (nom.contains("reebok")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?reebok,fitness,shoes",
                "https://source.unsplash.com/400x300/?crossfit,training,shoes",
                "https://source.unsplash.com/400x300/?fitness,athletic,shoes",
                "https://source.unsplash.com/400x300/?gym,training,footwear",
                "https://source.unsplash.com/400x300/?sport,fitness,training"
            }, nomSponsor);
        }
        if (nom.contains("under armour")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?athletic,performance,sport",
                "https://source.unsplash.com/400x300/?fitness,training,gear",
                "https://source.unsplash.com/400x300/?sport,performance,wear",
                "https://source.unsplash.com/400x300/?athlete,training,sport",
                "https://source.unsplash.com/400x300/?gym,performance,fitness"
            }, nomSponsor);
        }
        if (nom.contains("new balance")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?running,shoes,sport",
                "https://source.unsplash.com/400x300/?sneakers,running,athletic",
                "https://source.unsplash.com/400x300/?marathon,running,shoes",
                "https://source.unsplash.com/400x300/?jogging,sport,shoes",
                "https://source.unsplash.com/400x300/?running,athlete,shoes"
            }, nomSponsor);
        }

        // ═══ MARQUES AUTOMOBILES ═══
        if (nom.contains("bmw")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?bmw,luxury,car",
                "https://source.unsplash.com/400x300/?bmw,automobile,sport",
                "https://source.unsplash.com/400x300/?bmw,car,german",
                "https://source.unsplash.com/400x300/?bmw,vehicle,luxury",
                "https://source.unsplash.com/400x300/?bmw,sports,car"
            }, nomSponsor);
        }
        if (nom.contains("mercedes")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?mercedes,luxury,car",
                "https://source.unsplash.com/400x300/?mercedes,benz,automobile",
                "https://source.unsplash.com/400x300/?mercedes,car,elegant",
                "https://source.unsplash.com/400x300/?mercedes,vehicle,premium",
                "https://source.unsplash.com/400x300/?mercedes,sedan,luxury"
            }, nomSponsor);
        }
        if (nom.contains("audi")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?audi,car,luxury",
                "https://source.unsplash.com/400x300/?audi,automobile,sport",
                "https://source.unsplash.com/400x300/?audi,vehicle,german",
                "https://source.unsplash.com/400x300/?audi,car,premium",
                "https://source.unsplash.com/400x300/?audi,sports,car"
            }, nomSponsor);
        }
        if (nom.contains("toyota")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?toyota,car,reliable",
                "https://source.unsplash.com/400x300/?toyota,automobile,japan",
                "https://source.unsplash.com/400x300/?toyota,vehicle,modern",
                "https://source.unsplash.com/400x300/?toyota,car,family",
                "https://source.unsplash.com/400x300/?toyota,hybrid,car"
            }, nomSponsor);
        }
        if (nom.contains("volkswagen") || nom.contains("vw")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?volkswagen,car,german",
                "https://source.unsplash.com/400x300/?vw,automobile,europe",
                "https://source.unsplash.com/400x300/?volkswagen,vehicle,modern",
                "https://source.unsplash.com/400x300/?car,german,engineering",
                "https://source.unsplash.com/400x300/?volkswagen,car,quality"
            }, nomSponsor);
        }
        if (nom.contains("ferrari")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?ferrari,sports,car",
                "https://source.unsplash.com/400x300/?ferrari,red,supercar",
                "https://source.unsplash.com/400x300/?ferrari,racing,car",
                "https://source.unsplash.com/400x300/?supercar,luxury,speed",
                "https://source.unsplash.com/400x300/?ferrari,italian,car"
            }, nomSponsor);
        }

        // ═══ MARQUES TECHNOLOGIE ═══
        if (nom.contains("apple")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?apple,iphone,technology",
                "https://source.unsplash.com/400x300/?apple,macbook,laptop",
                "https://source.unsplash.com/400x300/?apple,ipad,device",
                "https://source.unsplash.com/400x300/?apple,technology,minimal",
                "https://source.unsplash.com/400x300/?apple,smartphone,modern"
            }, nomSponsor);
        }
        if (nom.contains("samsung")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?samsung,smartphone,technology",
                "https://source.unsplash.com/400x300/?samsung,galaxy,phone",
                "https://source.unsplash.com/400x300/?samsung,electronics,modern",
                "https://source.unsplash.com/400x300/?samsung,screen,technology",
                "https://source.unsplash.com/400x300/?samsung,device,innovation"
            }, nomSponsor);
        }
        if (nom.contains("google")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?google,technology,search",
                "https://source.unsplash.com/400x300/?google,android,phone",
                "https://source.unsplash.com/400x300/?google,cloud,technology",
                "https://source.unsplash.com/400x300/?google,innovation,tech",
                "https://source.unsplash.com/400x300/?google,pixel,smartphone"
            }, nomSponsor);
        }
        if (nom.contains("microsoft")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?microsoft,windows,technology",
                "https://source.unsplash.com/400x300/?microsoft,office,software",
                "https://source.unsplash.com/400x300/?microsoft,surface,laptop",
                "https://source.unsplash.com/400x300/?microsoft,cloud,azure",
                "https://source.unsplash.com/400x300/?microsoft,technology,modern"
            }, nomSponsor);
        }
        if (nom.contains("huawei")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?huawei,smartphone,technology",
                "https://source.unsplash.com/400x300/?huawei,phone,modern",
                "https://source.unsplash.com/400x300/?smartphone,technology,china",
                "https://source.unsplash.com/400x300/?mobile,technology,innovation",
                "https://source.unsplash.com/400x300/?huawei,device,tech"
            }, nomSponsor);
        }

        // ═══ MARQUES ALIMENTATION / BOISSONS ═══
        if (nom.contains("coca") || nom.contains("cola")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?coca-cola,drink,refreshing",
                "https://source.unsplash.com/400x300/?cola,beverage,cold",
                "https://source.unsplash.com/400x300/?soda,drink,refreshing",
                "https://source.unsplash.com/400x300/?cold,drink,beverage",
                "https://source.unsplash.com/400x300/?refreshing,drink,summer"
            }, nomSponsor);
        }
        if (nom.contains("pepsi")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?pepsi,drink,soda",
                "https://source.unsplash.com/400x300/?pepsi,beverage,cold",
                "https://source.unsplash.com/400x300/?soda,drink,blue",
                "https://source.unsplash.com/400x300/?cold,beverage,refreshing",
                "https://source.unsplash.com/400x300/?drink,soda,refreshing"
            }, nomSponsor);
        }
        if (nom.contains("red bull")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?energy,drink,sport",
                "https://source.unsplash.com/400x300/?redbull,energy,extreme",
                "https://source.unsplash.com/400x300/?energy,drink,athlete",
                "https://source.unsplash.com/400x300/?extreme,sport,energy",
                "https://source.unsplash.com/400x300/?energy,boost,sport"
            }, nomSponsor);
        }
        if (nom.contains("monster")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?energy,drink,green",
                "https://source.unsplash.com/400x300/?monster,energy,sport",
                "https://source.unsplash.com/400x300/?energy,extreme,sport",
                "https://source.unsplash.com/400x300/?energy,drink,extreme",
                "https://source.unsplash.com/400x300/?sport,energy,power"
            }, nomSponsor);
        }
        if (nom.contains("mcdonald") || nom.contains("mc donald")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?mcdonalds,burger,fast-food",
                "https://source.unsplash.com/400x300/?hamburger,fast,food",
                "https://source.unsplash.com/400x300/?burger,restaurant,food",
                "https://source.unsplash.com/400x300/?fast,food,restaurant",
                "https://source.unsplash.com/400x300/?burger,meal,food"
            }, nomSponsor);
        }
        if (nom.contains("kfc")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?kfc,chicken,fast-food",
                "https://source.unsplash.com/400x300/?fried,chicken,food",
                "https://source.unsplash.com/400x300/?chicken,restaurant,food",
                "https://source.unsplash.com/400x300/?fast,food,chicken",
                "https://source.unsplash.com/400x300/?crispy,chicken,meal"
            }, nomSponsor);
        }
        if (nom.contains("protein") || nom.contains("whey") || nom.contains("supplement")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?protein,supplement,fitness",
                "https://source.unsplash.com/400x300/?protein,powder,gym",
                "https://source.unsplash.com/400x300/?nutrition,supplement,sport",
                "https://source.unsplash.com/400x300/?fitness,nutrition,health",
                "https://source.unsplash.com/400x300/?gym,supplement,muscle"
            }, nomSponsor);
        }

        // ═══ BANQUES ET FINANCE TUNISIENNES ═══
        if (nom.contains("biat")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?bank,finance,tunisia",
                "https://source.unsplash.com/400x300/?banking,business,finance",
                "https://source.unsplash.com/400x300/?bank,building,finance",
                "https://source.unsplash.com/400x300/?finance,investment,bank",
                "https://source.unsplash.com/400x300/?banking,corporate,finance"
            }, nomSponsor);
        }
        if (nom.contains("attijari")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?bank,finance,business",
                "https://source.unsplash.com/400x300/?banking,investment,finance",
                "https://source.unsplash.com/400x300/?finance,corporate,bank",
                "https://source.unsplash.com/400x300/?bank,money,finance",
                "https://source.unsplash.com/400x300/?banking,professional,finance"
            }, nomSponsor);
        }
        if (nom.contains("stb") || nom.contains("amen") || nom.contains("bh bank")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?bank,finance,professional",
                "https://source.unsplash.com/400x300/?banking,business,corporate",
                "https://source.unsplash.com/400x300/?finance,bank,investment",
                "https://source.unsplash.com/400x300/?corporate,finance,bank",
                "https://source.unsplash.com/400x300/?bank,building,professional"
            }, nomSponsor);
        }

        // ═══ SECTEURS GENERIQUES (par mots-cles dans le nom) ═══
        if (nom.contains("sport") || nom.contains("fitness") || nom.contains("gym")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?sport,fitness,gym",
                "https://source.unsplash.com/400x300/?fitness,training,sport",
                "https://source.unsplash.com/400x300/?gym,workout,fitness",
                "https://source.unsplash.com/400x300/?sport,athlete,training",
                "https://source.unsplash.com/400x300/?fitness,health,sport"
            }, nomSponsor);
        }
        if (nom.contains("food") || nom.contains("nutrition") || nom.contains("health")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?healthy,food,nutrition",
                "https://source.unsplash.com/400x300/?nutrition,health,food",
                "https://source.unsplash.com/400x300/?healthy,eating,food",
                "https://source.unsplash.com/400x300/?food,nutrition,wellness",
                "https://source.unsplash.com/400x300/?healthy,meal,nutrition"
            }, nomSponsor);
        }
        if (nom.contains("tech") || nom.contains("digital") || nom.contains("soft")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?technology,digital,innovation",
                "https://source.unsplash.com/400x300/?tech,software,digital",
                "https://source.unsplash.com/400x300/?digital,innovation,tech",
                "https://source.unsplash.com/400x300/?technology,computer,modern",
                "https://source.unsplash.com/400x300/?software,technology,digital"
            }, nomSponsor);
        }
        if (nom.contains("bank") || nom.contains("banque") || nom.contains("finance")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?bank,finance,business",
                "https://source.unsplash.com/400x300/?finance,investment,money",
                "https://source.unsplash.com/400x300/?banking,corporate,finance",
                "https://source.unsplash.com/400x300/?business,finance,professional",
                "https://source.unsplash.com/400x300/?bank,money,investment"
            }, nomSponsor);
        }
        if (nom.contains("car") || nom.contains("auto") || nom.contains("motor")) {
            return choisir(new String[]{
                "https://source.unsplash.com/400x300/?car,automobile,modern",
                "https://source.unsplash.com/400x300/?auto,vehicle,car",
                "https://source.unsplash.com/400x300/?car,motor,vehicle",
                "https://source.unsplash.com/400x300/?automobile,car,road",
                "https://source.unsplash.com/400x300/?vehicle,car,modern"
            }, nomSponsor);
        }

        // ═══ FALLBACK: image par type de partenariat ═══
        return genererParType(type, nomSponsor);
    }

    /**
     * Genere une image selon le type de partenariat
     */
    private static String genererParType(String type, String nomSponsor) {
        if (type == null) type = "standard";
        switch (type.toLowerCase().trim()) {
            case "gold":
            case "or":
                return choisir(new String[]{
                    "https://source.unsplash.com/400x300/?gold,luxury,premium",
                    "https://source.unsplash.com/400x300/?golden,award,luxury",
                    "https://source.unsplash.com/400x300/?gold,business,premium",
                    "https://source.unsplash.com/400x300/?luxury,gold,elegant",
                    "https://source.unsplash.com/400x300/?premium,gold,award"
                }, nomSponsor);
            case "silver":
            case "argent":
                return choisir(new String[]{
                    "https://source.unsplash.com/400x300/?silver,business,modern",
                    "https://source.unsplash.com/400x300/?silver,technology,modern",
                    "https://source.unsplash.com/400x300/?silver,elegant,business",
                    "https://source.unsplash.com/400x300/?modern,business,silver",
                    "https://source.unsplash.com/400x300/?silver,corporate,professional"
                }, nomSponsor);
            case "bronze":
                return choisir(new String[]{
                    "https://source.unsplash.com/400x300/?bronze,award,business",
                    "https://source.unsplash.com/400x300/?copper,warm,business",
                    "https://source.unsplash.com/400x300/?bronze,medal,award",
                    "https://source.unsplash.com/400x300/?warm,business,award",
                    "https://source.unsplash.com/400x300/?bronze,corporate,award"
                }, nomSponsor);
            case "platinum":
            case "platine":
                return choisir(new String[]{
                    "https://source.unsplash.com/400x300/?platinum,luxury,premium",
                    "https://source.unsplash.com/400x300/?premium,luxury,white",
                    "https://source.unsplash.com/400x300/?platinum,elegant,luxury",
                    "https://source.unsplash.com/400x300/?luxury,premium,elegant",
                    "https://source.unsplash.com/400x300/?platinum,award,premium"
                }, nomSponsor);
            default:
                return choisir(new String[]{
                    "https://source.unsplash.com/400x300/?business,partnership,professional",
                    "https://source.unsplash.com/400x300/?corporate,business,team",
                    "https://source.unsplash.com/400x300/?partnership,business,success",
                    "https://source.unsplash.com/400x300/?professional,business,modern",
                    "https://source.unsplash.com/400x300/?business,corporate,success"
                }, nomSponsor);
        }
    }

    /**
     * Choisit une URL parmi un tableau de facon deterministe selon le nom
     * (meme nom = meme image, mais images differentes entre marques)
     */
    private static String choisir(String[] urls, String nomSponsor) {
        int index = Math.abs(nomSponsor.hashCode()) % urls.length;
        System.out.println("   Image choisie [" + index + "]: " + urls[index]);
        return urls[index];
    }

    /**
     * Image par defaut variee
     */
    public static String getImageParDefaut() {
        String[] defaut = {
            "https://source.unsplash.com/400x300/?business,partnership",
            "https://source.unsplash.com/400x300/?corporate,professional",
            "https://source.unsplash.com/400x300/?business,success,team",
            "https://source.unsplash.com/400x300/?professional,business",
            "https://source.unsplash.com/400x300/?partnership,corporate"
        };
        return defaut[new Random().nextInt(defaut.length)];
    }
}
