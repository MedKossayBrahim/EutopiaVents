package com.esprit.models;

public class produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private int stock;
    private int categorieId;
    private byte[] image;

    public produit(int id, String nom, String description, double prix, int stock, int categorieId, byte[] image) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.categorieId = categorieId;
        this.image = image;
    }

    public produit(String nom, String description, double prix, int stock, int categorieId, byte[] image) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.categorieId = categorieId;
        this.image = image;
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

    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = this.image; }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", stock=" + stock +
                ", categorieId=" + categorieId +
                ", image='" + image + '\'' +
                '}';
    }
}

