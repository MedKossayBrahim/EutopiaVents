package com.esprit.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    // Configuration pour JavaMail (Gmail)
    private static final String GMAIL_USERNAME = "youssefharrane7@gmail.com"; // Remplacez par votre email Gmail
    private static final String GMAIL_PASSWORD = "omjp gzfi utbp brqx"; // Remplacez par votre mot de passe d'application
    
    /**
     * Envoie un email en utilisant JavaMail
     * 
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param message Contenu de l'email
     * @return true si l'email a été envoyé avec succès, false sinon
     */
    public boolean sendEmail(String to, String subject, String message) {
        // Pour le développement, on simule simplement l'envoi d'un email
        // et on affiche les détails dans la console
        System.out.println("\n=== SIMULATION D'ENVOI D'EMAIL ===");
        System.out.println("À: " + to);
        System.out.println("Sujet: " + subject);
        System.out.println("Message: " + message);
        System.out.println("=== FIN DE LA SIMULATION ===\n");

        // En environnement de production, décommentez le code ci-dessous
        // et configurez correctement les informations de votre compte Gmail


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

        // Toujours retourner true en mode simulation
    }
    
    /**
     * Envoie un email de confirmation de réservation
     * 
     * @param to Adresse email du destinataire
     * @param userName Nom de l'utilisateur
     * @param materielNom Nom du matériel réservé
     * @param quantite Quantité réservée
     * @param dateDebut Date de début de la réservation
     * @param dateFin Date de fin de la réservation
     * @param prixTotal Prix total de la réservation
     * @param estPaye Indique si la réservation a été payée
     * @return true si l'email a été envoyé avec succès, false sinon
     */
    public boolean sendReservationConfirmation(String to, String userName, String materielNom, 
                                              int quantite, String dateDebut, String dateFin, 
                                              double prixTotal, boolean estPaye) {
        String statusText = estPaye ? "Payée" : "En attente de paiement";
        
        String message = "Bonjour " + userName + ",\n\n" +
                "Nous vous confirmons votre réservation de matériel chez Eutopia Events.\n\n" +
                "Détails de la réservation :\n" +
                "- Matériel : " + materielNom + "\n" +
                "- Quantité : " + quantite + "\n" +
                "- Date de début : " + dateDebut + "\n" +
                "- Date de fin : " + dateFin + "\n" +
                "- Prix total : " + prixTotal + " TND\n" +
                "- Statut : " + statusText + "\n\n" +
                "Merci de votre confiance,\n" +
                "L'équipe Eutopia Events";
        
        return sendEmail(to, "Confirmation de votre réservation - Eutopia Events", message);
    }
} 