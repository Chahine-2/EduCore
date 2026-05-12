package test;

import models.Materiel;
import models.Reservation;
import services.Statistiques;
import services.ServiceMateriel;
import services.ServiceReservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ServiceMateriel sm = new ServiceMateriel();
        ServiceReservation sr = new ServiceReservation();
        Statistiques stats = new Statistiques();
        Scanner scanner = new Scanner(System.in);
        int choix = -1;

        while (choix != 0) {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1. Gestion Matériel");
            System.out.println("2. Gestion Réservation");
            System.out.println("3. Statistiques");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");

            try {
                choix = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrez un nombre valide !");
                continue;
            }

            switch (choix) {
                case 1: gestionMateriel(sm, scanner); break;
                case 2: gestionReservation(sr, scanner); break;
                case 3: gestionStatistiques(stats, scanner); break;
                case 0: System.out.println("Au revoir !"); break;
                default: System.out.println("Choix invalide !");
            }
        }
        scanner.close();
    }

    static void gestionMateriel(ServiceMateriel sm, Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n--- GESTION MATÉRIEL ---");
            System.out.println("1. Ajouter");
            System.out.println("2. Afficher");
            System.out.println("3. Modifier");
            System.out.println("4. Supprimer");
            System.out.println("0. Retour");
            System.out.print("Votre choix : ");
            try {
                choix = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrez un nombre valide !");
                continue;
            }
            switch (choix) {
                case 1:
                    Materiel m = new Materiel();
                    System.out.print("Nom : ");
                    m.setNom(scanner.nextLine().trim());
                    System.out.print("Code : ");
                    m.setCode(scanner.nextLine().trim());
                    System.out.print("Description : ");
                    m.setDescription(scanner.nextLine().trim());
                    System.out.print("Quantité : ");
                    m.setQuantite(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("État (disponible/indisponible/maintenance) : ");
                    m.setEtat(scanner.nextLine().trim());
                    sm.add(m);
                    break;
                case 2:
                    List<Materiel> liste = sm.getAll();
                    System.out.println("\n--- Liste des matériels ---");
                    if (liste.isEmpty()) System.out.println("Aucun matériel trouvé.");
                    else liste.forEach(System.out::println);
                    break;
                case 3:
                    System.out.print("ID à modifier : ");
                    Materiel mModif = new Materiel();
                    mModif.setId(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Nouveau nom : ");
                    mModif.setNom(scanner.nextLine().trim());
                    System.out.print("Nouveau code : ");
                    mModif.setCode(scanner.nextLine().trim());
                    System.out.print("Nouvelle description : ");
                    mModif.setDescription(scanner.nextLine().trim());
                    System.out.print("Nouvelle quantité : ");
                    mModif.setQuantite(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Nouvel état (disponible/indisponible/maintenance) : ");
                    mModif.setEtat(scanner.nextLine().trim());
                    sm.update(mModif);
                    break;
                case 4:
                    System.out.print("ID à supprimer : ");
                    Materiel mSupp = new Materiel();
                    mSupp.setId(Integer.parseInt(scanner.nextLine().trim()));
                    sm.delete(mSupp);
                    break;
            }
        }
    }

    static void gestionReservation(ServiceReservation sr, Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n--- GESTION RÉSERVATION ---");
            System.out.println("1. Ajouter");
            System.out.println("2. Afficher");
            System.out.println("3. Modifier");
            System.out.println("4. Supprimer");
            System.out.println("0. Retour");
            System.out.print("Votre choix : ");
            try {
                choix = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrez un nombre valide !");
                continue;
            }
            switch (choix) {
                case 1:
                    Reservation r = new Reservation();
                    System.out.print("ID Matériel : ");
                    r.setMaterielId(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Motif : ");
                    r.setMotif(scanner.nextLine().trim());
                    System.out.print("Date début (yyyy-MM-ddTHH:mm) ex: 2026-05-10T09:00 : ");
                    r.setDateDebut(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Date fin   (yyyy-MM-ddTHH:mm) ex: 2026-05-10T11:00 : ");
                    r.setDateFin(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Statut (en_attente/confirmee/annulee) : ");
                    r.setStatut(scanner.nextLine().trim());
                    sr.add(r);
                    break;
                case 2:
                    List<Reservation> listeR = sr.getAll();
                    System.out.println("\n--- Liste des réservations ---");
                    if (listeR.isEmpty()) System.out.println("Aucune réservation trouvée.");
                    else listeR.forEach(System.out::println);
                    break;
                case 3:
                    System.out.print("ID à modifier : ");
                    Reservation rModif = new Reservation();
                    rModif.setId(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Nouvel ID Matériel : ");
                    rModif.setMaterielId(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Nouveau motif : ");
                    rModif.setMotif(scanner.nextLine().trim());
                    System.out.print("Nouvelle date début (yyyy-MM-ddTHH:mm) : ");
                    rModif.setDateDebut(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Nouvelle date fin (yyyy-MM-ddTHH:mm) : ");
                    rModif.setDateFin(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Nouveau statut (en_attente/confirmee/annulee) : ");
                    rModif.setStatut(scanner.nextLine().trim());
                    sr.update(rModif);
                    break;
                case 4:
                    System.out.print("ID à supprimer : ");
                    Reservation rSupp = new Reservation();
                    rSupp.setId(Integer.parseInt(scanner.nextLine().trim()));
                    sr.delete(rSupp);
                    break;
            }
        }
    }

    static void gestionStatistiques(Statistiques stats, Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n--- STATISTIQUES ---");
            System.out.println("1. Réservations par matériel");
            System.out.println("2. Matériel le plus demandé");
            System.out.println("3. Taux d'occupation");
            System.out.println("4. Matériels par état");
            System.out.println("5. Réservations du mois courant");
            System.out.println("0. Retour");
            System.out.print("Votre choix : ");
            try {
                choix = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrez un nombre valide !");
                continue;
            }
            switch (choix) {
                case 1: stats.reservationsParMateriel(); break;
                case 2: stats.materielPlusDemande(); break;
                case 3: stats.tauxOccupation(); break;
                case 4: stats.materielsParEtat(); break;
                case 5: stats.reservationsMoisCourant(); break;
                case 0: break;
                default: System.out.println("Choix invalide !");
            }
        }
    }
}{}