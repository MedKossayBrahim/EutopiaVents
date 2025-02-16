package com.esprit.models;

import java.time.LocalDateTime;

public class reservation1 {
    private int id;
    private int idLieu; // ID du lieu
    private int idEvenement; // ID de l'événement
    private LocalDateTime dateDebut; // Date de début de la réservation
    private LocalDateTime dateFin; // Date de fin de la réservation

    // Constructeur
    public reservation1(int id, int idLieu, int idEvenement, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.id = id;
        this.idLieu = idLieu;
        this.idEvenement = idEvenement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdLieu() { return idLieu; }
    public void setIdLieu(int idLieu) { this.idLieu = idLieu; }

    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public String getNomLieu() {
        // Implémentez la logique pour récupérer le nom du lieu via l'ID
        // Exemple: return reservationService.getNomLieuById(this.idLieu);
        return "Nom du lieu"; // À adapter
    }

    public String getTitreEvent() {
        // Implémentez la logique pour récupérer le titre de l'événement via l'ID
        // Exemple: return reservationService.getTitreEventById(this.idEvenement);
        return "Titre de l'événement"; // À adapter
    }
    @Override
    public String toString() {
        return "Reservation{id=" + id + ", idLieu=" + idLieu + ", idEvenement=" + idEvenement + ", dateDebut=" + dateDebut + ", dateFin=" + dateFin + "}";
    }
}
