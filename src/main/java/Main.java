import models.*;
import services.*;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        ServiceCours sc     = new ServiceCours();
        ServiceChapitre schap = new ServiceChapitre();

        // ══ AJOUTER COURS ══════════════════════════
        sc.add(new Cours("Java Avancé",          "Maîtriser Java OOP",      "Comprendre la POO",   40, "avance",        "informatique", true,  LocalDate.of(2025,1,10), LocalDate.of(2025,6,30)));
        sc.add(new Cours("Python Débutant",      "Introduction à Python",   "Apprendre Python",    20, "debutant",      "informatique", false, LocalDate.of(2025,2,1),  LocalDate.of(2025,5,1)));
        sc.add(new Cours("Résistance Matériaux", "RDM de base",             "Calcul des forces",   35, "intermediaire", "mecanique",    true,  LocalDate.of(2025,3,1),  LocalDate.of(2025,7,1)));
        sc.add(new Cours("Circuits Électriques", "Loi d'Ohm et Kirchhoff", "Maîtriser AC/DC",     30, "debutant",      "electrique",   false, LocalDate.of(2025,1,15), LocalDate.of(2025,4,15)));

        System.out.println("=== Tous les cours ===");
        System.out.println(sc.getAll());

        System.out.println("=== Cours Informatique ===");
        System.out.println(sc.getByCategorie("informatique"));

        System.out.println("=== Cours Mécanique ===");
        System.out.println(sc.getByCategorie("mecanique"));

        System.out.println("=== Cours Électrique ===");
        System.out.println(sc.getByCategorie("electrique"));

        System.out.println("=== Cours Débutant ===");
        System.out.println(sc.getByNiveau("debutant"));

        System.out.println("=== Cours Certifiants ===");
        System.out.println(sc.getCertifiants());

        // ══ AJOUTER CHAPITRES ══════════════════════
        schap.add(new Chapitre("Introduction Java",   "Hello World",        1, 30,  "video", "https://youtube.com/java1",    LocalDate.now(), 1));
        schap.add(new Chapitre("POO et Héritage",     "Classes & objets",   2, 90,  "video", "https://youtube.com/java2",    LocalDate.now(), 1));
        schap.add(new Chapitre("Collections Java",    "List, Map, Set",     3, 60,  "texte", null,                           LocalDate.now(), 1));
        schap.add(new Chapitre("Quiz Final Java",     "Test tes connaiss",  4, 20,  "quiz",  null,                           LocalDate.now(), 1));
        schap.add(new Chapitre("Hello Python",        "Premier programme",  1, 30,  "video", "https://youtube.com/python1",  LocalDate.now(), 2));
        schap.add(new Chapitre("Variables & Types",   "Types de données",   2, 45,  "texte", null,                           LocalDate.now(), 2));
        schap.add(new Chapitre("Intro RDM",           "Forces et moments",  1, 60,  "pdf",   "https://drive.com/rdm1",       LocalDate.now(), 3));
        schap.add(new Chapitre("Loi d'Ohm",           "U = R x I",          1, 45,  "video", "https://youtube.com/ohm",      LocalDate.now(), 4));

        System.out.println("=== Tous les chapitres ===");
        System.out.println(schap.getAll());

        System.out.println("=== Chapitres du cours Java (id=1) ===");
        System.out.println(schap.getByCours(1));

        System.out.println("=== Chapitres de type video ===");
        System.out.println(schap.getByType("video"));

        // ══ JOINTURE ═══════════════════════════════
        sc.getCoursAvecChapitres();

        // ══ UPDATE ═════════════════════════════════
        System.out.println("=== UPDATE cours id=1 ===");
        Cours modif = new Cours("Java Expert", "Cours Java mis à jour", "Maîtriser Java 17", 50, "avance", "informatique", true, LocalDate.of(2025,1,10), LocalDate.of(2025,8,30));
        modif.setId(1);
        sc.update(modif);
        System.out.println(sc.getAll());

        // ══ DELETE ═════════════════════════════════
        System.out.println("=== DELETE chapitre id=4 ===");
        Chapitre suppr = new Chapitre();
        suppr.setId(4);
        schap.delete(suppr);
        System.out.println(schap.getAll());
    }
}