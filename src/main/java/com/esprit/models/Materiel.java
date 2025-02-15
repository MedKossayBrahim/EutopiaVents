package com.esprit.models;

public class Materiel {
    private int id;
    private String libelle;
    private String description; // RÉINTRODUCTION DU CHAMP
    private int quantite;
    private int categorieId; // Clé étrangère vers Categorie
    private double prix; // Ajout du prix
    private String image_url;

    public Materiel(int id, String libelle, String description, int quantite, int categorieId, double prix, String image_url) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.quantite = quantite;
        this.categorieId = categorieId;
        this.prix = prix;
        this.image_url = image_url;
    }

    public Materiel(String libelle, String description, int quantite, int categorieId, double prix, String image_url) {
        this.libelle = libelle;
        this.description = description;
        this.quantite = quantite;
        this.categorieId = categorieId;
        this.prix = prix;
        this.image_url = image_url;
    }

    public Materiel(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public String getImage_url() { return image_url; }

    @Override
    public String toString() {
        return "Materiel{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                ", description='" + description + '\'' +
                ", quantite=" + quantite +
                ", categorieId=" + categorieId +
                ", prix=" + prix +
                ", image_url='" + image_url + '\'' +
                '}';
    }
}
