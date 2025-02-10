package com.esprit.models;

public class Reservations {
    private int id;
    private int evenementId;
    private int utilisateurId;
    private int quantite;
    private double prixTotal;
    private String statut; // 'en attente' ou 'confirmé'

    public Reservations(int id, int evenementId, int utilisateurId, int quantite, double prixTotal, String statut) {
        this.id = id;
        this.evenementId = evenementId;
        this.utilisateurId = utilisateurId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.statut = statut;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEvenementId() {
        return evenementId;
    }

    public void setEvenementId(int evenementId) {
        this.evenementId = evenementId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", evenementId=" + evenementId +
                ", utilisateurId=" + utilisateurId +
                ", quantite=" + quantite +
                ", prixTotal=" + prixTotal +
                ", statut='" + statut + '\'' +
                '}';
    }
}