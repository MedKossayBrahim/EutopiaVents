package com.esprit.models;

import com.esprit.services.ProduitService;

import java.sql.SQLException;

public class commande {

        private int id;
        private int clientId;
        private int produitId;
        private int quantite;
        private double prixTotal;

        public commande() {
        }

        public commande(int id, int clientId, int produitId, int quantite) {
            this.id = id;
            this.clientId = clientId;
            this.produitId = produitId;
            this.quantite = quantite;
        }

        public commande(int clientId, int produitId, int quantite) {
            this.clientId = clientId;
            this.produitId = produitId;
            this.quantite = quantite;
        }

    public commande(int id) {
this.id = id;
    }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getClientId() { return clientId; }
        public void setClientId(int clientId) { this.clientId = clientId; }

        public int getProduitId() { return produitId; }
        public void setProduitId(int produitId) { this.produitId = produitId; }

        public int getQuantite() { return quantite; }
        public void setQuantite(int quantite) { this.quantite = quantite; }

        @Override
        public String toString() {
            return "commande{" +
                    "id=" + id +
                    ", clientId=" + clientId +
                    ", produitId=" + produitId +
                    ", quantite=" + quantite +
                    '}';
        }

    public double getPrixTotal() throws SQLException {
        // Créer une instance de ProduitService pour obtenir le prix du produit
        ProduitService produitService = new ProduitService();
        // Récupérer le produit par son ID
        produit produit = produitService.getOne(this.produitId);
        // Calculer le prix total (prix du produit * quantité)
        return produit.getPrix() * this.quantite;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }
}

