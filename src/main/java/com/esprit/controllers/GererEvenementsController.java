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
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
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
    @FXML private Button btnAjouterCateg;
    @FXML private Button btnAjouterEvenement;
    @FXML private Button btnModifierEvenement;
    @FXML private Button btnGererEvenements;
    @FXML private BorderPane rootPane;

    private final EvenementService evenementService = new EvenementService();
    private final ReservationService reservationMaterielService = new ReservationService();
    private final ReservationServiceImpl reservationLieuService = new ReservationServiceImpl();

    public GererEvenementsController() throws SQLException {
    }

    @FXML
    private void goToAjouterCateg() {
        loadPage("/AjouterCateg.fxml");
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
        configureButtonsBasedOnUserRole();
    }
    
    private void configureButtonsBasedOnUserRole() {
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null) return;
        
        switch (currentUser.getRole()) {
            case Admin:
                // Admin peut voir tous les boutons
                break;
            case Organisateur:
                // Masquer certains boutons
                btnAjouterCateg.setVisible(false);
                btnAjouterCateg.setManaged(false);
                btnGererEvenements.setVisible(false);
                btnGererEvenements.setManaged(false);
                break;
            default:
                // Masquer tous les boutons
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
                    acceptButton.setOnAction(event -> handleAccept(getTableView().getItems().get(getIndex())));
                    refuserButton.setOnAction(event -> handleRefuse(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Evenement evenement = getTableView().getItems().get(getIndex());
                        setGraphic("en attente".equals(evenement.getStatut()) ? buttonBox : null);
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
                // Si l'événement a un lieu assigné
                if (evenement.getLieuId() > 0) {
                    LocalDateTime dateDebut = LocalDateTime.parse(evenement.getDateDebut().replace(" ", "T"));
                    LocalDateTime dateFin = LocalDateTime.parse(evenement.getDateFin().replace(" ", "T"));

                    reservation1 reservationLieu = new reservation1(
                            0, evenement.getLieuId(), evenement.getId(), dateDebut, dateFin);
                    
                    // Définir l'utilisateur de la réservation (utiliser l'organisateur de l'événement)
                    reservationLieu.setUserID(evenement.getOrganisateurId());
                    reservationLieu.setTypeReservation("evenement");
                    
                    // Vérifier si la réservation est possible
                    if (!reservationLieuService.checkEventAvailability(evenement.getLieuId(), dateDebut, dateFin)) {
                        throw new RuntimeException("Le lieu est déjà réservé pour cette période.");
                    }
                    
                    reservationLieuService.ajouter(reservationLieu);
                }

                // Créer des réservations pour les matériels
                createMaterialReservations(evenement);

                // Mettre à jour le statut de l'événement
                evenement.setStatut("acceptée");
                evenementService.modifier(evenement);
                loadEvents();

                showInfoDialog("Succès", "Événement accepté avec succès. Les réservations ont été créées.");
            } catch (Exception e) {
                showErrorDialog("Erreur lors de l'acceptation de l'événement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void createMaterialReservations(Evenement evenement) throws Exception {
        this.connection = DataSource.getInstance().getConnection();
        String req = "SELECT em.materiel_id, em.quantite, m.prix " +
                    "FROM event_materiel em " +
                    "JOIN materiel m ON em.materiel_id = m.id " +
                    "WHERE em.evenement_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evenement.getId());
            ResultSet rs = pst.executeQuery();

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
    }

    private void handleRefuse(Evenement evenement) {
        if (showConfirmationDialog("Confirmer le refus de l'événement", 
                                  "Êtes-vous sûr de vouloir refuser cet événement ?")) {
            evenement.setStatut("refusée");
            evenementService.modifier(evenement);
            loadEvents();
            showInfoDialog("Succès", "Événement refusé avec succès.");
        }
    }
    
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        ButtonType confirmButton = new ButtonType("Confirmer");
        ButtonType cancelButton = new ButtonType("Annuler");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }
    
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshTable() {
        loadEvents();
    }
} 