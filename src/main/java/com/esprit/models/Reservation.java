package com.esprit.models;

import java.sql.Timestamp;
import java.util.Date;

public class Reservation {
    private int id;
    private int userId;
    private int evenementId; // Clé étrangère vers Evenement
    private int materielId;  // Clé étrangère vers Materiel
    private int quantite;
    private double prixTotal; // Prix total = prix du matériel * quantité
    private java.sql.Timestamp dateDebut;
    private java.sql.Timestamp dateFin;
    private boolean paye;  // true si payé, false sinon

    public Reservation(int id, int userId, Integer evenementId, int materielId, int quantite, double prixTotal, java.util.Date dateDebut, java.util.Date dateFin) {
        this.id = id;
        this.userId = userId;
        this.evenementId = evenementId;
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = convertToSqlTimestamp(dateDebut);
        this.dateFin = convertToSqlTimestamp(dateFin);
    }

    private java.sql.Timestamp convertToSqlTimestamp(java.util.Date date) {
        if (date != null) {
            return new java.sql.Timestamp(date.getTime());
        }
        return null;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }





    public Reservation(int id, int evenementId, int materielId, int quantite, double prixTotal, java.util.Date dateDebut, java.util.Date dateFin) {
        this.id = id;
        this.evenementId = evenementId;
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = convertToSqlTimestamp(dateDebut);
        this.dateFin = convertToSqlTimestamp(dateFin);
    }

    public Reservation( int materielId, int quantite, double prixTotal, java.util.Date dateDebut, java.util.Date dateFin, int userId) {

        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = convertToSqlTimestamp(dateDebut);
        this.dateFin = convertToSqlTimestamp(dateFin);
        this.userId = userId;
    }

    public Reservation(int evenementId, int materielId, int quantite, double prixTotal, java.util.Date dateDebut, java.util.Date dateFin) {
        this.evenementId = evenementId;
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = convertToSqlTimestamp(dateDebut);
        this.dateFin = convertToSqlTimestamp(dateFin);
    }


    public Reservation(int id, int qte,int materielId,int evenementId) {
        this.id=id;
        this.quantite=qte;
        this.materielId=materielId;
        this.evenementId=evenementId;
    }
    public Reservation( int qte,int materielId,int evenementId) {

        this.quantite=qte;
        this.materielId=materielId;
        this.evenementId=evenementId;
    }

    public Reservation(int i) {
        this.id=i;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getEvenementId() { return evenementId; }
    public void setEvenementId(int evenementId) { this.evenementId = evenementId; }

    public int getMaterielId() { return materielId; }
    public void setMaterielId(int materielId) { this.materielId = materielId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }

    public java.sql.Timestamp getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(java.util.Date dateDebut) {
        this.dateDebut = convertToSqlTimestamp(dateDebut);
    }

    public java.sql.Timestamp getDateFin() {
        return dateFin;
    }

    public void setDateFin(java.util.Date dateFin) {
        this.dateFin = convertToSqlTimestamp(dateFin);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", evenementId=" + evenementId +
                ", materielId=" + materielId +
                ", quantite=" + quantite +
                ", prixTotal=" + prixTotal +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                '}';
    }
}
