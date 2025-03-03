package com.esprit.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailServiceY {
    // Configuration pour JavaMail (Gmail)
    private static final String GMAIL_USERNAME = "youssefharrane7@gmail.com";
    private static final String GMAIL_PASSWORD = "omjp gzfi utbp brqx";


    public boolean sendEmail(String to, String subject, String message) {
        System.out.println("\n=== SIMULATION D'ENVOI D'EMAIL ===");
        System.out.println("À: " + to);
        System.out.println("Sujet: " + subject);
        System.out.println("Message: " + message);
        System.out.println("=== FIN DE LA SIMULATION ===\n");


        // Configuration des propriétés pour le serveur SMTP de Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Créer une session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
            }
        });

        try {
            // Créer un message
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(GMAIL_USERNAME));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            // Envoyer le message
            Transport.send(mimeMessage);
            System.out.println("Email envoyé avec succès via JavaMail à " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email via JavaMail: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }


}