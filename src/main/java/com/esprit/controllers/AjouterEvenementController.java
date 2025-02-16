package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.models.Materiel;
import com.esprit.models.MaterielSelection;
import com.esprit.models.CategoriesEvent;
import com.esprit.models.Lieu;
import com.esprit.services.EvenementService;
import com.esprit.services.MaterielService;
import com.esprit.services.CategoriesEventService;
import com.esprit.services.LieuServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterEvenementController implements Initializable {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<CategoriesEvent> categorieComboBox;
    @FXML private ComboBox<Lieu> lieuComboBox;
    @FXML private TextField lieuProprietaireField;
    @FXML private TextField organisateurIdField;
    @FXML private TextField prixField;
    @FXML private TextField imageField;
    @FXML private RadioButton lieuExistantRadio;
    @FXML private RadioButton lieuPersonnaliseRadio;
    @FXML private VBox lieuExistantVBox;
    @FXML private VBox lieuPersonnaliseVBox;

    @FXML private TableView<MaterielSelection> materielTable;
    @FXML private TableColumn<MaterielSelection, String> materielColumn;
    @FXML private TableColumn<MaterielSelection, Integer> quantiteColumn;
    @FXML private TableColumn<MaterielSelection, Void> actionColumn;

    private final EvenementService evenementService = new EvenementService();
    private final MaterielService materielService = new MaterielService();
    private final CategoriesEventService categoriesEventService = new CategoriesEventService();
    private final LieuServiceImpl lieuService = new LieuServiceImpl();
    private final ObservableList<MaterielSelection> materielSelections = FXCollections.observableArrayList();

    @FXML
    private BorderPane rootPane;

    public AjouterEvenementController() throws SQLException {
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
        setupComboBoxes();
        setupMaterielTable();
        setupLieuSelection();
    }

    private void setupLieuSelection() {
        ToggleGroup lieuToggleGroup = new ToggleGroup();
        lieuExistantRadio.setToggleGroup(lieuToggleGroup);
        lieuPersonnaliseRadio.setToggleGroup(lieuToggleGroup);

        lieuExistantRadio.setSelected(true);
        lieuPersonnaliseVBox.setVisible(false);
        lieuPersonnaliseVBox.setManaged(false);

        lieuExistantRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            lieuExistantVBox.setVisible(newVal);
            lieuExistantVBox.setManaged(newVal);
            lieuPersonnaliseVBox.setVisible(!newVal);
            lieuPersonnaliseVBox.setManaged(!newVal);
        });

        lieuPersonnaliseRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            lieuPersonnaliseVBox.setVisible(newVal);
            lieuPersonnaliseVBox.setManaged(newVal);
            lieuExistantVBox.setVisible(!newVal);
            lieuExistantVBox.setManaged(!newVal);
        });
    }

    private void setupMaterielTable() {
        materielColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMateriel().getLibelle()));

        quantiteColumn.setCellValueFactory(cellData ->
                cellData.getValue().quantiteProperty().asObject());
        quantiteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantiteColumn.setOnEditCommit(event -> {
            event.getRowValue().setQuantite(event.getNewValue());
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    deleteButton.setOnAction(event -> {
                        materielSelections.remove(getTableRow().getItem());
                    });
                    setGraphic(deleteButton);
                }
            }
        });

        materielTable.setItems(materielSelections);
        materielTable.setEditable(true);
    }

    private void setupComboBoxes() {
        List<CategoriesEvent> categories = categoriesEventService.rechercher();
        categorieComboBox.setItems(FXCollections.observableArrayList(categories));
        categorieComboBox.setConverter(new StringConverter<CategoriesEvent>() {
            @Override
            public String toString(CategoriesEvent categorie) {
                return categorie != null ? categorie.getNom() : "";
            }

            @Override
            public CategoriesEvent fromString(String string) {
                return null;
            }
        });

        List<Lieu> lieux = lieuService.rechercher();
        lieuComboBox.setItems(FXCollections.observableArrayList(lieux));
        lieuComboBox.setConverter(new StringConverter<Lieu>() {
            @Override
            public String toString(Lieu lieu) {
                return lieu != null ? lieu.getNom() : "";
            }

            @Override
            public Lieu fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void ouvrirSelectionMateriel() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sélection de Matériel");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        List<Materiel> materiels = materielService.rechercher();
        for (Materiel materiel : materiels) {
            HBox materielRow = new HBox(10);
            CheckBox checkBox = new CheckBox(materiel.getLibelle());
            Spinner<Integer> quantiteSpinner = new Spinner<>(1, 100, 1);

            materielRow.getChildren().addAll(checkBox, quantiteSpinner);
            content.getChildren().add(materielRow);

            boolean isAlreadySelected = materielSelections.stream()
                    .anyMatch(ms -> ms.getMateriel().getId() == materiel.getId());
            checkBox.setSelected(isAlreadySelected);
        }

        ButtonType confirmButton = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButton) {
                materielSelections.clear();
                for (int i = 0; i < content.getChildren().size(); i++) {
                    HBox row = (HBox) content.getChildren().get(i);
                    CheckBox checkBox = (CheckBox) row.getChildren().get(0);
                    Spinner<Integer> spinner = (Spinner<Integer>) row.getChildren().get(1);

                    if (checkBox.isSelected()) {
                        Materiel materiel = materiels.get(i);
                        MaterielSelection selection = new MaterielSelection(materiel);
                        selection.setQuantite(spinner.getValue());
                        materielSelections.add(selection);
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleAjouterEvenement() {
        try {
            int lieuId = 0;
            String lieuProprietaire = null;

            if (lieuExistantRadio.isSelected()) {
                Lieu selectedLieu = lieuComboBox.getValue();
                if (selectedLieu != null) {
                    lieuId = selectedLieu.getId();
                }
            } else {
                lieuProprietaire = lieuProprietaireField.getText();
            }

            Evenement evenement = new Evenement(
                    0, // ID sera généré par la base de données
                    titreField.getText(),
                    descriptionField.getText(),
                    dateDebutPicker.getValue().toString(),
                    dateFinPicker.getValue().toString(),
                    Integer.parseInt(capaciteField.getText()),
                    categorieComboBox.getValue().getId(),
                    lieuId,
                    Integer.parseInt(organisateurIdField.getText()),
                    Double.parseDouble(prixField.getText()),
                    "en attente",
                    lieuProprietaire,
                    imageField.getText()
            );

            evenementService.ajouter(evenement);

            for (MaterielSelection selection : materielSelections) {
                evenementService.ajouterMaterielAEvenement(
                        evenement.getId(),
                        selection.getMateriel().getId(),
                        selection.getQuantite()
                );
            }

            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        capaciteField.clear();
        categorieComboBox.setValue(null);
        lieuComboBox.setValue(null);
        lieuProprietaireField.clear();
        organisateurIdField.clear();
        prixField.clear();
        imageField.clear();
        materielSelections.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
