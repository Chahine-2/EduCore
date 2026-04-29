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







        //  TEST COURS

        System.out.println("========== COURS ==========");
        ServiceCours serviceCours = new ServiceCours();

        // Ajouter
        Cours cours1 = new Cours(
                "Java Avancé",
                "Cours complet sur Java",
                "Maîtriser Java",
                "Bases de Java",
                40, "avance", "Français",
                1, "publie"
        );
        Cours cours2 = new Cours(
                "Python Débutant",
                "Introduction à Python",
                "Apprendre Python",
                "Aucun",
                20, "debutant", "Français",
                1, "publie"
        );
        serviceCours.add(cours1);
        serviceCours.add(cours2);

        // Afficher tout
        System.out.println("--- Tous les cours ---");
        System.out.println(serviceCours.getAll());

        // Modifier
        cours1.setId(1);
        cours1.setTitre("Java Expert");
        cours1.setStatut("archive");
        serviceCours.update(cours1);
        System.out.println("--- Après modification ---");
        System.out.println(serviceCours.getAll());

        // Supprimer
        cours2.setId(2);
        serviceCours.delete(cours2);
        System.out.println("--- Après suppression ---");
        System.out.println(serviceCours.getAll());



        // TEST CHAPITRE

        System.out.println("========== CHAPITRE ==========");
        ServiceChapitre serviceChapitre = new ServiceChapitre();

        // Ajouter  (cours_id = 1 → doit exister dans la table cours)
        Chapitre chap1 = new Chapitre("Introduction à Java", "Bases du langage", 1, 60, true, 1);
        Chapitre chap2 = new Chapitre("POO et Héritage", "Programmation orientée objet", 2, 120, false, 1);
        serviceChapitre.add(chap1);
        serviceChapitre.add(chap2);

        // Afficher tout
        System.out.println("--- Tous les chapitres ---");
        System.out.println(serviceChapitre.getAll());

        // Modifier
        chap1.setId(1);
        chap1.setTitre("Introduction complète à Java");
        chap1.setDureeMinutes(90);
        serviceChapitre.update(chap1);
        System.out.println("--- Après modification ---");
        System.out.println(serviceChapitre.getAll());

        // Supprimer
        chap2.setId(2);
        serviceChapitre.delete(chap2);
        System.out.println("--- Après suppression ---");
        System.out.println(serviceChapitre.getAll());



        // TEST LECON

        System.out.println("========== LECON ==========");
        ServiceLecon serviceLecon = new ServiceLecon();

        // Ajouter  (chapitre_id = 1 → doit exister dans la table chapitre)
        Lecon lecon1 = new Lecon(
                "Qu'est-ce que Java ?",
                "Java est un langage orienté objet...",
                "texte", null, 15, 1, true, 1
        );
        Lecon lecon2 = new Lecon(
                "Vidéo : installer Java",
                null,
                "video", "https://youtube.com/java-install", 10, 2, true, 1
        );
        serviceLecon.add(lecon1);
        serviceLecon.add(lecon2);

        // Afficher tout
        System.out.println("--- Toutes les leçons ---");
        System.out.println(serviceLecon.getAll());

        // Modifier
        lecon1.setId(1);
        lecon1.setTitre("Introduction à Java");
        lecon1.setDureeMinutes(20);
        serviceLecon.update(lecon1);
        System.out.println("--- Après modification ---");
        System.out.println(serviceLecon.getAll());

        // Supprimer
        lecon2.setId(2);
        serviceLecon.delete(lecon2);
        System.out.println("--- Après suppression ---");
        System.out.println(serviceLecon.getAll());



        //  TEST RESSOURCE

        System.out.println("========== RESSOURCE ==========");
        ServiceRessource serviceRessource = new ServiceRessource();

        // Ajouter
        Ressource res1 = new Ressource("Exercices_Java.pdf", "pdf", "/files/exercices.pdf", 250, 1, 0);
        Ressource res2 = new Ressource("Doc Oracle", "lien", "https://docs.oracle.com/java", 0, 0, 1);
        serviceRessource.add(res1);
        serviceRessource.add(res2);

        // Afficher tout
        System.out.println("--- Toutes les ressources ---");
        System.out.println(serviceRessource.getAll());

        // Modifier
        res1.setId(1);
        res1.setNom("Exercices_Java_V2.pdf");
        res1.setTailleKo(300);
        serviceRessource.update(res1);
        System.out.println("--- Après modification ---");
        System.out.println(serviceRessource.getAll());

        // Supprimer
        res2.setId(2);
        serviceRessource.delete(res2);
        System.out.println("--- Après suppression ---");
        System.out.println(serviceRessource.getAll());








    }
}