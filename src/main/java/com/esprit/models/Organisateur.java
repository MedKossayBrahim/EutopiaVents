package com.esprit.models;

public class Organisateur extends User {

    public Organisateur(String nom, String prenom, String userName, String email, String passwd, int tel) {
        super(nom, userName, email, passwd, tel);
    }

    public Organisateur(int userID, String nom, String prenom, String email, String passwd, String userName, String image, int tel, Boolean isActive, Role role) {
        super(userID, nom, email, passwd, userName, image, tel, isActive, role);
    }
}
