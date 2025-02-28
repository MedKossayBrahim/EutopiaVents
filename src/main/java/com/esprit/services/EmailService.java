package com.esprit.services;

import com.esprit.models.Evenement;
import com.esprit.models.Reservations;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

public class EmailService {
    private final String username = "raefhossni@gmail.com"; // Remplacez par votre email
    private final String password = "tkabuwcphhppykix"; // Mot de passe d'application Gmail

    public void envoyerBillet(String emailDestinataire, Reservations reservation, Evenement evenement) {
        try {
            // Générer le PDF du billet
            String cheminBillet = genererBilletPDF(reservation, evenement);

            // Configuration pour Gmail
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject("Votre billet pour " + evenement.getTitre());

            // Corps du message
            String corps = String.format(
                    "Cher client,\n\n" +
                            "Merci pour votre réservation à l'événement %s.\n" +
                            "Voici les détails de votre réservation :\n" +
                            "- Date : %s\n" +
                            "- Lieu : %s\n" +
                            "- Quantité : %d\n" +
                            "- Prix total : %.2f TND\n\n" +
                            "Votre billet est joint à ce mail.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe Eutopia",
                    evenement.getTitre(),
                    evenement.getDateDebut(),
                    evenement.getLieuNom(),
                    reservation.getQuantite(),
                    reservation.getPrixTotal()
            );

            // Partie 1 : Texte
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(corps);

            // Partie 2 : Pièce jointe (PDF)
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(cheminBillet));

            // Combiner les parties
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Envoyer le message
            Transport.send(message);

            // Supprimer le fichier PDF temporaire
            new File(cheminBillet).delete();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi du mail: " + e.getMessage());
        }
    }

    private String genererBilletPDF(Reservations reservation, Evenement evenement) {
        String cheminFichier = "billet_" + reservation.getId() + ".pdf";
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Titre
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Billet - " + evenement.getTitre());
                contentStream.endText();

                // Détails
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Numéro de réservation: " + reservation.getId());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Date: " + evenement.getDateDebut());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Lieu: " + evenement.getLieuNom());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Quantité: " + reservation.getQuantite());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Prix total: " + reservation.getPrixTotal() + " TND");
                contentStream.endText();
            }

            document.save(cheminFichier);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage());
        }
        return cheminFichier;
    }
}