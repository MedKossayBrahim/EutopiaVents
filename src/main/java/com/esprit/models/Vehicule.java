package com.Quickmove.models;

public class Vehicule {
    private int id_v;
    private String marque;
    private String type;
    private int annee;

    // Constructor for creating a new vehicle (without ID)
    public Vehicule(String marque, String type, int annee) {
        this.marque = marque;
        this.type = type;
        this.annee = annee;
    }

    // Constructor for creating a vehicle with all fields (including ID)
    public Vehicule(int id_v, String marque, String type, int annee) {
        this.id_v = id_v;
        this.marque = marque;
        this.type = type;
        this.annee = annee;
    }

    // Getters
    public int getId() { return id_v; }
    public String getMarque() { return marque; }
    public String getType() { return type; }
    public int getAnnee() { return annee; }

    // Setters
    public void setId(int id_v) { this.id_v = id_v; }
    public void setMarque(String marque) { this.marque = marque; }
    public void setType(String type) { this.type = type; }
    public void setAnnee(int annee) { this.annee = annee; }

    @Override
    public String toString() {
        return "Vehicule{id_v=" + id_v + ", marque='" + marque + "', type='" + type + "', annee=" + annee + "}";
    }
}