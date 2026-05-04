package test;

import interfaces.IService;
import models.Evaluation;
import models.Question;
import models.QuestionType;
import models.Reponse;
import models.ReponseEtudiant;
import models.Resultat;
// Bareme model/service removed from runtime
import services.EvaluationDAOImpl;
import services.QuestionDAOImpl;
import services.ReponseDAOImpl;
import services.ReponseEtudiantDAOImpl;
import services.ResultatDAOImpl;
import utils.ValidationUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        IService<Evaluation> evaluationDAO = new EvaluationDAOImpl();
        IService<Question> questionDAO = new QuestionDAOImpl();
        IService<Reponse> reponseDAO = new ReponseDAOImpl();
        IService<ReponseEtudiant> reponseEtudiantDAO = new ReponseEtudiantDAOImpl();
        IService<Resultat> resultatDAO = new ResultatDAOImpl();
        // Bareme functionality removed

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        addEvaluation(scanner, evaluationDAO);
                        break;
                    case "2":
                        showAllEvaluations(evaluationDAO);
                        break;
                    case "3":
                        updateEvaluation(scanner, evaluationDAO);
                        break;
                    case "4":
                        deleteEvaluation(scanner, evaluationDAO);
                        break;
                    case "5":
                        addQuestion(scanner, questionDAO);
                        break;
                    case "6":
                        showAllQuestions(questionDAO);
                        break;
                    case "7":
                        updateQuestion(scanner, questionDAO);
                        break;
                    case "8":
                        deleteQuestion(scanner, questionDAO);
                        break;
                    case "9":
                        addReponse(scanner, reponseDAO);
                        break;
                    case "10":
                        showAllReponses(reponseDAO);
                        break;
                    case "11":
                        updateReponse(scanner, reponseDAO);
                        break;
                    case "12":
                        deleteReponse(scanner, reponseDAO);
                        break;
                    case "13":
                        addReponseEtudiant(scanner, reponseEtudiantDAO);
                        break;
                    case "14":
                        showAllReponsesEtudiant(reponseEtudiantDAO);
                        break;
                    case "15":
                        updateReponseEtudiant(scanner, reponseEtudiantDAO);
                        break;
                    case "16":
                        deleteReponseEtudiant(scanner, reponseEtudiantDAO);
                        break;
                    case "17":
                        addResultat(scanner, resultatDAO);
                        break;
                    case "18":
                        showAllResultats(resultatDAO);
                        break;
                    case "19":
                        updateResultat(scanner, resultatDAO);
                        break;
                    case "20":
                        deleteResultat(scanner, resultatDAO);
                        break;
                    // Bareme options removed (21-24)
                    case "25":
                        corrigerResultat(scanner, resultatDAO);
                        break;
                    case "0":
                        running = false;
                        System.out.println("Bye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (RuntimeException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("===== CRUD Menu =====");
        System.out.println("--- Evaluation ---");
        System.out.println("1. Add evaluation");
        System.out.println("2. Show all evaluations");
        System.out.println("3. Update evaluation");
        System.out.println("4. Delete evaluation");
        System.out.println("--- Question ---");
        System.out.println("5. Add question");
        System.out.println("6. Show all questions");
        System.out.println("7. Update question");
        System.out.println("8. Delete question");
        System.out.println("--- Reponse ---");
        System.out.println("9. Add reponse");
        System.out.println("10. Show all reponses");
        System.out.println("11. Update reponse");
        System.out.println("12. Delete reponse");
        System.out.println("--- Reponse Etudiant ---");
        System.out.println("13. Add reponse etudiant");
        System.out.println("14. Show all reponses etudiant");
        System.out.println("15. Update reponse etudiant");
        System.out.println("16. Delete reponse etudiant");
        System.out.println("--- Resultat ---");
        System.out.println("17. Add resultat");
        System.out.println("18. Show all resultats");
        System.out.println("19. Update resultat");
        System.out.println("20. Delete resultat");
        // Bareme commands removed
        System.out.println("25. Corriger resultat (auto-correction)");
        System.out.println("0. Exit");
    }

    private static void addEvaluation(Scanner scanner, IService<Evaluation> dao) {
        Evaluation evaluation = readEvaluationData(scanner, 0, false);
        dao.add(evaluation);
        System.out.println("✓ Evaluation added successfully.");
    }

    private static void showAllEvaluations(IService<Evaluation> dao) {
        List<Evaluation> evaluations = dao.getAll();
        if (evaluations.isEmpty()) {
            System.out.println("No evaluations found.");
            return;
        }

        for (Evaluation evaluation : evaluations) {
            System.out.println(evaluation);
        }
    }

    private static void updateEvaluation(Scanner scanner, IService<Evaluation> dao) {
        int id;
        while (true) {
            System.out.print("Enter evaluation id to update: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Evaluation current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No evaluation found with id " + id);
            return;
        }

        Evaluation updated = readEvaluationData(scanner, id, true);
        dao.update(updated);
        System.out.println("✓ Evaluation updated successfully.");
    }

    private static void deleteEvaluation(Scanner scanner, IService<Evaluation> dao) {
        int id;
        while (true) {
            System.out.print("Enter evaluation id to delete: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Evaluation current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No evaluation found with id " + id);
            return;
        }

        dao.delete(id);
        System.out.println("✓ Evaluation deleted successfully.");
    }

    private static Evaluation readEvaluationData(Scanner scanner, int id, boolean includeId) {
        // Titre validation
        String titre;
        while (true) {
            System.out.print("Titre: ");
            titre = scanner.nextLine();
            if (ValidationUtil.isNotEmpty(titre)) break;
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Titre", "EMPTY"));
        }

        System.out.print("Description: ");
        String description = scanner.nextLine();

        // Duree validation
        int duree;
        while (true) {
            System.out.print("Duree en minutes: ");
            String dureeStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(dureeStr)) {
                duree = Integer.parseInt(dureeStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Duree", "POSITIVE_INT"));
        }

        // Note max validation
        float noteMax;
        while (true) {
            System.out.print("Note max: ");
            String noteMaxStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveFloat(noteMaxStr)) {
                noteMax = Float.parseFloat(noteMaxStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Note max", "POSITIVE_FLOAT"));
        }

        // Note passage validation
        float notePassage;
        while (true) {
            System.out.print("Note passage: ");
            String notePassageStr = scanner.nextLine();
            if (ValidationUtil.isValidNonNegativeFloat(notePassageStr)) {
                notePassage = Float.parseFloat(notePassageStr);
                if (ValidationUtil.isValidNoteRange(notePassage, noteMax)) break;
                System.out.println("❌ " + ValidationUtil.getErrorMessage("", "NOTE_RANGE"));
            } else {
                System.out.println("❌ " + ValidationUtil.getErrorMessage("Note passage", "NON_NEGATIVE_FLOAT"));
            }
        }

        LocalDateTime dateDebut = readDateTime(scanner, "Date debut (yyyy-MM-dd HH:mm:ss): ");
        LocalDateTime dateFin = readDateTime(scanner, "Date fin (yyyy-MM-dd HH:mm:ss): ");
        // Build Evaluation according to current DB-backed model
        if (includeId) {
            return new Evaluation(id, titre, description, duree, noteMax, notePassage, dateDebut, dateFin, null);
        }
        return new Evaluation(titre, description, duree, noteMax, notePassage, dateDebut, dateFin);
    }

    private static void addQuestion(Scanner scanner, IService<Question> dao) {
        Question question = readQuestionData(scanner, 0, false);
        dao.add(question);
        System.out.println("✓ Question added successfully.");
    }

    private static void showAllQuestions(IService<Question> dao) {
        List<Question> questions = dao.getAll();
        if (questions.isEmpty()) {
            System.out.println("No questions found.");
            return;
        }

        for (Question question : questions) {
            System.out.println(question);
        }
    }

    private static void updateQuestion(Scanner scanner, IService<Question> dao) {
        int id;
        while (true) {
            System.out.print("Enter question id to update: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Question current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No question found with id " + id);
            return;
        }

        Question updated = readQuestionData(scanner, id, true);
        dao.update(updated);
        System.out.println("✓ Question updated successfully.");
    }

    private static void deleteQuestion(Scanner scanner, IService<Question> dao) {
        int id;
        while (true) {
            System.out.print("Enter question id to delete: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Question current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No question found with id " + id);
            return;
        }

        dao.delete(id);
        System.out.println("✓ Question deleted successfully.");
    }

    private static Question readQuestionData(Scanner scanner, int id, boolean includeId) {
        // Texte validation
        String texte;
        while (true) {
            System.out.print("Question texte: ");
            texte = scanner.nextLine();
            if (ValidationUtil.isNotEmpty(texte)) break;
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Texte", "EMPTY"));
        }

        // Type validation
        QuestionType type;
        while (true) {
            System.out.print("Type (qcm, vrai_faux, texte_libre, correspondance): ");
            String typeStr = scanner.nextLine();
            if (ValidationUtil.isNotEmpty(typeStr)) {
                type = QuestionType.fromDbValue(typeStr);
                if (type != null) break;
            }
            System.out.println("❌ Type invalide");
        }

        // Points validation
        float points;
        while (true) {
            System.out.print("Points: ");
            String pointsStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveFloat(pointsStr)) {
                points = Float.parseFloat(pointsStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Points", "POSITIVE_FLOAT"));
        }

        // Evaluation ID validation

        // Evaluation ID validation
        int evaluationId;
        while (true) {
            System.out.print("Evaluation ID: ");
            String evalIdStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(evalIdStr)) {
                evaluationId = Integer.parseInt(evalIdStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Evaluation ID", "POSITIVE_INT"));
        }

        if (includeId) {
            return new Question(id, texte, type, points, evaluationId);
        }
        return new Question(texte, type, points, evaluationId);
    }

    private static void addReponse(Scanner scanner, IService<Reponse> dao) {
        Reponse reponse = readReponseData(scanner, 0, false);
        dao.add(reponse);
        System.out.println("✓ Reponse added successfully.");
    }

    private static void showAllReponses(IService<Reponse> dao) {
        List<Reponse> reponses = dao.getAll();
        if (reponses.isEmpty()) {
            System.out.println("No reponses found.");
            return;
        }

        for (Reponse reponse : reponses) {
            System.out.println(reponse);
        }
    }

    private static void updateReponse(Scanner scanner, IService<Reponse> dao) {
        int id;
        while (true) {
            System.out.print("Enter reponse id to update: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Reponse current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No reponse found with id " + id);
            return;
        }

        Reponse updated = readReponseData(scanner, id, true);
        dao.update(updated);
        System.out.println("✓ Reponse updated successfully.");
    }

    private static void deleteReponse(Scanner scanner, IService<Reponse> dao) {
        int id;
        while (true) {
            System.out.print("Enter reponse id to delete: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Reponse current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No reponse found with id " + id);
            return;
        }

        dao.delete(id);
        System.out.println("✓ Reponse deleted successfully.");
    }

    private static Reponse readReponseData(Scanner scanner, int id, boolean includeId) {
        // Texte validation
        String texte;
        while (true) {
            System.out.print("Reponse texte: ");
            texte = scanner.nextLine();
            if (ValidationUtil.isNotEmpty(texte)) break;
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Texte", "EMPTY"));
        }

        boolean estCorrect = readBoolean(scanner, "Est correcte (true/false): ");

        // Question ID validation

        // Question ID validation
        int questionId;
        while (true) {
            System.out.print("Question ID: ");
            String qidStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(qidStr)) {
                questionId = Integer.parseInt(qidStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Question ID", "POSITIVE_INT"));
        }

        if (includeId) {
            return new Reponse(id, texte, estCorrect, questionId);
        }
        return new Reponse(texte, estCorrect, questionId);
    }

    private static void addReponseEtudiant(Scanner scanner, IService<ReponseEtudiant> dao) {
        ReponseEtudiant reponseEtudiant = readReponseEtudiantData(scanner, 0, false);
        dao.add(reponseEtudiant);
        System.out.println("✓ Reponse etudiant added successfully.");
    }

    private static void showAllReponsesEtudiant(IService<ReponseEtudiant> dao) {
        List<ReponseEtudiant> reponsesEtudiant = dao.getAll();
        if (reponsesEtudiant.isEmpty()) {
            System.out.println("No reponses etudiant found.");
            return;
        }

        for (ReponseEtudiant reponseEtudiant : reponsesEtudiant) {
            System.out.println(reponseEtudiant);
        }
    }

    private static void updateReponseEtudiant(Scanner scanner, IService<ReponseEtudiant> dao) {
        int id;
        while (true) {
            System.out.print("Enter reponse etudiant id to update: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        ReponseEtudiant current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No reponse etudiant found with id " + id);
            return;
        }

        ReponseEtudiant updated = readReponseEtudiantData(scanner, id, true);
        dao.update(updated);
        System.out.println("✓ Reponse etudiant updated successfully.");
    }

    private static void deleteReponseEtudiant(Scanner scanner, IService<ReponseEtudiant> dao) {
        int id;
        while (true) {
            System.out.print("Enter reponse etudiant id to delete: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        ReponseEtudiant current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No reponse etudiant found with id " + id);
            return;
        }

        dao.delete(id);
        System.out.println("✓ Reponse etudiant deleted successfully.");
    }

    private static ReponseEtudiant readReponseEtudiantData(Scanner scanner, int id, boolean includeId) {
        // Resultat ID validation
        int resultatId;
        while (true) {
            System.out.print("Resultat ID: ");
            String ridStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(ridStr)) {
                resultatId = Integer.parseInt(ridStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Resultat ID", "POSITIVE_INT"));
        }

        // Question ID validation
        int questionId;
        while (true) {
            System.out.print("Question ID: ");
            String qidStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(qidStr)) {
                questionId = Integer.parseInt(qidStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Question ID", "POSITIVE_INT"));
        }

        System.out.print("Reponse ID (leave empty if none): ");
        String reponseIdInput = scanner.nextLine().trim();
        Integer reponseId = reponseIdInput.isEmpty() ? null : Integer.parseInt(reponseIdInput);

        System.out.print("Texte libre: ");
        String texteLibre = scanner.nextLine();

        if (includeId) {
            return new ReponseEtudiant(id, resultatId, questionId, reponseId, texteLibre);
        }
        return new ReponseEtudiant(resultatId, questionId, reponseId, texteLibre);
    }

    private static void addResultat(Scanner scanner, IService<Resultat> dao) {
        Resultat resultat = readResultatData(scanner, 0, false);
        dao.add(resultat);
        System.out.println("✓ Resultat added successfully.");
    }

    private static void showAllResultats(IService<Resultat> dao) {
        List<Resultat> resultats = dao.getAll();
        if (resultats.isEmpty()) {
            System.out.println("No resultats found.");
            return;
        }

        for (Resultat resultat : resultats) {
            System.out.println(resultat);
        }
    }

    private static void updateResultat(Scanner scanner, IService<Resultat> dao) {
        int id;
        while (true) {
            System.out.print("Enter resultat id to update: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Resultat current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No resultat found with id " + id);
            return;
        }

        Resultat updated = readResultatData(scanner, id, true);
        dao.update(updated);
        System.out.println("✓ Resultat updated successfully.");
    }

    private static void deleteResultat(Scanner scanner, IService<Resultat> dao) {
        int id;
        while (true) {
            System.out.print("Enter resultat id to delete: ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        Resultat current = dao.getById(id);
        if (current == null) {
            System.out.println("❌ No resultat found with id " + id);
            return;
        }

        dao.delete(id);
        System.out.println("✓ Resultat deleted successfully.");
    }

    private static Resultat readResultatData(Scanner scanner, int id, boolean includeId) {
        // Score validation
        Float score;
        while (true) {
            System.out.print("Score (leave empty for null): ");
            String scoreStr = scanner.nextLine().trim();
            if (scoreStr.isEmpty()) {
                score = null;
                break;
            }
            if (ValidationUtil.isValidNonNegativeFloat(scoreStr)) {
                score = Float.parseFloat(scoreStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Score", "NON_NEGATIVE_FLOAT"));
        }

        LocalDateTime datePassage = readDateTime(scanner, "Date passage (yyyy-MM-dd HH:mm:ss): ");

        // Evaluation ID validation
        int evaluationId;
        while (true) {
            System.out.print("Evaluation ID: ");
            String eidStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(eidStr)) {
                evaluationId = Integer.parseInt(eidStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Evaluation ID", "POSITIVE_INT"));
        }

        // Student ID validation
        int studentId;
        while (true) {
            System.out.print("Student ID: ");
            String studentIdStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(studentIdStr)) {
                studentId = Integer.parseInt(studentIdStr);
                break;
            }
            System.out.println("❌ " + ValidationUtil.getErrorMessage("Student ID", "POSITIVE_INT"));
        }

        if (includeId) {
            return new Resultat(id, studentId, evaluationId, score, datePassage);
        }
        return new Resultat(studentId, evaluationId, score, datePassage);
    }

    private static LocalDateTime readDateTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return LocalDateTime.parse(input, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Use yyyy-MM-dd HH:mm:ss");
            }
        }
    }

    private static boolean readBoolean(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if ("true".equals(input) || "false".equals(input)) {
                return Boolean.parseBoolean(input);
            }
            System.out.println("Type true or false.");
        }
    }

    // Bareme commands removed

    /**
     * Menu helper to trigger auto-correction for a resultat.
     */
    private static void corrigerResultat(Scanner scanner, IService<Resultat> dao) {
        int id;
        while (true) {
            System.out.print("Enter resultat id to corriger (auto-correction): ");
            String idStr = scanner.nextLine();
            if (ValidationUtil.isValidPositiveInteger(idStr)) {
                id = Integer.parseInt(idStr);
                break;
            }
            System.out.println("❌ ID must be a positive integer");
        }

        // The implementation method is specific to ResultatDAOImpl, so we try to cast.
        boolean success = false;
        if (dao instanceof ResultatDAOImpl) {
            success = ((ResultatDAOImpl) dao).corrigerEvaluation(id);
        } else {
            // Fallback: create a new DAO instance and call the method
            success = new ResultatDAOImpl().corrigerEvaluation(id);
        }

        if (success) {
            System.out.println("✓ Auto-correction executed for resultat id " + id);
        } else {
            System.out.println("✗ Auto-correction failed for resultat id " + id + ". See logs for details.");
        }
    }

    // Bareme helpers removed
}
