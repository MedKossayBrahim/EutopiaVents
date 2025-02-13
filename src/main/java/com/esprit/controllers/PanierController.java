package com.esprit.controllers;

import com.esprit.models.Reservations;
import com.esprit.models.Evenement;
import com.esprit.services.ReservationsService;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.net.URL;
import java.util.ResourceBundle;

public class PanierController implements Initializable {

    @FXML private VBox reservationsContainer;
    @FXML private Label totalLabel;

    private ReservationsService reservationsService = new ReservationsService();
    private EvenementService evenementService = new EvenementService();
    private static final int USER_ID = 10;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadReservations();
    }

    private void loadReservations() {
        reservationsContainer.getChildren().clear();
        double totalPanier = 0;

        var reservations = reservationsService.rechercher().stream()
            .filter(r -> r.getUtilisateurId() == USER_ID)
            .toList();

        for (Reservations reservation : reservations) {
            Evenement event = evenementService.rechercherParId(reservation.getEvenementId());
            VBox card = createReservationCard(reservation, event);
            reservationsContainer.getChildren().add(card);
            totalPanier += reservation.getPrixTotal();
        }

        totalLabel.setText(String.format("%.2f €", totalPanier));
    }

    private VBox createReservationCard(Reservations reservation, Evenement event) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #ddd; -fx-border-radius: 5;");
        card.setPadding(new Insets(10));

        // Titre de l'événement
        Label titreLabel = new Label(event.getTitre());
        titreLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // Informations de l'événement
        String lieu = event.getLieuId() != 0 ? "Lieu " + event.getLieuId() : event.getLieu_proprietaire();
        Label infoLabel = new Label(String.format("Date: %s\nLieu: %s", event.getDateDebut(), lieu));

        // Prix et quantité
        HBox prixQuantiteBox = new HBox(20);
        prixQuantiteBox.setAlignment(Pos.CENTER_LEFT);

        Label prixLabel = new Label(String.format("Prix unitaire: %.2f €", event.getPrix()));
        
        Spinner<Integer> quantiteSpinner = new Spinner<>(1, 100, reservation.getQuantite());
        quantiteSpinner.setMaxWidth(100);
        quantiteSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            reservation.setQuantite(newValue);
            reservation.setPrixTotal(event.getPrix() * newValue);
            reservationsService.modifier(reservation);
            loadReservations();
        });

        Label totalLabel = new Label(String.format("Total: %.2f €", reservation.getPrixTotal()));
        
        prixQuantiteBox.getChildren().addAll(prixLabel, new Label("Quantité:"), quantiteSpinner, totalLabel);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button confirmerBtn = new Button("Confirmer");
        confirmerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        Button annulerBtn = new Button("Annuler");
        annulerBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        confirmerBtn.setOnAction(e -> {
            reservationsService.confirmerAchat(reservation.getId(), reservation.getQuantite());
            loadReservations();
        });

        annulerBtn.setOnAction(e -> {
            reservation.setStatut("annulé");
            reservationsService.modifier(reservation);
            loadReservations();
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
} 