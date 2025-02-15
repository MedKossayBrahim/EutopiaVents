package com.esprit.models;

import java.util.ArrayList;
import java.util.List;

public class Lieu {
    private int id;
    private String nom;
    private String adresse;
    private String ville;
    private String codePostal;
    private int capacite;
    private String image;
    private categorie_salle categoriesalle;
    private List<Evenement> evenements;
    private double prix;

    public Lieu(int id, String nom, String adresse, String ville, String codePostal, int capacite, String image, categorie_salle categoriesalle) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.codePostal = codePostal;
        this.capacite = capacite;
        this.image = image;
        this.categoriesalle = categoriesalle;
        this.evenements = new ArrayList<>();
    }
    // Constructeur complet (avec prix)
    public Lieu(int id, String nom, String adresse, String ville, String codePostal, int capacite, String image, categorie_salle categoriesalle, double prix) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.codePostal = codePostal;
        this.capacite = capacite;
        this.image = image;
        this.categoriesalle = categoriesalle;
        this.evenements = new ArrayList<>();
        this.prix = prix;
    }

    // Constructeur sans ID (utile pour les insertions en BDD où l'ID est généré automatiquement)
    public Lieu(String nom, String adresse, String ville, String codePostal, int capacite, String image, categorie_salle categoriesalle, double prix) {
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.codePostal = codePostal;
        this.capacite = capacite;
        this.image = image;
        this.categoriesalle = categoriesalle;
        this.evenements = new ArrayList<>();
        this.prix = prix;
    }

    // Constructeur avec valeurs minimales (utile si certains champs sont facultatifs)
    public Lieu(String nom, String ville, int capacite) {
        this.nom = nom;
        this.ville = ville;
        this.capacite = capacite;
        this.evenements = new ArrayList<>();
        this.prix = 0.0; // Valeur par défaut
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public categorie_salle getCategorie() { return categoriesalle; }
    public void setCategorie(categorie_salle categoriesalle) { this.categoriesalle = categoriesalle; }

    public List<Evenement> getEvenements() { return evenements; }
    public void addEvenement(Evenement evenement) { this.evenements.add(evenement); }
    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    @Override
    public String toString() {
        return "Lieu{id=" + id + ", nom='" + nom + "', ville='" + ville + "', capacite=" + capacite + ", prix=" + prix + "}";
    }
}
