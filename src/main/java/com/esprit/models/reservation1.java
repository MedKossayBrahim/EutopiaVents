package com.esprit.models;

import java.time.LocalDateTime;

public class reservation1 {
    private int id;
    private int idLieu;
    private int idEvenement;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private int userID;
    private String typeReservation;

    // Constructeur par défaut
    public reservation1() {
    }

    public reservation1(int id, int idLieu, int idEvenement, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.id = id;
        this.idLieu = idLieu;
        this.idEvenement = idEvenement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdLieu() {
        return idLieu;
    }

    public void setIdLieu(int idLieu) {
        this.idLieu = idLieu;
    }

    public int getIdEvenement() {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getTypeReservation() {
        return typeReservation;
    }

    public void setTypeReservation(String typeReservation) {
        this.typeReservation = typeReservation;
    }
}

