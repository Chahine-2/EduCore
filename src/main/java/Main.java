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

        // Si l'utilisateur n'existe pas, mauvais mdp, ou compte suspendu
        if (user == null) {
            System.out.println("❌ ÉCHEC : Email incorrect, mot de passe incorrect, ou compte suspendu.");
            System.exit(0);
        }

        // --- 2. ROUTAGE INTELLIGENT BASÉ SUR LE RÔLE ---
        String roleName = user.getRole().getNomRole();

        if (roleName.equals("Administrateur")) {
            System.out.println("✅ Connexion réussie ! Bienvenue Admin " + user.getPrenom() + ".");
            afficherMenuAdmin(scanner, service, user.getId());
        }
        else if (roleName.equals("Etudiant")) {
            System.out.println("✅ Connexion réussie ! Bienvenue " + user.getPrenom() + ".");
            afficherMenuEtudiant(scanner, (Etudiant) user);
        }
        else if (roleName.equals("Enseignant")) {
            System.out.println("✅ Connexion réussie ! Bienvenue Professeur " + user.getPrenom() + ".");
            System.out.println("(Le menu enseignant est en cours de développement...)");
        }
        else {
            System.out.println("Rôle non reconnu.");
        }

        System.out.println("\nFermeture de l'application. À bientôt !");
        scanner.close();
    }

    // --- 3. MENU ADMINISTRATEUR ---
    private static void afficherMenuAdmin(Scanner scanner, IUtilisateurService service, int monIdAdmin) {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n=======================================");
            System.out.println("          PANNEAU ADMINISTRATEUR       ");
            System.out.println("=======================================");
            System.out.println("1. Lister tous les utilisateurs (READ)");
            System.out.println("2. Ajouter un étudiant (CREATE - Sécurisé avec BCrypt)");
            System.out.println("3. Modifier l'email d'un utilisateur (UPDATE)");
            System.out.println("4. Supprimer un utilisateur (DELETE)");
            System.out.println("5. Suspendre / Activer un compte (MÉTIER)");
            System.out.println("6.consulterHistorique");
            System.out.println("0. Se déconnecter");
            System.out.print("Choix : ");

            int choix = -1;
            try {
                choix = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
                continue;
            }

            switch (choix) {
                case 1:
                    System.out.println("\n--- LISTE DES UTILISATEURS ---");
                    List<Utilisateur> users = service.listerUtilisateurs();
                    for (Utilisateur u : users) {
                        String statut = u.isStatutActif() ? "[ACTIF]" : "[SUSPENDU]";
                        System.out.println("ID: " + u.getId() + " | " + statut + " | Nom: " + u.getPrenom() + " " + u.getNom() + " | Email: " + u.getEmail() + " | Rôle: " + u.getRole().getNomRole());
                    }
                    break;

                case 2:
                    System.out.println("\n--- AJOUTER UN ETUDIANT ---");
                    System.out.print("Nom : "); String nom = scanner.nextLine();
                    System.out.print("Prénom : "); String prenom = scanner.nextLine();
                    System.out.print("Email : "); String email = scanner.nextLine();
                    System.out.print("Mot de passe : "); String mdp = scanner.nextLine();
                    System.out.print("Numéro Etudiant (ex: ETUD-123) : "); String numEtud = scanner.nextLine();

                    Role roleEtudiant = new Role(3, "Etudiant");
                    Etudiant nouvelEtudiant = new Etudiant(0, nom, prenom, 20, email, 12345678, mdp, roleEtudiant, "N/A", "N/A", true);

                    if (service.ajouterEtudiant(nouvelEtudiant)) {
                        System.out.println("✅ Étudiant ajouté avec succès ! (Mot de passe haché dans la BDD)");
                    } else {
                        System.out.println("❌ Erreur lors de l'ajout.");
                    }
                    break;

                case 3:
                    System.out.println("\n--- MODIFIER UN EMAIL ---");
                    System.out.print("ID de l'utilisateur à modifier : "); int idModif = Integer.parseInt(scanner.nextLine());
                    System.out.print("Nouvel email : "); String nouvelEmail = scanner.nextLine();

                    if (service.modifierEmailUtilisateur(idModif, nouvelEmail)) {
                        System.out.println("✅ Email mis à jour !");
                    } else {
                        System.out.println("❌ Erreur ou ID introuvable.");
                    }
                    break;

                case 4:
                    System.out.println("\n--- SUPPRIMER UN UTILISATEUR ---");
                    System.out.print("ID de l'utilisateur à supprimer : "); int idSupp = Integer.parseInt(scanner.nextLine());

                    if (service.supprimerUtilisateur(idSupp)) {
                        System.out.println("✅ Utilisateur supprimé de la base de données !");
                    } else {
                        System.out.println("❌ Erreur ou ID introuvable.");
                    }
                    break;

                case 5:
                    System.out.println("\n--- SUSPENDRE / ACTIVER UN COMPTE ---");
                    System.out.print("Entrez l'ID de l'utilisateur : ");
                    int idStatut = Integer.parseInt(scanner.nextLine());

                    // --- LA SÉCURITÉ ANTI-LOCKOUT ---
                    if (idStatut == monIdAdmin) {
                        System.out.println("❌ ACTION REFUSÉE : Vous ne pouvez pas suspendre votre propre compte !");
                        break; // On arrête l'action ici
                    }

                    System.out.print("Voulez-vous (1) Activer ou (2) Suspendre ce compte ? : ");
                    int choixStatut = Integer.parseInt(scanner.nextLine());

                    boolean rendreActif = (choixStatut == 1);

                    if (service.changerStatutCompte(idStatut, rendreActif)) {
                        System.out.println("✅ Statut du compte mis à jour !");
                    } else {
                        System.out.println("❌ Erreur lors de la mise à jour.");
                    }
                    break;
                // Ajoutez ceci juste en dessous du case 5
                case 6:
                    service.consulterHistoriqueConnexions();
                    break;
                case 0:
                    continuer = false;
                    System.out.println("Déconnexion...");
                    break;

                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    // --- 4. MENU ETUDIANT ---
    private static void afficherMenuEtudiant(Scanner scanner, Etudiant etudiant) {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n=======================================");
            System.out.println("            ESPACE ETUDIANT            ");
            System.out.println("=======================================");
            System.out.println("1. Voir mon profil personnel");
            System.out.println("0. Se déconnecter");
            System.out.print("Choix : ");

            int choix = -1;
            try {
                choix = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
                continue;
            }

            switch (choix) {
                case 1:
                    System.out.println("\n--- MON PROFIL ---");
                    System.out.println("Nom : " + etudiant.getNom());
                    System.out.println("Prénom : " + etudiant.getPrenom());
                    System.out.println("Email : " + etudiant.getEmail());
                    // Note: Dans un vrai système, il faudrait faire une requête SQL spécifique
                    // dans UtilisateurService pour récupérer ces données depuis la table 'etudiants'
                    // lors de l'authentification.
                    break;
                case 0:
                    continuer = false;
                    System.out.println("Déconnexion...");
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }
}