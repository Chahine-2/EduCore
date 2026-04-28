import interfaces.IUtilisateurService;
import models.Etudiant;
import models.Role;
import models.Utilisateur;
import services.UtilisateurService;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        IUtilisateurService service = new UtilisateurService();

        System.out.println("=======================================");
        System.out.println("        BIENVENUE SUR EDUCORE          ");
        System.out.println("=======================================\n");

        // --- 1. PHASE DE CONNEXION ---
        System.out.print("Entrez votre email : ");
        String email = scanner.nextLine();

        System.out.print("Entrez votre mot de passe : ");
        String password = scanner.nextLine();

        System.out.println("\nTentative de connexion...\n");
        Utilisateur user = service.authentifier(email, password);

        // Si l'utilisateur n'existe pas ou mot de passe incorrect
        if (user == null) {
            System.out.println("❌ ÉCHEC : Email ou mot de passe incorrect.");
            System.exit(0);
        }

        // --- 2. ROUTAGE INTELLIGENT BASÉ SUR LE RÔLE ---
        String roleName = user.getRole().getNomRole();

        if (roleName.equals("Etudiant")) {
            System.out.println("✅ Connexion réussie !");
            System.out.println("Bienvenue, " + user.getPrenom() + ". Vous êtes connecté en tant qu'Étudiant.");
            // Later, you can add student-specific CLI options here (like viewing grades)
        }
        else if (roleName.equals("Administrateur")) {
            System.out.println("✅ Connexion réussie ! Bienvenue Admin " + user.getPrenom() + ".");
            // Launch the CRUD menu for the admin
            afficherMenuAdmin(scanner, service);
        }
        else if (roleName.equals("Enseignant")) {
            System.out.println("✅ Connexion réussie !");
            System.out.println("Bienvenue, " + user.getPrenom() + ". Vous êtes connecté en tant qu'Enseignant.");
        }
        else {
            System.out.println("Rôle non reconnu.");
        }

        System.out.println("\nFermeture de l'application. À bientôt !");
        scanner.close();
    }

    // --- 3. MENU CRUD ADMINISTRATEUR ---
    private static void afficherMenuAdmin(Scanner scanner, IUtilisateurService service) {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n=======================================");
            System.out.println("          PANNEAU ADMINISTRATEUR       ");
            System.out.println("=======================================");
            System.out.println("1. Lister tous les utilisateurs (READ)");
            System.out.println("2. Ajouter un étudiant (CREATE)");
            System.out.println("3. Modifier l'email d'un utilisateur (UPDATE)");
            System.out.println("4. Supprimer un utilisateur (DELETE)");
            System.out.println("0. Se déconnecter");
            System.out.print("Choix : ");

            int choix = scanner.nextInt();
            scanner.nextLine(); // Consommer le saut de ligne

            switch (choix) {
                case 1:
                    System.out.println("\n--- LISTE DES UTILISATEURS ---");
                    List<Utilisateur> users = service.listerUtilisateurs();
                    for (Utilisateur u : users) {
                        System.out.println("ID: " + u.getId() + " | Nom: " + u.getPrenom() + " " + u.getNom() + " | Email: " + u.getEmail() + " | Rôle: " + u.getRole().getNomRole());
                    }
                    break;

                case 2:
                    System.out.println("\n--- AJOUTER UN ETUDIANT ---");
                    System.out.print("Nom : "); String nom = scanner.nextLine();
                    System.out.print("Prénom : "); String prenom = scanner.nextLine();
                    System.out.print("Email : "); String email = scanner.nextLine();
                    System.out.print("Mot de passe : "); String mdp = scanner.nextLine();
                    System.out.print("Numéro Etudiant : "); String numEtud = scanner.nextLine();

                    Role roleEtudiant = new Role(3, "Etudiant");
                    Etudiant nouvelEtudiant = new Etudiant(0, nom, prenom, 20, email, 12345678, mdp, roleEtudiant, numEtud, "GL-3");

                    if (service.ajouterEtudiant(nouvelEtudiant)) {
                        System.out.println("✅ Étudiant ajouté avec succès !");
                    } else {
                        System.out.println("❌ Erreur lors de l'ajout.");
                    }
                    break;

                case 3:
                    System.out.println("\n--- MODIFIER UN EMAIL ---");
                    System.out.print("ID de l'utilisateur à modifier : "); int idModif = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nouvel email : "); String nouvelEmail = scanner.nextLine();

                    if (service.modifierEmailUtilisateur(idModif, nouvelEmail)) {
                        System.out.println("✅ Email mis à jour !");
                    } else {
                        System.out.println("❌ Erreur ou ID introuvable.");
                    }
                    break;

                case 4:
                    System.out.println("\n--- SUPPRIMER UN UTILISATEUR ---");
                    System.out.print("ID de l'utilisateur à supprimer : "); int idSupp = scanner.nextInt();

                    if (service.supprimerUtilisateur(idSupp)) {
                        System.out.println("✅ Utilisateur supprimé !");
                    } else {
                        System.out.println("❌ Erreur ou ID introuvable.");
                    }
                    break;

                case 0:
                    continuer = false;
                    System.out.println("Déconnexion de l'administrateur...");
                    break;

                default:
                    System.out.println("Choix invalide.");
            }
        }
    }
}