package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.models.Reservations;
import com.esprit.services.ReservationsService;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class EventDetailsController {

    @FXML
    private Label titreLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label lieuLabel;
    @FXML
    private Label lieuInfoLabel;
    @FXML
    private Label heureLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private ImageView eventImageView;
    @FXML
    private Button reserverButton;

    private EvenementService evenementService = new EvenementService();
    private ReservationsService reservationsService = new ReservationsService();
    private static final int USER_ID = 10; // Remplacez par l'ID de l'utilisateur connecté
    private int evenementId; // ID de l'événement sélectionné

    public void afficherDetails(int evenementId) {
        this.evenementId = evenementId; // Stocker l'ID de l'événement
        Evenement evenement = evenementService.rechercherParId(evenementId);

        if (evenement != null) {
            titreLabel.setText(evenement.getTitre());
            descriptionLabel.setText(evenement.getDescription());
            String lieuAffiche = evenement.getLieuId() > 0 ? evenement.getLieuNom() : evenement.getLieu_proprietaire();
            lieuLabel.setText(lieuAffiche);
            lieuInfoLabel.setText(lieuAffiche);

            String dateDebut = evenement.getDateDebut().split(" ")[0];
            String dateFin = evenement.getDateFin().split(" ")[0];
            String heureDebut = evenement.getDateDebut().split(" ")[1];
            String heureFin = evenement.getDateFin().split(" ")[1];

            dateLabel.setText("Date : " + dateDebut + " à " + dateFin);
            timeLabel.setText("Horaires : " + heureDebut + " - " + heureFin);

            if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
                try {
                    Image image = new Image(evenement.getImage());
                    eventImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                    eventImageView.setImage(new Image(getClass().getResourceAsStream("/images/default-event.png")));
                }
            } else {
                eventImageView.setImage(new Image(getClass().getResourceAsStream("/images/default-event.png")));
            }

            reserverButton.setText(String.format("Réserver maintenant (%.2f TND)", evenement.getPrix()));
        } else {
            System.err.println("Événement non trouvé avec l'ID : " + evenementId);
        }
    }

    @FXML
    private void reserverEvenement() {
        // Créer une nouvelle réservation avec le prix initial de l'événement
        Evenement evenement = evenementService.rechercherParId(evenementId); // Utiliser l'ID stocké
        Reservations reservation = new Reservations(0, evenement.getId(), USER_ID, 1, evenement.getPrix(), "en_attente");
        reservationsService.ajouter(reservation); // Appel à la méthode ajouter qui met à jour le prix total

        // Rediriger vers le panier
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Panier.fxml"));
            Parent root = loader.load();

            // Ouvrir une nouvelle fenêtre pour le panier
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Panier");
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) reserverButton.getScene().getWindow();
            currentStage.hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
