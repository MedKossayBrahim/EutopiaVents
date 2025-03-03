package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.models.Materiel;
import com.esprit.services.AdminService;
import com.esprit.services.CategorieService;
import com.esprit.services.MaterielService;
import com.esprit.utils.DataSource;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.sql.*;
import java.util.Comparator;
import java.util.List;

public class ListeMaterielController {
    @FXML
    private TableView<Materiel> materielTable;
    @FXML
    private TableColumn<Materiel, String> libelleColumn;
    @FXML
    private TableColumn<Materiel, String> descriptionColumn;
    @FXML
    private TableColumn<Materiel, Integer> quantiteColumn;
    @FXML
    private TableColumn<Materiel, Double> prixColumn;
    @FXML
    private TableColumn<Materiel, String> categorieColumn;
    @FXML
    private TableColumn<Materiel, String> imageUrlColumn;
    @FXML
    private TableColumn<Materiel, Void> actionsColumn;
    @FXML
    private TextField searchField; // Zone de recherche
    @FXML
    private ComboBox<String> categorieComboBox; // ComboBox pour sélectionner la catégorie

    private final MaterielService materielService;
    private ObservableList<Materiel> materielsList; // Liste observable des matériels
    private FilteredList<Materiel> filteredMateriels; // Liste filtrée
    private SortedList<Materiel> sortedMateriels; // Liste triée

    public ListeMaterielController() {
        materielService = new MaterielService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadMateriels();
        setupSearch();
        setupCategorieComboBox(); // Appel à la méthode pour configurer le ComboBox
    }

    private void setupColumns() {
        // Configuration des colonnes
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        imageUrlColumn.setCellValueFactory(new PropertyValueFactory<>("Image_url"));

        // Activer le tri uniquement sur les colonnes "categorie", "prix" et "quantite"
        quantiteColumn.setSortable(true);
        prixColumn.setSortable(true);
        categorieColumn.setSortable(true); // Assurez-vous que la colonne categorie est définie

        // Désactiver le tri pour les autres colonnes
        libelleColumn.setSortable(false);
        descriptionColumn.setSortable(false);
        
        // Rendre les colonnes éditables
        libelleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        quantiteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        prixColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        // Configuration de la colonne imageUrlColumn pour afficher l'image
        imageUrlColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final HBox hbox = new HBox(imageView);

            {
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
                hbox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(imageUrl, true);
                        imageView.setImage(image);
                        setGraphic(hbox);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // Gérer les modifications
        libelleColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setLibelle(event.getNewValue());
            materielService.modifier(materiel);
        });

        categorieColumn.setCellValueFactory(cellData -> {
            int categorieId = cellData.getValue().getCategorieId();
            String categorieName = null;
            try {
                categorieName = getCategorieName(categorieId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(categorieName);
        });

        descriptionColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setDescription(event.getNewValue());
            materielService.modifier(materiel);
        });

        quantiteColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setQuantite(event.getNewValue());
            materielService.modifier(materiel);
        });

        prixColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setPrix(event.getNewValue());
            materielService.modifier(materiel);
        });

        // Configuration de la colonne des actions (supprimer uniquement)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5); // 5 est l'espacement entre les boutons

            {
                buttonsBox.getChildren().addAll(deleteBtn);

                deleteBtn.setOnAction(event -> {
                    Materiel materiel = getTableView().getItems().get(getIndex());
                    materielService.supprimer(materiel);
                    loadMateriels();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });

        // Activer l'édition sur double-clic
        materielTable.setEditable(true);
        materielTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !materielTable.getSelectionModel().isEmpty()) {
                TablePosition<Materiel, ?> pos = materielTable.getSelectionModel().getSelectedCells().get(0);
                int row = pos.getRow();
                TableColumn<Materiel, ?> col = pos.getTableColumn();
                if (col == libelleColumn || col == descriptionColumn || col == quantiteColumn || col == prixColumn) {
                    materielTable.edit(row, col);
                }
            }
        });
    }

    private void loadMateriels() {
        // Charger les données
        materielsList = FXCollections.observableArrayList(materielService.rechercher());
        
        // Créer la FilteredList
        filteredMateriels = new FilteredList<>(materielsList, p -> true);
        
        // Créer la SortedList
        sortedMateriels = new SortedList<>(filteredMateriels);
        
        // Lier le comparateur de la SortedList à celui de la TableView
        sortedMateriels.comparatorProperty().bind(materielTable.comparatorProperty());
        
        // Définir la SortedList comme items de la TableView
        materielTable.setItems(sortedMateriels);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMateriels.setPredicate(materiel -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return materiel.getLibelle().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private String getCategorieName(int categorieId) throws SQLException {

        String req = "SELECT nom FROM categorie WHERE id = ?";
        Connection connection = DataSource.getInstance().getConnection();
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, categorieId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de la catégorie : " + e.getMessage());
        }
        return null;
    }
    @FXML
    private void ajouterMateriel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutMateriel.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajout de Matériel");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> loadMateriels());
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
        }
    }

    private void setupCategorieComboBox() {
        // Remplir le ComboBox avec les catégories
        CategorieService categorieService = new CategorieService();
        List<Categorie> categories = categorieService.rechercher(); // Récupérer les catégories
        categorieComboBox.getItems().clear(); // Vider le ComboBox avant d'ajouter de nouvelles catégories
        for (Categorie categorie : categories) {
            categorieComboBox.getItems().add(categorie.getNom()); // Ajouter le nom de la catégorie
        }
        
        // Écouteur pour filtrer les matériels par catégorie
        categorieComboBox.setOnAction(event -> {
            String selectedCategory = categorieComboBox.getValue();
            filteredMateriels.setPredicate(materiel -> {
                if (selectedCategory == null || selectedCategory.isEmpty()) {
                    return true; // Afficher tous les matériels si aucune catégorie n'est sélectionnée
                }
                // Vérifier si le nom de la catégorie correspond à la catégorie sélectionnée
                try {
                    String categorieName = getCategorieName(materiel.getCategorieId());
                    return categorieName != null && categorieName.equals(selectedCategory);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            });
        });
    }

}