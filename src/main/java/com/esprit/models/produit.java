package com.esprit.models;

public class produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private int stock;
    private int categorie_produitId;

    public produit(int id, String nom, String description, double prix, int stock, int categorie_produitId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.categorie_produitId = categorie_produitId;
    }

    public produit(String nom, String description, double prix, int stock, int categorie_produitId) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.categorie_produitId = categorie_produitId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getCategorie_produitId() { return categorie_produitId; }
    public void setCategorie_produitId(int categorie_produitId) { this.categorie_produitId = categorie_produitId; }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", stock=" + stock +
                ", categorie_produitId=" + categorie_produitId +
                '}';
    }
}
