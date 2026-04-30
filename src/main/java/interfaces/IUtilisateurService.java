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
    void consulterHistoriqueConnexions();

    // --- Fonctionnalités Enseignant (Présences) ---
    List<String> listerClassesExistantes();
    List<Utilisateur> listerEtudiantsParClasse(String classe);
    boolean marquerPresence(int enseignantId, String classe, List<Integer> etudiantsPresents, List<Integer> etudiantsAbsents);
}