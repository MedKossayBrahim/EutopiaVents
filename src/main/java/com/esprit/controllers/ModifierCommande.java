package com.esprit.controllers;

import com.esprit.models.commande; // Assuming you have a Commande model
import com.esprit.models.produit; // Assuming you have a Produit model
import com.esprit.services.CommandeService; // Assuming you have a CommandeService
import com.esprit.services.ProduitService; // Assuming you have a ProduitService
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ModifierCommande { // Changed from ModifierMaterielController to ModifierCommandeController
    @FXML
    private TextField clientIdField; // TextField for client ID
    @FXML
    private ComboBox<produit> produitComboBox; // ComboBox for selecting a product
    @FXML
    private TextField quantiteField; // TextField for quantity input

    private final CommandeService commandeService; // Changed from MaterielService to CommandeService
    private final ProduitService produitService; // Added ProduitService
    private commande currentCommande; // Changed from Materiel to Commande

    public ModifierCommande() {
        commandeService = new CommandeService(); // Changed from MaterielService to CommandeService
        produitService = new ProduitService(); // Initialize ProduitService
    }

    @FXML
    public void initialize() {
        loadProduits(); // Load products into the ComboBox
    }

    private void loadProduits() {
        produitComboBox.getItems().addAll(produitService.rechercher()); // Assuming you have a method to get products
    }

    public void setCommande(commande commande) { // Changed from setMateriel to setCommande
        this.currentCommande = commande; // Changed from currentMateriel to currentCommande
        displayCommande(); // Changed from displayMateriel to displayCommande
    }

    private void displayCommande() { // Changed from displayMateriel to displayCommande
        if (currentCommande != null) {
            clientIdField.setText(String.valueOf(currentCommande.getClientId())); // Set client ID
            quantiteField.setText(String.valueOf(currentCommande.getQuantite())); // Set quantity

            // Set selected product in the ComboBox
            for (produit produit : produitComboBox.getItems()) {
                if (produit.getId() == currentCommande.getProduitId()) { // Assuming you have a getProduitId method
                    produitComboBox.setValue(produit);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSauvegarder() {
        if (currentCommande != null) { // Changed from currentMateriel to currentCommande
            currentCommande.setClientId(Integer.parseInt(clientIdField.getText())); // Set client ID
            currentCommande.setProduitId(produitComboBox.getValue().getId()); // Set product ID
            currentCommande.setQuantite(Integer.parseInt(quantiteField.getText())); // Set quantity

            commandeService.modifier(currentCommande); // Call the modifier method in CommandeService
        }
    }
}