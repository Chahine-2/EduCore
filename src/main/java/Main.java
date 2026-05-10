import interfaces.IUtilisateurService;
import models.Etudiant;
import models.Role;
import models.Utilisateur;
import services.UtilisateurService;

import java.util.ArrayList;
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
            afficherMenuEnseignant(scanner, service, user);
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
            System.out.println("6.consulter Historique de connection");
            System.out.println("7. Ajouter un enseignant (CREATE)");
            System.out.println("8. Ajouter une classe (CREATE)");
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

                    // 1. Vérification des classes disponibles
                    List<String> classesDispo = service.listerToutesLesClasses();
                    if (classesDispo.isEmpty()) {
                        System.out.println("⚠️ Aucune classe n'existe dans le système !");
                        System.out.println("Veuillez d'abord créer une classe avec l'option 8.");
                        break;
                    }

                    // 2. Saisie manuelle de TOUS les champs
                    System.out.print("Nom : "); String nom = scanner.nextLine();
                    System.out.print("Prénom : "); String prenom = scanner.nextLine();

                    System.out.print("Âge : ");
                    int age = 0;
                    try { age = Integer.parseInt(scanner.nextLine()); }
                    catch (NumberFormatException e) { System.out.println("⚠️ Âge invalide, mis à 0 par défaut."); }

                    System.out.print("Email : "); String email = scanner.nextLine();

                    System.out.print("Numéro de téléphone : ");
                    int tel = 0;
                    try { tel = Integer.parseInt(scanner.nextLine()); }
                    catch (NumberFormatException e) { System.out.println("⚠️ Numéro invalide, mis à 0 par défaut."); }

                    System.out.print("Mot de passe : "); String mdp = scanner.nextLine();
                    System.out.print("Numéro Etudiant (ex: ETUD-123) : "); String numEtud = scanner.nextLine();

                    System.out.print("Le compte est-il actif immédiatement ? (O/N) : ");
                    String repActif = scanner.nextLine().trim().toUpperCase();
                    boolean statutActif = repActif.equals("O") || repActif.equals("OUI");

                    // 3. Choix de la classe
                    System.out.println("\nChoisissez la classe de l'étudiant :");
                    for (int i = 0; i < classesDispo.size(); i++) {
                        System.out.println((i + 1) + ". " + classesDispo.get(i));
                    }
                    System.out.print("Votre choix (numéro) : ");
                    int choixClasse;
                    try {
                        choixClasse = Integer.parseInt(scanner.nextLine()) - 1;
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Choix invalide. Annulation de la création.");
                        break;
                    }

                    if (choixClasse < 0 || choixClasse >= classesDispo.size()) {
                        System.out.println("❌ Numéro de classe invalide. Annulation.");
                        break;
                    }

                    String classeChoisie = classesDispo.get(choixClasse);
                    Role roleEtudiant = new Role(3, "Etudiant");

                    // 4. Création de l'objet avec les variables saisies (Vérifiez l'ordre selon votre constructeur Etudiant)
                    Etudiant nouvelEtudiant = new Etudiant(0, nom, prenom, age, email, tel, mdp, roleEtudiant, numEtud,"N/A",true);

                    // 5. Appel au service pour l'insertion en BDD
                    if (service.ajouterEtudiant(nouvelEtudiant)) {
                        System.out.println("✅ Étudiant ajouté avec succès dans la classe " + classeChoisie + " !");
                    } else {
                        System.out.println("❌ Erreur lors de l'ajout en base de données.");
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
                    System.out.println("\n--- HISTORIQUE DE CONNEXION ---");
                    List<models.HistoriqueConnexion> historique = service.recupererHistoriqueConnexions();
                    for (models.HistoriqueConnexion h : historique) {
                        System.out.println("[" + h.getDate() + "] " + h.getStatut() + " | Email : " + h.getEmail());
                    }
                    break;
                case 7:
                    System.out.println("\n--- AJOUTER UN ENSEIGNANT ---");
                    System.out.print("Nom : "); String nomProf = scanner.nextLine();
                    System.out.print("Prénom : "); String prenomProf = scanner.nextLine();
                    System.out.print("Email : "); String emailProf = scanner.nextLine();
                    System.out.print("Mot de passe : "); String mdpProf = scanner.nextLine();
                    System.out.print("Spécialité (ex: Informatique) : "); String specialite = scanner.nextLine();
                    System.out.print("Matricule (ex: PROF-101) : "); String matricule = scanner.nextLine();

                    Role roleProf = new Role(2, "Enseignant");

                    // Création de l'objet (l'âge et le tél sont mis à 30 et 0000 par défaut ici pour l'exemple)
                    models.Enseignant nouvelEnseignant = new models.Enseignant(0, nomProf, prenomProf, 30, emailProf, 00000000, mdpProf, roleProf, specialite, matricule,true);

                    if (service.ajouterEnseignant(nouvelEnseignant)) {
                        System.out.println("✅ Enseignant ajouté avec succès !");
                    } else {
                        System.out.println("❌ Erreur lors de l'ajout.");
                    }
                    break;
                case 8: // <-- NOUVELLE OPTION POUR CRÉER UNE CLASSE
                    System.out.println("\n--- AJOUTER UNE NOUVELLE CLASSE ---");
                    System.out.print("Nom de la nouvelle classe (ex: INFO-1) : ");
                    String nouvelleClasse = scanner.nextLine().trim().toUpperCase();

                    if (nouvelleClasse.isEmpty()) {
                        System.out.println("Le nom de la classe ne peut pas être vide.");
                        break;
                    }

                    if (service.ajouterClasse(nouvelleClasse)) {
                        System.out.println("✅ La classe " + nouvelleClasse + " a été ajoutée avec succès !");
                    } else {
                        System.out.println("❌ Impossible d'ajouter cette classe.");
                    }
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
    // --- 5. MENU ENSEIGNANT (NOUVEAU) ---
    private static void afficherMenuEnseignant(Scanner scanner, IUtilisateurService service, Utilisateur enseignant) {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n=======================================");
            System.out.println("          ESPACE ENSEIGNANT            ");
            System.out.println("=======================================");
            System.out.println("1. Faire l'appel (Marquer les présences)");
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
                    System.out.println("\n--- FAIRE L'APPEL ---");
                    List<String> classes = service.listerClassesExistantes();
                    if (classes.isEmpty()) {
                        System.out.println("Aucune classe trouvée.");
                        break;
                    }

                    System.out.println("Classes disponibles :");
                    for (int i = 0; i < classes.size(); i++) {
                        System.out.println((i + 1) + ". " + classes.get(i));
                    }

                    System.out.print("Sélectionnez le numéro de la classe : ");
                    int choixClasse = Integer.parseInt(scanner.nextLine()) - 1;

                    if (choixClasse < 0 || choixClasse >= classes.size()) {
                        System.out.println("Choix invalide.");
                        break;
                    }

                    String classeSelectionnee = classes.get(choixClasse);
                    List<models.Etudiant> etudiants = service.listerEtudiantsParClasse(classeSelectionnee);


                    if (etudiants.isEmpty()) {
                        System.out.println("Aucun étudiant actif trouvé dans cette classe.");
                        break;
                    }

                    List<Integer> presents = new ArrayList<>();
                    List<Integer> absents = new ArrayList<>();

                    System.out.println("\nAppel pour la classe " + classeSelectionnee + " :");
                    for (Utilisateur etu : etudiants) {
                        System.out.print(etu.getPrenom() + " " + etu.getNom() + " est-il/elle présent(e) ? (O/N) : ");
                        String reponse = scanner.nextLine().trim().toUpperCase();

                        if (reponse.equals("O")) {
                            presents.add(etu.getId());
                        } else {
                            absents.add(etu.getId());
                        }
                    }

                    if (service.marquerPresence(enseignant.getId(), classeSelectionnee, presents, absents)) {
                        System.out.println("✅ L'appel a été enregistré avec succès !");
                    } else {
                        System.out.println("❌ Erreur lors de l'enregistrement de l'appel.");
                    }
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