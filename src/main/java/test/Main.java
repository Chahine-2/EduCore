package test;

import models.Materiel;
import models.ReservationMateriel;
import services.ServiceMateriel;
import services.ServiceReservationMateriel;
import services.Statistiques;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ServiceMateriel sm = new ServiceMateriel();
        ServiceReservationMateriel sr = new ServiceReservationMateriel();
        Statistiques stats = new Statistiques();
        Scanner scanner = new Scanner(System.in);
        int choix = -1;

        while (choix != 0) {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1. Gestion Materiel");
            System.out.println("2. Gestion Reservation");
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
            System.out.println("\n--- GESTION MATERIEL ---");
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
                    System.out.print("Quantite : ");
                    m.setQuantite(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Etat (disponible/indisponible/maintenance) : ");
                    m.setEtat(scanner.nextLine().trim());
                    sm.add(m);
                    break;
                case 2:
                    List<Materiel> liste = sm.getAll();
                    System.out.println("\n--- Liste des materiels ---");
                    if (liste.isEmpty()) System.out.println("Aucun materiel trouve.");
                    else liste.forEach(System.out::println);
                    break;
                case 3:
                    System.out.print("ID a modifier : ");
                    int idModif = Integer.parseInt(scanner.nextLine().trim());
                    Materiel mModif = sm.getById(idModif);
                    if (mModif == null) { System.out.println("Materiel introuvable !"); break; }
                    System.out.print("Nouveau nom : ");
                    mModif.setNom(scanner.nextLine().trim());
                    System.out.print("Nouveau code : ");
                    mModif.setCode(scanner.nextLine().trim());
                    System.out.print("Nouvelle description : ");
                    mModif.setDescription(scanner.nextLine().trim());
                    System.out.print("Nouvelle quantite : ");
                    mModif.setQuantite(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Nouvel etat (disponible/indisponible/maintenance) : ");
                    mModif.setEtat(scanner.nextLine().trim());
                    sm.update(mModif);
                    break;
                case 4:
                    System.out.print("ID a supprimer : ");
                    int idSupp = Integer.parseInt(scanner.nextLine().trim());
                    sm.delete(idSupp);
                    break;
            }
        }
    }

    static void gestionReservation(ServiceReservationMateriel sr, Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n--- GESTION RESERVATION ---");
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
                    ReservationMateriel r = new ReservationMateriel();
                    System.out.print("ID Materiel : ");
                    r.setMaterielId(Integer.parseInt(scanner.nextLine().trim()));
                    System.out.print("Motif : ");
                    r.setMotif(scanner.nextLine().trim());
                    System.out.print("Date debut (yyyy-MM-ddTHH:mm) : ");
                    r.setDateDebut(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Date fin (yyyy-MM-ddTHH:mm) : ");
                    r.setDateFin(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Statut (en_attente/confirmee/annulee) : ");
                    r.setStatut(scanner.nextLine().trim());
                    sr.add(r);
                    break;
                case 2:
                    List<ReservationMateriel> listeR = sr.getAll();
                    System.out.println("\n--- Liste des reservations ---");
                    if (listeR.isEmpty()) System.out.println("Aucune reservation trouvee.");
                    else listeR.forEach(System.out::println);
                    break;
                case 3:
                    System.out.print("ID a modifier : ");
                    int idModifR = Integer.parseInt(scanner.nextLine().trim());
                    ReservationMateriel rModif = sr.getById(idModifR);
                    if (rModif == null) { System.out.println("Reservation introuvable !"); break; }
                    System.out.print("Nouveau motif : ");
                    rModif.setMotif(scanner.nextLine().trim());
                    System.out.print("Nouvelle date debut (yyyy-MM-ddTHH:mm) : ");
                    rModif.setDateDebut(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Nouvelle date fin (yyyy-MM-ddTHH:mm) : ");
                    rModif.setDateFin(LocalDateTime.parse(scanner.nextLine().trim()));
                    System.out.print("Nouveau statut (en_attente/confirmee/annulee) : ");
                    rModif.setStatut(scanner.nextLine().trim());
                    sr.update(rModif);
                    break;
                case 4:
                    System.out.print("ID a supprimer : ");
                    int idSuppR = Integer.parseInt(scanner.nextLine().trim());
                    sr.delete(idSuppR);
                    break;
            }
        }
    }

    static void gestionStatistiques(Statistiques stats, Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n--- STATISTIQUES ---");
            System.out.println("1. Reservations par materiel");
            System.out.println("2. Materiel le plus demande");
            System.out.println("3. Taux d'occupation");
            System.out.println("4. Materiels par etat");
            System.out.println("5. Reservations du mois courant");
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
}