package com.esprit.controllers;

import com.esprit.models.*;
import com.esprit.services.EvenementService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import com.esprit.services.ReservationService;
import com.esprit.services.ReservationServiceImpl;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.esprit.utils.DataSource;
public class GererEvenementsController implements Initializable {
    private Connection connection;
    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> descriptionColumn;
    @FXML private TableColumn<Evenement, String> dateDebutColumn;
    @FXML private TableColumn<Evenement, String> dateFinColumn;
    @FXML private TableColumn<Evenement, Integer> capaciteColumn;
    @FXML private TableColumn<Evenement, String> categorieIdColumn;
    @FXML private TableColumn<Evenement, String> lieuColumn;
    @FXML private TableColumn<Evenement, String> organisateurColumn;
    @FXML private TableColumn<Evenement, Double> prixColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;
    @FXML
    private Button btnAjouterCateg;
    @FXML
    private Button btnAjouterEvenement;
    @FXML
    private Button btnModifierEvenement;
    @FXML
    private Button btnGererEvenements;

    private final EvenementService evenementService = new EvenementService();
    private final ReservationService reservationMaterielService = new ReservationService();
    private final ReservationServiceImpl reservationLieuService = new ReservationServiceImpl();





    @FXML
    private BorderPane rootPane;

    public GererEvenementsController() throws SQLException {
    }

    @FXML
    private void goToAjouterCateg() {
        loadPage("/AjouterCategEvent.fxml");
    }

    @FXML
    private void goToAjouterEvenement() {
        loadPage("/AjouterEvenement.fxml");
    }

    @FXML
    private void goToModifierEvenement() {
        loadPage("/ModifierEvenement.fxml");
    }

    @FXML
    private void goToEventsView() {
        loadPage("/events-view.fxml");
    }

    @FXML
    private void goToGererEvenements() {
        loadPage("/GererEvenements.fxml");
    }

    private void loadPage(String page) {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource(page));
            Scene scene = rootPane.getScene();
            scene.setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadEvents();
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser != null) {
            // Vérifier le type de service en fonction du rôle de l'utilisateur
            switch (currentUser.getRole()) {
                case Admin:
                    // Admin peut voir tous les boutons
                    break;

                case Organisateur:
                    // Organisateur peut voir tous les boutons sauf GererEvenements et AjouterCateg
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;

                case Participant:
                    // Participant ne peut voir aucun bouton de gestion
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnAjouterEvenement.setVisible(false);
                    btnAjouterEvenement.setManaged(false);
                    btnModifierEvenement.setVisible(false);
                    btnModifierEvenement.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;

                default:
                    // Par défaut, cacher tous les boutons de gestion
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnAjouterEvenement.setVisible(false);
                    btnAjouterEvenement.setManaged(false);
                    btnModifierEvenement.setVisible(false);
                    btnModifierEvenement.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;
            }
        }
    }



    private void setupColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        categorieIdColumn.setCellValueFactory(new PropertyValueFactory<>("categorieNom"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        organisateurColumn.setCellValueFactory(new PropertyValueFactory<>("organisateurNom"));

        lieuColumn.setCellValueFactory(cellData -> {
            Evenement event = cellData.getValue();
            String lieuInfo = event.getLieuNom() != null ?
                    event.getLieuNom() :
                    event.getLieu_proprietaire();
            return new SimpleStringProperty(lieuInfo);
        });

        actionsColumn.setCellFactory(column -> {
            return new TableCell<>() {
                private final Button acceptButton = new Button("Accepter");
                private final Button refuserButton = new Button("Refuser");
                private final HBox buttonBox = new HBox(5, acceptButton, refuserButton);

                {
                    acceptButton.setOnAction(event -> {
                        Evenement evenement = getTableView().getItems().get(getIndex());
                        handleAccept(evenement);
                    });

                    refuserButton.setOnAction(event -> {
                        Evenement evenement = getTableView().getItems().get(getIndex());
                        handleRefuse(evenement);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Evenement evenement = getTableView().getItems().get(getIndex());
                        if ("en attente".equals(evenement.getStatut())) {
                            setGraphic(buttonBox);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            };
        });
    }

    private void loadEvents() {
        List<Evenement> events = evenementService.rechercher()
                .stream()
                .filter(event -> "en attente".equals(event.getStatut()))
                .toList();

        eventTable.setItems(FXCollections.observableArrayList(events));
    }

    private void handleAccept(Evenement evenement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirmer l'acceptation de l'événement");
        alert.setContentText("Êtes-vous sûr de vouloir accepter cet événement ?");

        // Ajout des boutons "Confirmer" et "Annuler"
        ButtonType confirmButton = new ButtonType("Confirmer");
        ButtonType cancelButton = new ButtonType("Annuler");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        // Afficher l'alerte et attendre la réponse de l'utilisateur
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            // Si l'utilisateur confirme, procéder à l'acceptation
            try {
                // Si l'événement utilise un lieu existant (lieuId > 0), créer une réservation pour ce lieu
                if (evenement.getLieuId() > 0) {
                    // Convertir les dates en LocalDateTime
                    LocalDateTime dateDebut = LocalDateTime.parse(evenement.getDateDebut().replace(" ", "T"));
                    LocalDateTime dateFin = LocalDateTime.parse(evenement.getDateFin().replace(" ", "T"));

                    reservation1 reservationLieu = new reservation1(
                            0, // id sera généré
                            evenement.getLieuId(),
                            evenement.getId(),
                            dateDebut,
                            dateFin
                    );

                    reservationLieuService.ajouter(reservationLieu);
                }

                // Initialiser la connexion
                this.connection = DataSource.getInstance().getConnection();

                // Créer des réservations pour chaque matériel
                String req = "SELECT em.materiel_id, em.quantite, m.prix " +
                        "FROM event_materiel em " +
                        "JOIN materiel m ON em.materiel_id = m.id " +
                        "WHERE em.evenement_id = ?";

                try (PreparedStatement pst = connection.prepareStatement(req)) {
                    pst.setInt(1, evenement.getId());
                    ResultSet rs = pst.executeQuery();

                    ReservationService reservationMaterielService = new ReservationService();
                    while (rs.next()) {
                        int materielId = rs.getInt("materiel_id");
                        int quantite = rs.getInt("quantite");
                        double prix = rs.getDouble("prix");

                        Reservation reservationMateriel = new Reservation(
                                evenement.getId(),
                                materielId,
                                quantite,
                                prix * quantite,
                                java.sql.Date.valueOf(evenement.getDateDebut().split(" ")[0]),
                                java.sql.Date.valueOf(evenement.getDateFin().split(" ")[0])
                        );
                        reservationMaterielService.ajouter(reservationMateriel);
                    }
                }

                // Mettre à jour le statut de l'événement
                evenement.setStatut("acceptée");
                evenementService.modifier(evenement);

                // Rafraîchir la table
                loadEvents();

                // Afficher un message de succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setContentText("Événement accepté avec succès. Les réservations ont été créées.");
                successAlert.showAndWait();

            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setContentText("Erreur lors de l'acceptation de l'événement: " + e.getMessage());
                errorAlert.showAndWait();
                e.printStackTrace(); // Pour avoir plus de détails sur l'erreur dans la console
            }
        }
    }

    private void handleRefuse(Evenement evenement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirmer le refus de l'événement");
        alert.setContentText("Êtes-vous sûr de vouloir refuser cet événement ?");

        // Ajout des boutons "Confirmer" et "Annuler"
        ButtonType confirmButton = new ButtonType("Confirmer");
        ButtonType cancelButton = new ButtonType("Annuler");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        // Afficher l'alerte et attendre la réponse de l'utilisateur
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            // Si l'utilisateur confirme, procéder au refus
            evenement.setStatut("refusée");
            evenementService.modifier(evenement);
            loadEvents();

            // Afficher un message de succès
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setContentText("Événement refusé avec succès.");
            successAlert.showAndWait();
        }
    }


    public void refreshTable() {
        loadEvents();
    }
} 