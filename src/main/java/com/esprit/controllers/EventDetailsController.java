package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.models.Reservations;
import com.esprit.models.User;
import com.esprit.services.ReservationsService;
import com.esprit.services.EvenementService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class EventDetailsController {

    @FXML
    private Label titreLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label lieuLabel;



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
    private int evenementId;

    public EventDetailsController() throws SQLException {
    }

    public void afficherDetails(int evenementId) {
        this.evenementId = evenementId;
        Evenement evenement = evenementService.rechercherParId(evenementId);

        if (evenement != null) {
            titreLabel.setText(evenement.getTitre());
            descriptionLabel.setText(evenement.getDescription());
            String lieuAffiche = evenement.getLieuId() > 0 ? evenement.getLieuNom() : evenement.getLieu_proprietaire();
            lieuLabel.setText(lieuAffiche);

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

            // Vérifier si un utilisateur est connecté
            User currentUser = Eutopia.getCurrentUser();
            if (currentUser == null) {
                reserverButton.setDisable(true);
                reserverButton.setText("Connectez-vous pour réserver");
            }
        } else {
            System.err.println("Événement non trouvé avec l'ID : " + evenementId);
        }
    }

    @FXML
    private void reserverEvenement() {
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null) {
            // Afficher une alerte si l'utilisateur n'est pas connecté
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Connexion requise");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez vous connecter pour effectuer une réservation.");
            alert.showAndWait();
            return;
        }

        // Créer une nouvelle réservation avec l'ID de l'utilisateur connecté
        Evenement evenement = evenementService.rechercherParId(evenementId);
        Reservations reservation = new Reservations(0, evenement.getId(), currentUser.getUserID(), 1, evenement.getPrix(), "en_attente");
        reservationsService.ajouter(reservation);

        try {
            // Rediriger vers le panier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Panier.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Panier");
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) reserverButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la redirection vers le panier.");
            alert.showAndWait();
        }
    }
}
