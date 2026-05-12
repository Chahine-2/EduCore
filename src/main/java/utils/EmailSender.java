package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Envoi d'email via Gmail SMTP (gratuit, aucun compte externe).
 *
 * CONFIGURATION REQUISE (une seule fois) :
 * ─────────────────────────────────────────
 * 1. Connectez-vous à : https://myaccount.google.com/security
 * 2. Activez "Validation en deux étapes" si pas déjà fait
 * 3. Allez sur : https://myaccount.google.com/apppasswords
 * 4. Créez un mot de passe d'application (App) → catégorie "Mail"
 * 5. Copiez le mot de passe généré (16 caractères) → collez-le dans APP_PASSWORD ci-dessous
 *
 * ⚠️  N'utilisez PAS votre mot de passe Gmail normal ici !
 */
public class EmailSender {

    // ── À CONFIGURER ──────────────────────────────────────────────────
    private static final String EXPEDITEUR    = "benhassineabdelkader2211@gmail.com";
    private static final String APP_PASSWORD = "xrhxjtouwgfsiila";
    private static final String DESTINATAIRE  = "benhssineabdelkader@gmail.com";
    // ─────────────────────────────────────────────────────────────────

    public static void envoyer(String sujet, String corps) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EXPEDITEUR, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EXPEDITEUR, "Site Éducatif"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(DESTINATAIRE));
            message.setSubject(sujet);
            message.setContent(corps, "text/plain; charset=UTF-8");            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à " + DESTINATAIRE);
        } catch (Exception e) {
            System.out.println("❌ Erreur envoi email : " + e.getMessage());
            // L'application continue même si l'email échoue
        }
    }
}