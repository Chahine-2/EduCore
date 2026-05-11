package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Chapitre;
import models.Cours;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import services.ServiceFeedback;
import utils.NavigationManager;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
@SuppressWarnings({
    "FieldCanBeLocal",  // Les champs @FXML sont assignés par le framework
    "unused",           // Les méthodes sont appelées par FXML
    "UnusedAssignment"  // Certaines variables assignées par FXML
})
public class LectureChapitreController {

    // ── Données partagées depuis EtudiantController ──────────────
    public static Cours       coursActuel    = null;
    public static Chapitre    chapitreActuel = null;
    public static List<Chapitre> tousChapitres = null;

    // ── Navbar ───────────────────────────────────────────────────
    @FXML private Label        lblNomCours;
    @FXML private Label        lblTitreNavbar;
    @FXML private ProgressBar  progressBar;
    @FXML private Label        lblProgression;
    @FXML private Button       btnPrecedent;
    @FXML private Button       btnSuivant;

    // ── Sidebar ──────────────────────────────────────────────────
    @FXML private ListView<Chapitre> listViewSommaire;
    @FXML private Label              lblSommaireTotal;

    // ── Contenu principal ────────────────────────────────────────
    @FXML private Label   lblNumero;
    @FXML private Label   lblTitreChapitre;
    @FXML private Label   lblBadgeType;
    @FXML private Label   lblDureeEstimee;
    @FXML private Label   lblDescription;
    @FXML private VBox    boxApercuChapitre;
    @FXML private VBox    boxSupport;
    @FXML private Label   lblUrl;
    @FXML private ComboBox<String> cbLangueLecture;
    @FXML private Label   lblLectureEtat;
    @FXML private Button  btnLireApercu;
    @FXML private Button  btnStopLecture;
    @FXML private TextArea taFeedback;
    @FXML private Label lblFeedbackEtat;


    /** When set, {@link #retour} returns to the embedded catalogue instead of replacing the scene root. */
    private Runnable embeddedBackToCatalog;

    private int indexActuel = 0;
    private Process ttsProcess;
    private volatile boolean arretManuel = false;
    private final ServiceFeedback serviceFeedback = new ServiceFeedback();

    // ─────────────────────────────────────────────────────────────
    @FXML
    void initialize() {
        try {
            System.out.println("🔧 Initialisation LectureChapitreController...");

            if (tousChapitres == null || tousChapitres.isEmpty()) {
                System.out.println("⚠️ Aucun chapitre disponible");
                return;
            }

            // Trouver l'index du chapitre actuel
            if (chapitreActuel != null) {
                for (int i = 0; i < tousChapitres.size(); i++) {
                    if (tousChapitres.get(i).getId() == chapitreActuel.getId()) {
                        indexActuel = i;
                        break;
                    }
                }
            }

             // Configurer le sommaire (affichage personnalisé)
             listViewSommaire.setCellFactory(lv -> new ListCell<Chapitre>() {
                 @Override
                 protected void updateItem(Chapitre ch, boolean empty) {
                     super.updateItem(ch, empty);
                     if (empty || ch == null) {
                         setText(null);
                         setGraphic(null);
                         setStyle("-fx-background-color: transparent;");
                     } else {
                         String icone = getIconeType(ch.getTypeContenu());
                         // Ajouter un badge "Masqué" si le chapitre n'est pas visible
                         String masque = !ch.isVisible() ? " 👁‍🗨" : "";
                         setText(icone + "  " + ch.getOrdre() + ". " + ch.getTitre() + masque);
                         setStyle(
                             "-fx-text-fill: #475569;" +
                             "-fx-font-size: 13;" +
                             "-fx-padding: 10 15;" +
                             "-fx-background-color: transparent;" +
                             "-fx-border-color: transparent;" +
                             "-fx-cursor: hand;"
                         );

                         // Style conditionnel pour l'élément sélectionné
                         if (isSelected()) {
                             setStyle(getStyle() + "-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-font-weight: bold; -fx-background-radius: 6;");
                         }
                     }
                 }
             });

            // Remplir le sommaire
            listViewSommaire.getItems().setAll(tousChapitres);
            lblSommaireTotal.setText(tousChapitres.size() + " chapitre(s)");
            configurerLanguesLecture();
            updateLectureEtat(false, false);

            // Afficher le chapitre courant
            afficherChapitre(indexActuel);
            System.out.println("✅ LectureChapitreController initialisé avec succès");
        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de l'initialisation de LectureChapitreController :");
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Afficher un chapitre par index ───────────────────────────
    private void afficherChapitre(int index) {
        if (index < 0 || index >= tousChapitres.size()) return;
        arreterLectureInterne();

        indexActuel = index;
        Chapitre ch = tousChapitres.get(index);
        chapitreActuel = ch;

        // ─ Navbar ─
        if (coursActuel != null) {
            lblNomCours.setText("📚 " + coursActuel.getTitre());
        }
        lblTitreNavbar.setText(ch.getTitre());

        // ─ Progression ─
        double pct = (double)(index + 1) / tousChapitres.size();
        progressBar.setProgress(pct);
        lblProgression.setText((index + 1) + " / " + tousChapitres.size());

        // ─ Boutons navigation ─
        btnPrecedent.setDisable(index == 0);
        btnSuivant.setDisable(index == tousChapitres.size() - 1);

        // ─ Sélectionner dans le sommaire ─
        listViewSommaire.getSelectionModel().select(index);
        listViewSommaire.scrollTo(index);

        // ─ Contenu ─
        lblNumero.setText("CHAPITRE " + ch.getOrdre());
        lblTitreChapitre.setText(ch.getTitre());
        lblBadgeType.setText(getLabelType(ch.getTypeContenu()));
        lblBadgeType.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20;");
        lblDureeEstimee.setText("⏱  " + ch.getDureeMinutes() + " min");
        lblDescription.setText(
            ch.getDescription() != null && !ch.getDescription().isEmpty()
                ? ch.getDescription()
                : "Aucune description disponible pour ce chapitre."
        );

        // ─ URL / Support ─
        boolean hasUrl = ch.getUrlContenu() != null && !ch.getUrlContenu().isEmpty();
        boxSupport.setVisible(hasUrl);
        boxSupport.setManaged(hasUrl);
        if (hasUrl) {
            String url = ch.getUrlContenu();
            // N'afficher que le nom du fichier s'il s'agit d'un chemin local
            String displayName = url;
            if (url.contains("/") || url.contains("\\")) {
                int lastIndex = Math.max(url.lastIndexOf("/"), url.lastIndexOf("\\"));
                displayName = url.substring(lastIndex + 1);
            }
            lblUrl.setText(displayName);
        }
    }

    @FXML
    public void envoyerFeedback(ActionEvent event) {
        if (chapitreActuel == null || coursActuel == null) {
            if (lblFeedbackEtat != null) {
                lblFeedbackEtat.setText("Impossible d'envoyer: chapitre/cours non chargé.");
            }
            return;
        }
        String message = taFeedback != null ? taFeedback.getText() : "";
        if (message == null || message.trim().isEmpty()) {
            if (lblFeedbackEtat != null) {
                lblFeedbackEtat.setText("Veuillez saisir un feedback avant envoi.");
            }
            return;
        }

        boolean ok = serviceFeedback.addFeedback(coursActuel.getId(), chapitreActuel.getId(), message.trim());
        if (ok) {
            if (taFeedback != null) {
                taFeedback.clear();
            }
            if (lblFeedbackEtat != null) {
                lblFeedbackEtat.setText("Merci. Votre feedback anonyme a été envoyé.");
            }
        } else if (lblFeedbackEtat != null) {
            lblFeedbackEtat.setText("Échec d'envoi du feedback. Réessayez.");
        }
    }

    // ── Clic sur le sommaire ─────────────────────────────────────
    @FXML
    private void selectionnerDepuisSommaire(MouseEvent event) {
        int idx = listViewSommaire.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {  // Toujours afficher le chapitre au clic, peu importe quel chapitre
            afficherChapitre(idx);
            listViewSommaire.refresh(); // Pour rafraîchir le style sélectionné
        }
    }

    // ── Navigation Précédent ─────────────────────────────────────
    @FXML
    public void chapitrePrec(ActionEvent event) {
        if (indexActuel > 0) {
            afficherChapitre(indexActuel - 1);
            listViewSommaire.refresh();
        }
    }

    // ── Navigation Suivant ───────────────────────────────────────
    @FXML
    public void chapitreNext(ActionEvent event) {
        if (indexActuel < tousChapitres.size() - 1) {
            afficherChapitre(indexActuel + 1);
            listViewSommaire.refresh();
        }
    }

    public void setDashboardEmbedMode(boolean enabled, Runnable backToCatalog) {
        this.embeddedBackToCatalog = (enabled && backToCatalog != null) ? backToCatalog : null;
    }

    // ── Retour à la liste des cours ──────────────────────────
    @FXML
    public void retour(ActionEvent event) {
        try {
            arreterLectureInterne();
            if (embeddedBackToCatalog != null) {
                embeddedBackToCatalog.run();
                return;
            }
            Scene scene = lblTitreChapitre.getScene();
            if (scene != null) {
                NavigationManager.navigateTo(scene, "/Etudiant.fxml");
            } else {
                System.out.println("⚠️ Erreur : Scène non trouvée");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur retour : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Actions sur le support ───────────────────────────────────
    @FXML
    public void ouvrirSupport(ActionEvent event) {
        if (chapitreActuel != null && chapitreActuel.getUrlContenu() != null) {
            String url = chapitreActuel.getUrlContenu();
            try {
                if (url.startsWith("http")) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    File file = new File(url);
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        showAlert("Erreur", "Fichier introuvable", "Le fichier spécifié n'existe pas : " + url);
                    }
                }
            } catch (Exception e) {
                showAlert("Erreur", "Ouverture impossible", "Impossible d'ouvrir le support : " + e.getMessage());
            }
        }
    }
    @FXML
    public void ouvrirChatBot(ActionEvent event) {
        try {
            // Passer les données au ChatBot
            ChatBotController.coursActuel    = coursActuel;
            ChatBotController.chapitreActuel = chapitreActuel;

            NavigationManager.openNewWindow("/ChatBot.fxml", "🤖 Assistant IA — " + chapitreActuel.getTitre());
        } catch (Exception e) {
            System.out.println("❌ Erreur ChatBot : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'assistant", e.getMessage());
        }
    }
    @FXML
    public void telechargerSupport(ActionEvent event) {
        // Le téléchargement peut être une simple ouverture ou copie.
        // On va réutiliser l'ouverture car Desktop.open() gère souvent le téléchargement/visualisation pour les fichiers.
        if (chapitreActuel != null && chapitreActuel.getUrlContenu() != null) {
            String url = chapitreActuel.getUrlContenu();
            try {
                if (url.startsWith("http")) {
                    // Si c'est un lien web, on l'ouvre dans le navigateur
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    // Si c'est un fichier local
                    File file = new File(url);
                    if (file.exists()) {
                        showAlert("Téléchargement", "Support disponible", "Le fichier est déjà présent localement. Nous allons l'ouvrir pour vous.");
                        Desktop.getDesktop().open(file);
                    } else {
                        showAlert("Erreur", "Fichier introuvable", "Le fichier spécifié n'existe pas : " + url);
                    }
                }
            } catch (Exception e) {
                showAlert("Erreur", "Téléchargement impossible", "Impossible de traiter la demande : " + e.getMessage());
            }
        }
    }

    @FXML
    public void imprimerApercuPdf(ActionEvent event) {
        if (lblDescription == null || lblDescription.getText() == null || lblDescription.getText().isBlank()) {
            showAlert("Impression PDF", "Aucun contenu", "Aucun aperçu de chapitre à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'aperçu en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
        fileChooser.setInitialFileName(genererNomPdf());
        File outputFile = fileChooser.showSaveDialog(lblDescription.getScene().getWindow());
        if (outputFile == null) {
            return;
        }

        String titre = lblTitreChapitre != null && lblTitreChapitre.getText() != null
            ? lblTitreChapitre.getText()
            : "Aperçu du chapitre";
        String contenu = lblDescription.getText();

        try {
            genererPdfTexte(outputFile, titre, contenu);
            showAlert("Export PDF", "Succès", "Le PDF a été généré avec succès.");
        } catch (IOException e) {
            showAlert("Export PDF", "Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    private String genererNomPdf() {
        String base = (lblTitreChapitre != null && lblTitreChapitre.getText() != null && !lblTitreChapitre.getText().isBlank())
            ? lblTitreChapitre.getText().replaceAll("[^a-zA-Z0-9-_ ]", "").trim().replace(" ", "_")
            : "apercu_chapitre";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        return base + "_" + timestamp + ".pdf";
    }

    private void genererPdfTexte(File outputFile, String titre, String contenu) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50f;
            float titleSize = 16f;
            float bodySize = 11f;
            float lineHeight = 16f;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();
            float printableWidth = mediaBox.getWidth() - 2 * margin;
            float y = mediaBox.getHeight() - margin;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                // Title
                cs.beginText();
                cs.setFont(titleFont, titleSize);
                cs.newLineAtOffset(margin, y);
                cs.showText(titre);
                cs.endText();
                y -= 28f;

                // Body
                List<String> lines = wrapText(contenu, bodyFont, bodySize, printableWidth);
                cs.setFont(bodyFont, bodySize);
                for (String line : lines) {
                    if (y <= margin) {
                        break;
                    }
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    cs.showText(line);
                    cs.endText();
                    y -= lineHeight;
                }
            }

            // Additional pages when needed
            List<String> remaining = wrapText(contenu, bodyFont, bodySize, printableWidth);
            int startIndex = (int) Math.max(0, Math.floor((mediaBox.getHeight() - margin - 28f - margin) / lineHeight));
            while (startIndex < remaining.size()) {
                PDPage nextPage = new PDPage(PDRectangle.A4);
                document.addPage(nextPage);
                try (PDPageContentStream cs = new PDPageContentStream(document, nextPage)) {
                    float yNext = nextPage.getMediaBox().getHeight() - margin;
                    int linesPerPage = (int) Math.floor((nextPage.getMediaBox().getHeight() - 2 * margin) / lineHeight);
                    cs.setFont(bodyFont, bodySize);
                    for (int i = startIndex; i < Math.min(startIndex + linesPerPage, remaining.size()); i++) {
                        cs.beginText();
                        cs.newLineAtOffset(margin, yNext);
                        cs.showText(remaining.get(i));
                        cs.endText();
                        yNext -= lineHeight;
                    }
                    startIndex += linesPerPage;
                }
            }

            document.save(outputFile);
        }
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.replace("\r", "").split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                lines.add(" ");
                continue;
            }
            String[] words = paragraph.split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                float width = font.getStringWidth(candidate) / 1000f * fontSize;
                if (width > maxWidth && !line.isEmpty()) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    @FXML
    public void lireApercu(ActionEvent event) {
        String texte = lblDescription != null ? lblDescription.getText() : null;
        if (texte == null || texte.trim().isEmpty()) {
            showAlert("Lecture vocale", "Aucun texte à lire", "Le chapitre ne contient pas de description.");
            return;
        }

        if (!isWindows()) {
            showAlert("Lecture vocale non disponible", "Système non pris en charge", "Cette version utilise la synthèse vocale Windows.");
            return;
        }

        arreterLectureInterne();
        arretManuel = false;
        updateLectureEtat(true, false);
        String languageCode = getSelectedLanguageCode();
        String encodedText = Base64.getEncoder().encodeToString(texte.getBytes(StandardCharsets.UTF_8));
        try {
            Path scriptPath = creerScriptTtsTemporaire();
            ProcessBuilder pb = new ProcessBuilder(
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                scriptPath.toString(),
                encodedText,
                languageCode
            );
            pb.redirectErrorStream(true);
            ttsProcess = pb.start();
            monitorerLecture(ttsProcess);
        } catch (IOException e) {
            updateLectureEtat(false, true);
            showAlert("Lecture vocale", "Erreur de lecture", "Impossible de démarrer la synthèse vocale : " + e.getMessage());
        }
    }

    @FXML
    public void arreterLecture(ActionEvent event) {
        arreterLectureInterne();
    }

    private void arreterLectureInterne() {
        if (ttsProcess != null && ttsProcess.isAlive()) {
            arretManuel = true;
            ttsProcess.destroy();
            try {
                if (!ttsProcess.waitFor(300, TimeUnit.MILLISECONDS)) {
                    ttsProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        ttsProcess = null;
        updateLectureEtat(false, false);
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    private void configurerLanguesLecture() {
        if (cbLangueLecture == null) return;
        cbLangueLecture.getItems().setAll(
            "Français (fr-FR)",
            "English (en-US)",
            "العربية (ar-SA)"
        );
        cbLangueLecture.setValue("Français (fr-FR)");
    }

    private String getSelectedLanguageCode() {
        if (cbLangueLecture == null || cbLangueLecture.getValue() == null) {
            return "fr-FR";
        }
        String value = cbLangueLecture.getValue();
        int start = value.lastIndexOf('(');
        int end = value.lastIndexOf(')');
        if (start >= 0 && end > start) {
            return value.substring(start + 1, end).trim();
        }
        return "fr-FR";
    }

    private void monitorerLecture(Process process) {
        Thread watcher = new Thread(() -> {
            int exitCode = -1;
            String output = "";
            try {
                exitCode = process.waitFor();
                output = lireSortie(process.getInputStream());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
            }

            final int codeFinal = exitCode;
            final String sortieFinale = output;
            Platform.runLater(() -> {
                ttsProcess = null;
                if (arretManuel) {
                    arretManuel = false;
                    updateLectureEtat(false, false);
                    return;
                }
                if (codeFinal == 0) {
                    updateLectureEtat(false, false);
                } else {
                    updateLectureEtat(false, true);
                    String details = (sortieFinale == null || sortieFinale.isBlank())
                        ? "La synthèse vocale a échoué. Vérifiez les voix installées sur Windows."
                        : formatErreurLecture(sortieFinale);
                    showAlert("Lecture vocale", "Échec de lecture", details);
                }
            });
        });
        watcher.setDaemon(true);
        watcher.start();
    }

    private String lireSortie(InputStream stream) throws IOException {
        byte[] data = stream.readAllBytes();
        return new String(data, StandardCharsets.UTF_8).trim();
    }

    private Path creerScriptTtsTemporaire() throws IOException {
        String script = "param([string]$encoded, [string]$culture)\n"
            + "$ErrorActionPreference = 'Stop'\n"
            + "Add-Type -AssemblyName System.Speech\n"
            + "$txt = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($encoded))\n"
            + "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer\n"
            + "$voice = $speak.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.Name -eq $culture -or $_.VoiceInfo.Culture.Name -like ($culture + '*') } | Select-Object -First 1\n"
            + "if (-not $voice) {\n"
            + "  $langOnly = $culture.Split('-')[0]\n"
            + "  $voice = $speak.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.Name -like ($langOnly + '*') } | Select-Object -First 1\n"
            + "}\n"
            + "if ($voice) { $speak.SelectVoice($voice.VoiceInfo.Name) }\n"
            + "$speak.Rate = 0\n"
            + "$speak.Speak($txt)\n";
        Path scriptPath = Files.createTempFile("educore-tts-", ".ps1");
        Files.writeString(scriptPath, script, StandardCharsets.UTF_8);
        scriptPath.toFile().deleteOnExit();
        return scriptPath;
    }

    private String formatErreurLecture(String raw) {
        String cleaned = raw.replace('\r', ' ').replace('\n', ' ').trim();
        if (cleaned.length() > 260) {
            cleaned = cleaned.substring(0, 260) + "...";
        }
        return cleaned;
    }

    private void updateLectureEtat(boolean enLecture, boolean erreur) {
        if (lblLectureEtat != null) {
            if (erreur) {
                lblLectureEtat.setText("⚠ Erreur de lecture");
                lblLectureEtat.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12; -fx-font-weight: bold; -fx-background-color: #fee2e2; -fx-padding: 6 10; -fx-background-radius: 999;");
            } else if (enLecture) {
                lblLectureEtat.setText("🔊 En lecture...");
                lblLectureEtat.setStyle("-fx-text-fill: #065f46; -fx-font-size: 12; -fx-font-weight: bold; -fx-background-color: #d1fae5; -fx-padding: 6 10; -fx-background-radius: 999;");
            } else {
                lblLectureEtat.setText("⏸ Arrêté");
                lblLectureEtat.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12; -fx-font-weight: bold; -fx-background-color: #f1f5f9; -fx-padding: 6 10; -fx-background-radius: 999;");
            }
        }
        if (btnLireApercu != null) btnLireApercu.setDisable(enLecture);
        if (btnStopLecture != null) btnStopLecture.setDisable(!enLecture);
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private String getIconeType(String type) {
        if (type == null) return "📄";
        return switch (type.toLowerCase()) {
            case "video"  -> "🎬";
            case "pdf"    -> "📕";
            case "quiz"   -> "❓";
            case "texte"  -> "📝";
            default       -> "📄";
        };
    }

    private String getLabelType(String type) {
        if (type == null) return "Contenu";
        return switch (type.toLowerCase()) {
            case "video"  -> "🎬  Vidéo";
            case "pdf"    -> "📕  PDF";
            case "quiz"   -> "❓  Quiz";
            case "texte"  -> "📝  Texte";
            default       -> "📄  " + type;
        };
    }
}
