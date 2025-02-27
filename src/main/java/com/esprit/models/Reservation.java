package com.esprit.models;

import java.sql.Timestamp;
import java.util.Date;

public class Reservation {
    private int id;
    private int evenementId; // Clé étrangère vers Evenement
    private int materielId;  // Clé étrangère vers Materiel
    private int quantite;
    private double prixTotal; // Prix total = prix du matériel * quantité
    private Date dateDebut;
    private Date dateFin;

    public Reservation(int id, int userId, int evenementId, int materielId, int quantite, double prixTotal, Timestamp dateDebut, Timestamp dateFin) {
        this.id = id;
        this.evenementId = evenementId;
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;

    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;


    public Reservation(int evenementId, int materielId, int quantite, double prixTotal, Date dateDebut, Date dateFin, int userId) {
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.userId = userId;
    }

    public Reservation(int evenementId, int materielId, int quantite, double prixTotal, Date dateDebut, Date dateFin) {
        this.evenementId = evenementId;
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
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

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

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