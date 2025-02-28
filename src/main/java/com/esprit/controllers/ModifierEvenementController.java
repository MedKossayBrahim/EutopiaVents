package com.esprit.controllers;

import com.esprit.models.CategoriesEvent;
import com.esprit.models.Evenement;
import com.esprit.services.CategoriesEventService;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.ComboBoxTableCell;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import com.esprit.models.User;
import com.esprit.models.Role;
import com.esprit.tests.Eutopia ;

public class ModifierEvenementController {

    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> descriptionColumn;
    @FXML private TableColumn<Evenement, String> dateDebutColumn;
    @FXML private TableColumn<Evenement, String> dateFinColumn;
    @FXML private TableColumn<Evenement, Integer> capaciteColumn;
    @FXML private TableColumn<Evenement, Double> prixColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, String> categorieColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;
    @FXML private ScrollPane scrollPane;
    @FXML
    private Button btnAjouterCateg;
    @FXML
    private Button btnAjouterEvenement;
    @FXML
    private Button btnModifierEvenement;
    @FXML
    private Button btnGererEvenements;

    private EvenementService evenementService = new EvenementService();
    private CategoriesEventService categoriesEventService = new CategoriesEventService();
    private ObservableList<Evenement> evenementList = FXCollections.observableArrayList();
    private ObservableList<CategoriesEvent> categoriesList = FXCollections.observableArrayList();

    public ModifierEvenementController() throws SQLException {
    }

    @FXML
    public void initialize() {


        loadCategories();
        setupTable();
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

    private void setupTable() {
        eventTable.setEditable(true);

        // Associer les colonnes aux propriétés de l'objet Evenement
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorieNom"));

        // Activer l'édition pour toutes les colonnes SAUF statut
        titreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        dateDebutColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        dateFinColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        capaciteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        prixColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        // Enregistrer temporairement les modifications
        titreColumn.setOnEditCommit(event -> event.getRowValue().setTitre(event.getNewValue()));
        descriptionColumn.setOnEditCommit(event -> event.getRowValue().setDescription(event.getNewValue()));
        dateDebutColumn.setOnEditCommit(event -> event.getRowValue().setDateDebut(event.getNewValue()));
        dateFinColumn.setOnEditCommit(event -> event.getRowValue().setDateFin(event.getNewValue()));
        capaciteColumn.setOnEditCommit(event -> event.getRowValue().setCapacite(event.getNewValue()));
        prixColumn.setOnEditCommit(event -> event.getRowValue().setPrix(event.getNewValue()));

        // Empêcher le changement de statut sauf "annulé"
        statutColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList("annulé")));
        statutColumn.setOnEditCommit(event -> event.getRowValue().setStatut("annulé"));

        // Colonne catégorie avec `ComboBoxTableCell`
        categorieColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(
                categoriesList.stream().map(CategoriesEvent::getNom).toList()
        )));

        categorieColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            String selectedCategoryName = event.getNewValue();
            CategoriesEvent selectedCategory = categoriesList.stream()
                    .filter(cat -> cat.getNom().equals(selectedCategoryName))
                    .findFirst()
                    .orElse(null);
            if (selectedCategory != null) {
                selectedEvent.setCategorieId(selectedCategory.getId());
            }
        });

        // Bouton "Modifier"
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");

            {
                modifyBtn.setOnAction(e -> {
                    Evenement selectedEvent = getTableView().getItems().get(getIndex());
                    modifierEvenement(selectedEvent);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyBtn);
            }
        });
    }

    private void loadCategories() {
        List<CategoriesEvent> categories = categoriesEventService.rechercher();
        categoriesList.setAll(categories);
    }

    private void loadEvents() {
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser != null) {
            // Charger uniquement les événements de l'organisateur connecté
            evenementList.setAll(evenementService.rechercherParOrganisateur(currentUser.getUserID()));
            eventTable.setItems(evenementList);
        }
    }

    private void modifierEvenement(Evenement evenement) {
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null || evenement.getOrganisateurId() != currentUser.getUserID()) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Vous n'avez pas les droits pour modifier cet événement.");
            return;
        }

        if (showConfirmationDialog("Confirmer la modification",
                "Voulez-vous modifier cet événement ?")) {
            evenementService.modifier(evenement);
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Événement modifié avec succès.");
            loadEvents();
        }
    }

    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleRetour() {
        loadPage("/events-view.fxml"); // Retour à la page des événements
    }

    private void loadPage(String page) {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource(page));
            Scene scene = scrollPane.getScene();
            scene.setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
