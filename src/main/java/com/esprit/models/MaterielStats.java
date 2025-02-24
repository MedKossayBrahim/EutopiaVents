package com.esprit.models;

public class MaterielStats {
    private String libelle;
    private int nombreUtilisation;

    public MaterielStats(String libelle, int nombreUtilisation) {
        this.libelle = libelle;
        this.nombreUtilisation = nombreUtilisation;
    }

    // ⚠️ JavaFX utilise ces getters pour récupérer les valeurs
    public String getLibelle() {
        return libelle;
    }

    public int getNombreUtilisation() {
        return nombreUtilisation;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setNombreUtilisation(int nombreUtilisation) {
        this.nombreUtilisation = nombreUtilisation;
    }
}