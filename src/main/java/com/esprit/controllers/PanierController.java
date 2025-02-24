package com.esprit.controllers;

import com.esprit.models.Reservations;
import com.esprit.models.Evenement;
import com.esprit.models.User;
import com.esprit.services.ReservationsService;
import com.esprit.services.EvenementService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.scene.control.TextInputDialog;
import com.esprit.services.EmailService;

public class PanierController implements Initializable {

    @FXML
    private VBox reservationsContainer;
    @FXML
    private Label totalLabel;
    @FXML
    private Button retourButton;

    private ReservationsService reservationsService = new ReservationsService();
    private EvenementService evenementService = new EvenementService();
    private EmailService emailService = new EmailService();

    public PanierController() throws SQLException {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null) {
            // Si aucun utilisateur n'est connecté, afficher un message et retourner
            showNoUserMessage();
            return;
        }
        loadReservations(currentUser.getUserID());
    }

    private void showNoUserMessage() {
        reservationsContainer.getChildren().clear();
        Label messageLabel = new Label("Veuillez vous connecter pour voir votre panier");
        messageLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #666;");
        reservationsContainer.getChildren().add(messageLabel);
        totalLabel.setText("0.00 TND");
    }

    private void loadReservations(int userId) {
        reservationsContainer.getChildren().clear();
        double totalPanier = 0;

        var reservations = reservationsService.rechercherParUtilisateur(userId).stream()
                .filter(r -> "en_attente".equals(r.getStatut()))
                .toList();

        if (reservations.isEmpty()) {
            Label emptyLabel = new Label("Votre panier est vide");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #666;");
            reservationsContainer.getChildren().add(emptyLabel);
        } else {
            for (Reservations reservation : reservations) {
                Evenement event = evenementService.rechercherParId(reservation.getEvenementId());
                VBox card = createReservationCard(reservation, event);
                reservationsContainer.getChildren().add(card);
                totalPanier += reservation.getPrixTotal();
            }
        }

        totalLabel.setText(String.format("%.2f TND", totalPanier));
    }

    private VBox createReservationCard(Reservations reservation, Evenement event) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #ddd; -fx-border-radius: 5;");
        card.setPadding(new Insets(10));

        // Titre de l'événement
        Label titreLabel = new Label(event.getTitre());
        titreLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // Informations de l'événement
        String lieu = event.getLieuId() != 0 ? event.getLieuNom() : event.getLieu_proprietaire(); // Utiliser le nom du lieu
        Label infoLabel = new Label(String.format("Date: %s\nLieu: %s", event.getDateDebut(), lieu));

        // Prix et quantité
        HBox prixQuantiteBox = new HBox(20);
        prixQuantiteBox.setAlignment(Pos.CENTER_LEFT);

        Label prixLabel = new Label(String.format("Prix unitaire: %.2f TND", event.getPrix()));

        Spinner<Integer> quantiteSpinner = new Spinner<>(1, 100, reservation.getQuantite());
        quantiteSpinner.setMaxWidth(100);
        quantiteSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            reservation.setQuantite(newValue);
            reservation.setPrixTotal(event.getPrix() * newValue);
            reservationsService.modifier(reservation);
            loadReservations(reservation.getUtilisateurId());
        });

        Label totalLabel = new Label(String.format("Total: %.2f TND", reservation.getPrixTotal()));

        prixQuantiteBox.getChildren().addAll(prixLabel, new Label("Quantité:"), quantiteSpinner, totalLabel);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button confirmerBtn = new Button("Confirmer");
        confirmerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button annulerBtn = new Button("Annuler");
        annulerBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        confirmerBtn.setOnAction(e -> {
            // Demander l'email
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Email pour le billet");
            dialog.setHeaderText("Entrez votre adresse email");
            dialog.setContentText("Email:");

            dialog.showAndWait().ifPresent(email -> {
                try {
                    // Confirmer la réservation
                    reservationsService.confirmerAchat(reservation.getId(), reservation.getQuantite());

                    // Envoyer le billet par email
                    emailService.envoyerBillet(email, reservation, event);

                    // Afficher un message de succès
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setContentText("Réservation confirmée ! Le billet a été envoyé à " + email);
                    alert.showAndWait();

                    // Recharger les réservations
                    loadReservations(reservation.getUtilisateurId());
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Erreur lors de l'envoi du billet: " + ex.getMessage());
                    alert.showAndWait();
                }
            });
        });

        annulerBtn.setOnAction(e -> {
            reservation.setStatut("annulé");
            reservationsService.modifier(reservation);
            loadReservations(reservation.getUtilisateurId());
        });

        // Statut
        Label statutLabel = new Label("Statut: " + reservation.getStatut());
        statutLabel.setStyle("-fx-font-style: italic;");

        buttonsBox.getChildren().addAll(confirmerBtn, annulerBtn);

        card.getChildren().addAll(
                titreLabel,
                infoLabel,
                prixQuantiteBox,
                statutLabel,
                buttonsBox
        );

        return card;
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/events-view.fxml"));
            Parent newPage = loader.load();
            Scene scene = retourButton.getScene();
            scene.setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : Impossible de charger la page /events-view.fxml");
        }
    }
}
