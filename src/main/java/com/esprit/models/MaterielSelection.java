package com.esprit.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MaterielSelection {
    private Materiel materiel;
    private IntegerProperty quantite = new SimpleIntegerProperty(1);

    public MaterielSelection(Materiel materiel) {
        this.materiel = materiel;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public int getQuantite() {
        return quantite.get();
    }

    public void setQuantite(int quantite) {
        this.quantite.set(quantite);
    }

    public IntegerProperty quantiteProperty() {
        return quantite;
    }
}