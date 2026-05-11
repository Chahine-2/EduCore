package interfaces;

import models.Utilisateur;

public interface IUtilisateurDAO {
    // Method to handle the "s'authentifier" use case
    Utilisateur authentifier(String email, String motDePasse);

    // You can add other methods here later, for example:
    // boolean ajouterUtilisateur(Utilisateur user);
    // boolean modifierProfil(Utilisateur user);
}