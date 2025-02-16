package com.esprit.models;

import java.util.ArrayList;
import java.util.List;
public class categorie_salle {
    private int id;
    private String nom;
    private String description;
    private List<Lieu> lieux;

    //les constructeurs
    public categorie_salle(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.lieux = new ArrayList<>();
    }
    public categorie_salle() {
        this.lieux = new ArrayList<>();
    }

    public categorie_salle(String nom, String description) {
        this.nom = nom;
        this.description = description;
        this.lieux = new ArrayList<>();
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<Lieu> getLieux() { return lieux; }

    public void addLieu(Lieu lieu) { this.lieux.add(lieu); }


    public void setLieux(List<Lieu> lieux) {
        this.lieux = lieux;
    }

    public categorie_salle(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", nom='" + nom + "', description='" + description + "'}";
    }
}
