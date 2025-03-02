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
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
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

    private User currentUser;



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

        ButtonType confirmButton = new ButtonType("Confirmer");
        ButtonType cancelButton = new ButtonType("Annuler");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            try {
                this.connection = DataSource.getInstance().getConnection();

                // Debug: Print event details before modification
                System.out.println("Event ID before modification: " + evenement.getId());

                // First update the event status
                evenement.setStatut("acceptée");
                evenementService.modifier(evenement);

                // Verify the event exists and get its actual ID from the database
                String verifyEventQuery = "SELECT id FROM events WHERE id = ?";
                int confirmedEventId;

                try (PreparedStatement pst = connection.prepareStatement(verifyEventQuery)) {
                    pst.setInt(1, evenement.getId());
                    System.out.println("Verifying event with ID: " + evenement.getId());

                    ResultSet rs = pst.executeQuery();
                    if (!rs.next()) {
                        // If not found by ID, try to find by other criteria
                        String findEventQuery = "SELECT id FROM events WHERE titre = ? AND date_debut = ?";
                        try (PreparedStatement findPst = connection.prepareStatement(findEventQuery)) {
                            findPst.setString(1, evenement.getTitre());
                            findPst.setString(2, evenement.getDateDebut());
                            ResultSet findRs = findPst.executeQuery();

                            if (findRs.next()) {
                                confirmedEventId = findRs.getInt("id");
                                System.out.println("Found event with different ID: " + confirmedEventId);
                            } else {
                                throw new SQLException("L'événement n'existe pas dans la base de données.");
                            }
                        }
                    } else {
                        confirmedEventId = rs.getInt("id");
                        System.out.println("Confirmed event ID: " + confirmedEventId);
                    }
                }

                // Update the event object with the confirmed ID
                evenement.setId(confirmedEventId);

                // Then handle lieu reservation if needed
                if (evenement.getLieuId() > 0) {
                    LocalDateTime dateDebut = LocalDateTime.parse(evenement.getDateDebut().replace(" ", "T"));
                    LocalDateTime dateFin = LocalDateTime.parse(evenement.getDateFin().replace(" ", "T"));

                    reservation1 reservationLieu = new reservation1(
                            0,
                            evenement.getLieuId(),
                            confirmedEventId,  // Use confirmed ID
                            dateDebut,
                            dateFin
                    );

                    reservationLieuService.ajouter(reservationLieu);
                }

                // Handle material reservations
                String req = "SELECT em.materiel_id, em.quantite, m.prix " +
                        "FROM event_materiel em " +
                        "JOIN materiel m ON em.materiel_id = m.id " +
                        "WHERE em.evenement_id = ?";

                try (PreparedStatement pst = connection.prepareStatement(req)) {
                    pst.setInt(1, confirmedEventId);  // Use confirmed ID
                    ResultSet rs = pst.executeQuery();

                    while (rs.next()) {
                        int materielId = rs.getInt("materiel_id");
                        int quantite = rs.getInt("quantite");
                        double prix = rs.getDouble("prix");

                        System.out.println("Creating reservation with confirmed event ID: " + confirmedEventId);

                        Reservation reservationMateriel = new Reservation(
                                materielId,
                                quantite,
                                prix * quantite,
                                Timestamp.valueOf(evenement.getDateDebut()),
                                Timestamp.valueOf(evenement.getDateFin()),
                                Eutopia.getCurrentUser().getUserID()
                        );

                        reservationMaterielService.ajouter(reservationMateriel);
                    }
                }

                loadEvents();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setContentText("Événement accepté avec succès. Les réservations ont été créées.");
                successAlert.showAndWait();

            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setContentText("Erreur lors de l'acceptation de l'événement: " + e.getMessage());
                errorAlert.showAndWait();
                e.printStackTrace();
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