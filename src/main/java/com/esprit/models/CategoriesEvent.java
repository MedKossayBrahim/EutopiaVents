package com.esprit.models;

public class CategoriesEvent {
    private int id;
    private String nom;

    public CategoriesEvent() {}

    public CategoriesEvent(String nom) {
        this.nom = nom;
    }

    public CategoriesEvent(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "CategoriesEvent{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                '}';
    }
}
