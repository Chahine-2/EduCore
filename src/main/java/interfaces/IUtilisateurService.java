package interfaces;

import models.Etudiant;
import models.Utilisateur;
import java.util.List;

public interface IUtilisateurService {
    Utilisateur authentifier(String email, String motDePasse);

    // Opérations CRUD pour l'Administrateur
    boolean ajouterEtudiant(Etudiant etudiant);
    boolean ajouterEnseignant(models.Enseignant enseignant);
    List<Utilisateur> listerUtilisateurs();
    boolean modifierEmailUtilisateur(int id, String nouvelEmail);
    boolean supprimerUtilisateur(int id);

    // Opération Métier Avancée
    boolean changerStatutCompte(int id, boolean rendreActif);


    // --- Fonctionnalités Enseignant (Présences) ---
    List<String> listerClassesExistantes();
    List<models.Etudiant> listerEtudiantsParClasse(String classe);
    /**
     * Starts a new attendance session for the class (call after loading the roster, before marking present/absent).
     * Required when {@link #enregistrerPresence} uses the {@code presences(session_id, ...)} schema.
     */
    void preparerNouvelleSessionAppel(String classe);

    boolean marquerPresence(int enseignantId, String classe, List<Integer> etudiantsPresents, List<Integer> etudiantsAbsents);

    // --- Gestion des Classes (Administrateur) ---
    boolean ajouterClasse(String nomClasse);
    List<String> listerToutesLesClasses();
    List<models.HistoriqueConnexion> recupererHistoriqueConnexions();

    Utilisateur getUtilisateurComplet(int id, String roleNom);
    boolean modifierUtilisateur(Utilisateur user);


    boolean enregistrerPresence(int etudiantId, String statut);
}