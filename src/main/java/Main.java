import models.*;
import services.*;

public class Main {
    public static void main(String[] args) {


        // TEST CATEGORIE

        System.out.println("========== CATEGORIE ==========");
        ServiceCategorie serviceCategorie = new ServiceCategorie();

        // Ajouter
        Categorie cat1 = new Categorie("Informatique", "Cours d'informatique", "💻", 1, "active");
        Categorie cat2 = new Categorie("Mathématiques", "Cours de maths", "📐", 2, "active");
        serviceCategorie.add(cat1);
        serviceCategorie.add(cat2);

        // Afficher tout
        System.out.println("--- Toutes les catégories ---");
        System.out.println(serviceCategorie.getAll());

        // Modifier
        cat1.setId(1);
        cat1.setNom("Informatique Avancée");
        serviceCategorie.update(cat1);
        System.out.println("--- Après modification ---");
        System.out.println(serviceCategorie.getAll());

        // Supprimer
        cat2.setId(2);
        serviceCategorie.delete(cat2);
        System.out.println("--- Après suppression ---");
        System.out.println(serviceCategorie.getAll());










    }
}