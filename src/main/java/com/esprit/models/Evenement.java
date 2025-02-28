package com.esprit.models;

public class Evenement {

    private int id;
    private String titre;
    private String description;
    private String dateDebut;
    private String dateFin;
    private int capacite;
    private int categorieId;
    private int lieuId;
    private int organisateurId;
    private double prix;
    private String statut;
    private String lieu_proprietaire;
    private String image;
    private String organisateurNom;
    private String categorieNom;
    private String lieuNom;

    // Constructor with all fields
    public Evenement(int id, String titre, String description, String dateDebut, String dateFin, int capacite, int categorieId, int lieuId, int organisateurId, double prix, String statut, String lieuProprietaire, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.capacite = capacite;
        this.categorieId = categorieId;
        this.lieuId = lieuId;
        this.organisateurId = organisateurId;
        this.prix = prix;
        this.statut = statut;
        this.lieu_proprietaire = lieuProprietaire;
        this.image = image;

        if (lieuId == 0 && (lieuProprietaire == null || lieuProprietaire.trim().isEmpty())) {
            throw new IllegalArgumentException("L'événement doit avoir un lieu existant ou un lieu propriétaire.");
        }
    }


    // Constructor without lieuProprietaire
    public Evenement(int id, String titre, String description, String dateDebut, String dateFin, int capacite, int categorieId, int lieuId, int organisateurId, double prix, String statut) {
        this(id, titre, description, dateDebut, dateFin, capacite, categorieId, lieuId, organisateurId, prix, statut, null, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public int getLieuId() {
        return lieuId;
    }

    public void setLieuId(int lieuId) {
        this.lieuId = lieuId;
    }

    public int getOrganisateurId() {
        return organisateurId;
    }

    public void setOrganisateurId(int organisateurId) {
        this.organisateurId = organisateurId;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLieu_proprietaire() {
        return lieu_proprietaire;
    }

    public void setLieu_proprietaire(String lieu_proprietaire) {
        this.lieu_proprietaire = lieu_proprietaire;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOrganisateurNom() {
        return organisateurNom;
    }

    public void setOrganisateurNom(String organisateurNom) {
        this.organisateurNom = organisateurNom;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
    }

    public String getLieuNom() {
        return lieuNom;
    }

    public void setLieuNom(String lieuNom) {
        this.lieuNom = lieuNom;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dateDebut='" + dateDebut + '\'' +
                ", dateFin='" + dateFin + '\'' +
                ", capacite=" + capacite +
                ", prix=" + prix +
                ", statut='" + statut + '\'' +
                ", image='" + image + '\'' + // Affichage de l'image
                '}';
    }
}

