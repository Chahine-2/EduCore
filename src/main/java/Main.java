import interfaces.IService;
import models.Evaluation;
import models.EvaluationStatut;
import models.EvaluationType;
import models.Question;
import models.QuestionType;
import services.EvaluationDAOImpl;
import services.QuestionDAOImpl;

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
        System.out.println("0. Exit");
    }

    private static void addEvaluation(Scanner scanner, IService<Evaluation> dao) {
        Evaluation evaluation = readEvaluationData(scanner, 0, false);
        dao.add(evaluation);
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
        System.out.print("Enter evaluation id to update: ");
        int id = Integer.parseInt(scanner.nextLine());

        Evaluation current = dao.getById(id);
        if (current == null) {
            System.out.println("No evaluation found with id " + id);
            return;
        }

        Evaluation updated = readEvaluationData(scanner, id, true);
        dao.update(updated);
    }

    private static void deleteEvaluation(Scanner scanner, IService<Evaluation> dao) {
        System.out.print("Enter evaluation id to delete: ");
        int id = Integer.parseInt(scanner.nextLine());
        dao.delete(id);
    }

    private static Evaluation readEvaluationData(Scanner scanner, int id, boolean includeId) {
        System.out.print("Titre: ");
        String titre = scanner.nextLine();

        System.out.print("Description: ");
        String description = scanner.nextLine();

        System.out.print("Type (qcm, examen, devoir, projet, tp): ");
        EvaluationType type = EvaluationType.fromDbValue(scanner.nextLine());

        System.out.print("Duree en minutes: ");
        int duree = Integer.parseInt(scanner.nextLine());

        System.out.print("Note max: ");
        float noteMax = Float.parseFloat(scanner.nextLine());

        System.out.print("Note passage: ");
        float notePassage = Float.parseFloat(scanner.nextLine());

        System.out.print("Nombre de tentatives: ");
        int nbTentatives = Integer.parseInt(scanner.nextLine());

        boolean ordreAleatoire = readBoolean(scanner, "Ordre aleatoire (true/false): ");
        boolean afficherCorrec = readBoolean(scanner, "Afficher correction (true/false): ");

        LocalDateTime dateDebut = readDateTime(scanner, "Date debut (yyyy-MM-dd HH:mm:ss): ");
        LocalDateTime dateFin = dateDebut.plusMinutes(duree);
        System.out.println("Date fin calculee automatiquement: " + dateFin.format(DATE_TIME_FORMATTER));

        System.out.print("Statut (brouillon, publie, ferme): ");
        EvaluationStatut statut = EvaluationStatut.fromDbValue(scanner.nextLine());

        if (includeId) {
            return new Evaluation(id, titre, description, type, duree, noteMax, notePassage,
                    nbTentatives, ordreAleatoire, afficherCorrec, dateDebut, dateFin, statut, null);
        }
        return new Evaluation(titre, description, type, duree, noteMax, notePassage,
                nbTentatives, ordreAleatoire, afficherCorrec, dateDebut, dateFin, statut);
    }

    private static void addQuestion(Scanner scanner, IService<Question> dao) {
        Question question = readQuestionData(scanner, 0, false);
        dao.add(question);
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
        System.out.print("Enter question id to update: ");
        int id = Integer.parseInt(scanner.nextLine());

        Question current = dao.getById(id);
        if (current == null) {
            System.out.println("No question found with id " + id);
            return;
        }

        Question updated = readQuestionData(scanner, id, true);
        dao.update(updated);
    }

    private static void deleteQuestion(Scanner scanner, IService<Question> dao) {
        System.out.print("Enter question id to delete: ");
        int id = Integer.parseInt(scanner.nextLine());
        dao.delete(id);
    }

    private static Question readQuestionData(Scanner scanner, int id, boolean includeId) {
        System.out.print("Question texte: ");
        String texte = scanner.nextLine();

        System.out.print("Type (qcm, vrai_faux, texte_libre, correspondance): ");
        QuestionType type = QuestionType.fromDbValue(scanner.nextLine());

        System.out.print("Points: ");
        float points = Float.parseFloat(scanner.nextLine());

        System.out.print("Explication: ");
        String explication = scanner.nextLine();

        System.out.print("Image URL: ");
        String imageUrl = scanner.nextLine();

        System.out.print("Ordre: ");
        int ordre = Integer.parseInt(scanner.nextLine());

        System.out.print("Evaluation ID: ");
        int evaluationId = Integer.parseInt(scanner.nextLine());

        if (includeId) {
            return new Question(id, texte, type, points, explication, imageUrl, ordre, evaluationId);
        }
        return new Question(texte, type, points, explication, imageUrl, ordre, evaluationId);
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
}
