package interfaces;

import models.Utilisateur;

public interface IUtilisateurService {
    // Method to handle the "s'authentifier" use case
    Utilisateur authentifier(String email, String motDePasse);
}