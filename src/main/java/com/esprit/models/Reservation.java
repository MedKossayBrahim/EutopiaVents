package com.esprit.models;

import java.sql.Timestamp;
import java.util.*;

public class Reservation {
    private int id;
    private int userId;
    private int materielId;  // Clé étrangère vers Materiel
    private int quantite;
    private double prixTotal; // Prix total = prix du matériel * quantité
    private java.sql.Timestamp dateDebut;
    private java.sql.Timestamp dateFin;
    private String status;
    private List<Materiel> materials;
    private Map<Materiel, Integer> quantities;
    private boolean paye;  // true si payé, false sinon
    private boolean recup; // true si récupéré, false sinon

    public Reservation() {
        this.materials = new ArrayList<>();
        this.quantities = new HashMap<>();
        this.status = "EN_ATTENTE";
        this.recup = false;
    }

    public Reservation(int id, int userId, int materielId, int quantite, double prixTotal, java.util.Date dateDebut, java.util.Date dateFin) {
        this.id = id;
        this.userId = userId;
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = convertToSqlTimestamp(dateDebut);
        this.dateFin = convertToSqlTimestamp(dateFin);
        this.recup = false;
    }

    private java.sql.Timestamp convertToSqlTimestamp(java.util.Date date) {
        if (date != null) {
            // Assurer que le timestamp inclut les heures, minutes et secondes
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

    public Reservation(int id, int materielId, int quantite, double prixTotal, java.util.Date dateDebut, java.util.Date dateFin) {
        this.id = id;
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = convertToSqlTimestamp(dateDebut);
        this.dateFin = convertToSqlTimestamp(dateFin);
        this.recup = false;
    }

    public Reservation(int materielId, int quantite, double prixTotal, java.util.Date dateDebut, java.util.Date dateFin, int userId) {
        this.materielId = materielId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateDebut = convertToSqlTimestamp(dateDebut);
        this.dateFin = convertToSqlTimestamp(dateFin);
        this.userId = userId;
        this.recup = false;
    }

    public Reservation(int id, int qte, int materielId) {
        this.id = id;
        this.quantite = qte;
        this.materielId = materielId;
        this.recup = false;
    }

    public Reservation(int qte, int materielId) {
        this.quantite = qte;
        this.materielId = materielId;
        this.recup = false;
    }

    public Reservation(int i) {
        this.id = i;
        this.recup = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.paye = "PAYÉ".equals(status);
    }

    public boolean isPaye() {
        return paye;
    }

    public void setPaye(boolean paye) {
        this.paye = paye;
        this.status = paye ? "PAYÉ" : "EN_ATTENTE";
    }

    public boolean isRecup() {
        return recup;
    }

    public void setRecup(boolean recup) {
        this.recup = recup;
    }

    public List<Materiel> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Materiel> materials) {
        this.materials = materials;
        if (materials != null && !materials.isEmpty()) {
            this.materielId = materials.get(0).getId(); // Pour la compatibilité avec l'ancien code
        }
    }

    public Map<Materiel, Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(Map<Materiel, Integer> quantities) {
        this.quantities = quantities;
        if (quantities != null && !quantities.isEmpty()) {
            // Mettre à jour la quantité totale pour la compatibilité
            this.quantite = quantities.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", userId=" + userId +
                ", materielId=" + materielId +
                ", quantite=" + quantite +
                ", prixTotal=" + prixTotal +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", status='" + status + '\'' +
                ", paye=" + paye +
                ", recup=" + recup +
                ", materials=" + materials +
                ", quantities=" + quantities +
                '}';
    }
}