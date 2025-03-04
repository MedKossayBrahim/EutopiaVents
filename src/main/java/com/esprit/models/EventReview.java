package com.esprit.models;

import java.time.LocalDateTime;

public class EventReview {
    private int id;
    private int evenementId;
    private int utilisateurId;
    private int note; // Rating from 1-5
    private String commentaire;
    private LocalDateTime dateCreation;
    
    // Propriétés pour l'affichage
    private String nomUtilisateur;
    private String titreEvenement;

    // Constructeur complet
    public EventReview(int id, int evenementId, int utilisateurId, int note, String commentaire, LocalDateTime dateCreation) {
        this.id = id;
        this.evenementId = evenementId;
        this.utilisateurId = utilisateurId;
        this.note = note;
        this.commentaire = commentaire;
        this.dateCreation = dateCreation;
        
        // Validation de la note
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
        }
    }
    
    // Constructeur pour création (sans ID)
    public EventReview(int evenementId, int utilisateurId, int note, String commentaire) {
        this(0, evenementId, utilisateurId, note, commentaire, LocalDateTime.now());
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

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
        }
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getTitreEvenement() {
        return titreEvenement;
    }

    public void setTitreEvenement(String titreEvenement) {
        this.titreEvenement = titreEvenement;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", evenementId=" + evenementId +
                ", utilisateurId=" + utilisateurId +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
} 