package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Chapitre;
import models.Cours;
import utils.AIService;

@SuppressWarnings({
    "FieldCanBeLocal",  // Les champs @FXML sont assignés par le framework
    "unused"            // Les méthodes sont appelées par FXML
})
public class ChatBotController {

    // ── Données du cours/chapitre courant ───────
    public static Cours    coursActuel    = null;
    public static Chapitre chapitreActuel = null;

    @FXML private Label      lblContexte;
    @FXML private VBox       chatBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField  tfMessage;

    // System prompt de base
    private String systemPrompt;

    @FXML
    void initialize() {
        try {
            System.out.println("🔧 Initialisation ChatBotController...");

            // Construire le contexte selon le cours/chapitre
            String contexte = "";
            if (coursActuel != null && chapitreActuel != null) {
                contexte = "Cours : " + coursActuel.getTitre() +
                        " | Chapitre : " + chapitreActuel.getTitre();
                lblContexte.setText("📚 " + contexte);

                systemPrompt =
                        "Tu es un assistant pédagogique pour la plateforme EduCore. " +
                                "Tu aides les étudiants à comprendre leurs cours. " +
                                "Le cours actuel est : '" + coursActuel.getTitre() + "'. " +
                                "Le chapitre actuel est : '" + chapitreActuel.getTitre() + "'. " +
                                "Description : " + chapitreActuel.getDescription() + ". " +
                                "Réponds toujours en français, de façon claire et pédagogique. " +
                                "Utilise des exemples simples et concrets.";
            } else {
                lblContexte.setText("📚 Assistant général EduCore");
                systemPrompt =
                        "Tu es un assistant pédagogique pour la plateforme EduCore. " +
                                "Tu aides les étudiants à comprendre leurs cours. " +
                                "Réponds toujours en français, de façon claire et pédagogique.";
            }

            // Message de bienvenue
            ajouterMessageBot("👋 Bonjour ! Je suis ton assistant IA. " +
                    "Je peux t'aider à :\n" +
                    "• 📝 Résumer le chapitre\n" +
                    "• ❓ Générer un quiz\n" +
                    "• 💡 Identifier les points clés\n" +
                    "• 💬 Répondre à tes questions\n\n" +
                    "Comment puis-je t'aider ?");

            System.out.println("✅ ChatBotController initialisé avec succès");
        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de l'initialisation de ChatBotController :");
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Résumer le chapitre ─────────────────────
    @FXML
    public void resumerChapitre(ActionEvent event) {
        String prompt = "Fais un résumé clair et structuré du chapitre '" +
                chapitreActuel.getTitre() + "'. " +
                "Description : " + chapitreActuel.getDescription() + ". " +
                "Le résumé doit être en points numérotés, facile à retenir.";
        ajouterMessageUser("📝 Résume ce chapitre");
        appellerIA(prompt);
    }

    // ── Générer un Quiz ─────────────────────────
    @FXML
    public void genererQuiz(ActionEvent event) {
        String prompt = "Génère un quiz de 5 questions à choix multiples " +
                "sur le chapitre '" + chapitreActuel.getTitre() + "'. " +
                "Pour chaque question, donne 4 choix (A, B, C, D) " +
                "et indique la bonne réponse à la fin. " +
                "Niveau : " + coursActuel.getNiveau();
        ajouterMessageUser("❓ Génère un quiz");
        appellerIA(prompt);
    }

    // ── Points clés ─────────────────────────────
    @FXML
    public void pointsCles(ActionEvent event) {
        String prompt = "Liste les 5 points clés les plus importants " +
                "à retenir du chapitre '" + chapitreActuel.getTitre() + "'. " +
                "Utilise des emojis pour rendre la liste visuelle et mémorable.";
        ajouterMessageUser("💡 Points clés du chapitre");
        appellerIA(prompt);
    }

    // ── Envoyer un message personnalisé ─────────
    @FXML
    public void envoyerMessage(ActionEvent event) {
        String message = tfMessage.getText().trim();
        if (message.isEmpty()) return;

        ajouterMessageUser(message);
        tfMessage.clear();
        appellerIA(message);
    }

    // ── Effacer le chat ─────────────────────────
    @FXML
    public void effacerChat(ActionEvent event) {
        chatBox.getChildren().clear();
        ajouterMessageBot("Chat effacé. Comment puis-je t'aider ?");
    }

    // ── Fermer la fenêtre ────────────────────────
    @FXML
    public void fermer(ActionEvent event) {
        Stage stage = (Stage) tfMessage.getScene().getWindow();
        stage.close();
    }

    // ── Appeler l'API Claude dans un thread ─────
    private void appellerIA(String message) {
        // Afficher un indicateur de chargement
        ajouterMessageBot("⏳ En cours de réflexion...");

        // Appel API dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            String reponse = AIService.envoyer(systemPrompt, message);

            // Mettre à jour l'UI depuis le thread JavaFX
            Platform.runLater(() -> {
                // Supprimer le message de chargement
                if (!chatBox.getChildren().isEmpty()) {
                    chatBox.getChildren().remove(
                            chatBox.getChildren().size() - 1
                    );
                }
                ajouterMessageBot(reponse);
                scrollToBottom();
            });
        }).start();
    }

    // ── Ajouter message Utilisateur ─────────────
    private void ajouterMessageUser(String message) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5));

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(450);
        label.setStyle(
                "-fx-background-color: #1a5276; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 15; " +
                        "-fx-background-radius: 15 15 0 15; " +
                        "-fx-font-size: 13;"
        );

        hbox.getChildren().add(label);
        chatBox.getChildren().add(hbox);
        scrollToBottom();
    }

    // ── Ajouter message Bot ─────────────────────
    private void ajouterMessageBot(String message) {
        HBox hbox = new HBox(8);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5));

        Label avatar = new Label("🤖");
        avatar.setStyle("-fx-font-size: 20;");

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(450);
        label.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-padding: 10 15; " +
                        "-fx-background-radius: 15 15 15 0; " +
                        "-fx-font-size: 13; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 15 15 15 0;"
        );

        hbox.getChildren().addAll(avatar, label);
        chatBox.getChildren().add(hbox);
        scrollToBottom();
    }

    // ── Scroller vers le bas ─────────────────────
    private void scrollToBottom() {
        Platform.runLater(() ->
                scrollPane.setVvalue(1.0)
        );
    }
}