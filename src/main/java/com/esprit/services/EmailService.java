package com.esprit.services;

import com.esprit.models.Reservations;
import com.esprit.models.Evenement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.awt.Color;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EmailService {
    private final String username = "raefhossni@gmail.com";
    private final String password = "tkabuwcphhppykix";

    public void envoyerBillet(String emailDestinataire, Reservations reservation, Evenement evenement) throws SQLException {
        // Recharger l'événement complet depuis la base de données
        EvenementService evenementService = new EvenementService();
        evenement = evenementService.rechercherParId(evenement.getId());

        String cheminBillet = null;
        try {
            cheminBillet = genererBilletPDF(reservation, evenement);

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "EutopiaVents"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject("🎫 Votre billet pour " + evenement.getTitre());

            // Corps HTML de l'email
            String lieu = evenement.getLieuNom() != null ? evenement.getLieuNom() : evenement.getLieu_proprietaire();
            String emailHTML = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background: #fff; padding: 20px; }
                        .ticket-info { background: #f9f9f9; padding: 15px; margin: 15px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Confirmation de Réservation</h1>
                        </div>
                        <div class="content">
                            <h2>Merci pour votre réservation !</h2>
                            <p>Cher client,</p>
                            <p>Votre réservation pour <strong>%s</strong> a été confirmée.</p>
                            
                            <div class="ticket-info">
                                <h3>📋 Détails de votre réservation :</h3>
                                <p>🗓️ Date : %s</p>
                                <p>📍 Lieu : %s</p>
                                <p>🎟️ Quantité : %d</p>
                                <p>💰 Prix total : %.2f TND</p>
                            </div>
                            
                            <p>Votre billet électronique est joint à cet email.</p>
                            <p>Cordialement,<br>L'équipe EutopiaVents</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                    evenement.getTitre(),
                    evenement.getDateDebut(),
                    lieu,
                    reservation.getQuantite(),
                    reservation.getPrixTotal()
            );

            // Configuration du message multipart
            MimeMultipart multipart = new MimeMultipart("related");

            // Partie HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(emailHTML, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Pièce jointe PDF
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(cheminBillet));
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            Transport.send(message);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du mail: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi du mail: " + e.getMessage());
        } finally {
            // Nettoyage du fichier PDF
            if (cheminBillet != null) {
                new File(cheminBillet).delete();
            }
        }
    }

    private String genererBilletPDF(Reservations reservation, Evenement evenement) {
        String cheminFichier = "billet_" + reservation.getId() + ".pdf";
        String qrCodePath = "qr_" + reservation.getId() + ".png";

        try {
            // Générer le QR Code
            String lieu = evenement.getLieuNom() != null ? evenement.getLieuNom() : evenement.getLieu_proprietaire();
            String qrContent = String.format("ID: %d\nÉvénement: %s\nDate: %s\nLieu: %s",
                    reservation.getId(), evenement.getTitre(), evenement.getDateDebut(), lieu);

            generateQRCode(qrContent, qrCodePath, 200, 200);

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    float pageWidth = page.getMediaBox().getWidth();
                    float pageHeight = page.getMediaBox().getHeight();

                    // Fond coloré en dégradé
                    contentStream.setNonStrokingColor(new Color(76, 175, 80));
                    contentStream.addRect(0, pageHeight - 100, pageWidth, 100);
                    contentStream.fill();

                    contentStream.setNonStrokingColor(new Color(67, 160, 71));
                    contentStream.addRect(0, pageHeight - 120, pageWidth, 20);
                    contentStream.fill();

                    // Logo
                    try {
                        PDImageXObject logo = PDImageXObject.createFromFile("src/main/resources/Images/logo.png", document);
                        contentStream.drawImage(logo, 50, pageHeight - 90, 70, 70);
                    } catch (Exception e) {
                        System.err.println("Logo non trouvé");
                    }

                    // Titre principal
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(Color.WHITE);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 28);
                    contentStream.newLineAtOffset(150, pageHeight - 60);
                    contentStream.showText("BILLET ÉLECTRONIQUE");
                    contentStream.endText();

                    // Ligne de séparation
                    contentStream.setStrokingColor(new Color(224, 224, 224));
                    contentStream.setLineWidth(1);
                    contentStream.moveTo(50, pageHeight - 130);
                    contentStream.lineTo(pageWidth - 50, pageHeight - 130);
                    contentStream.stroke();

                    // Titre de l'événement
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(new Color(33, 33, 33));
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                    contentStream.newLineAtOffset(50, pageHeight - 180);
                    contentStream.showText(evenement.getTitre());
                    contentStream.endText();

                    // Informations principales
                    float yPosition = pageHeight - 240;
                    float leftColumn = 50;
                    float rightColumn = pageWidth / 2 + 50;

                    // Colonne gauche
                    addDetailLine(contentStream, "Date", evenement.getDateDebut(), leftColumn, yPosition);
                    addDetailLine(contentStream, "Lieu", lieu, leftColumn, yPosition - 40);
                    addDetailLine(contentStream, "Prix total", reservation.getPrixTotal() + " TND", leftColumn, yPosition - 80);

                    // Colonne droite
                    addDetailLine(contentStream, "N° de réservation", String.valueOf(reservation.getId()), rightColumn, yPosition);
                    addDetailLine(contentStream, "Quantité", String.valueOf(reservation.getQuantite()), rightColumn, yPosition - 40);

                    // QR Code
                    PDImageXObject qrCode = PDImageXObject.createFromFile(qrCodePath, document);
                    contentStream.drawImage(qrCode, pageWidth - 250, 100, 200, 200);

                    // Pied de page
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                    contentStream.setNonStrokingColor(new Color(97, 97, 97));
                    contentStream.newLineAtOffset(50, 50);
                    contentStream.showText("Ce billet est émis par EutopiaVents. Merci de le présenter lors de l'événement.");
                    contentStream.endText();

                    // Bordure décorative
                    contentStream.setStrokingColor(new Color(76, 175, 80));
                    contentStream.setLineWidth(2);
                    float margin = 20;
                    contentStream.addRect(margin, margin, pageWidth - 2 * margin, pageHeight - 2 * margin);
                    contentStream.stroke();
                }

                document.save(cheminFichier);
            }

            // Supprimer le fichier QR Code temporaire
            new File(qrCodePath).delete();

            return cheminFichier;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    private void addDetailLine(PDPageContentStream contentStream, String label, String value, float x, float y) throws IOException {
        // Label
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.setNonStrokingColor(new Color(97, 97, 97));
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label + ":");
        contentStream.endText();

        // Value
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.setNonStrokingColor(new Color(33, 33, 33));
        contentStream.newLineAtOffset(x, y - 20);
        contentStream.showText(value != null ? value : "Non spécifié");
        contentStream.endText();
    }

    private void generateQRCode(String content, String filePath, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            Path path = Paths.get(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }
}