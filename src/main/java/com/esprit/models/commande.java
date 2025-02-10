package com.esprit.models;

public class commande {
    private int id;
    private int participantId;
    private int produitId;
    private int quantite;

    public commande(int id, int participantId , int produitId, int quantite) {
        this.id = id;
        this.participantId = participantId;
        this.produitId = produitId;
        this.quantite = quantite;
    }

    public commande(int participantId, int produitId, int quantite) {
        this.participantId = participantId;
        this.produitId = produitId;
        this.quantite = quantite;
    }

    public commande(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getParticipantId () { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }

    public int getProduitId() { return produitId; }
    public void setProduitId(int produitId) { this.produitId = produitId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return "commande{" +
                "id=" + id +
                ", participantId=" + participantId +
                ", produitId=" + produitId +
                ", quantite=" + quantite +
                '}';
    }
}
