package interfaces;

import models.Etudiant;
import models.Utilisateur;
import java.util.List;

public interface IUtilisateurService {
    Utilisateur authentifier(String email, String motDePasse);

    // CRUD Operations for Admin
    boolean ajouterEtudiant(Etudiant etudiant); // CREATE
    List<Utilisateur> listerUtilisateurs();     // READ
    boolean modifierEmailUtilisateur(int id, String nouvelEmail); // UPDATE
    boolean supprimerUtilisateur(int id);       // DELETE
}
