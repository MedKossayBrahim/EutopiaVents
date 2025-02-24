package com.esprit.models;

public class categorie {
        private int id;
        private String nom;
        private String description;

        public categorie(int id, String nom, String description) {
            this.id = id;
            this.nom = nom;
            this.description = description;
        }

        public categorie(String nom, String description) {
            this.nom = nom;
            this.description = description;
        }

    public categorie(int id, String nom) {
            this.id = id;
            this.nom = nom;
    }

    public categorie(int id) {
        this.id = id;
    }

    public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return "Categorie{" +
                    "id=" + id +
                    ", nom='" + nom + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }


}
