package com.esprit.models;

public class Admin extends User{

    public Admin(String nom, String prenom, String email, String passwd, int tel) {
        super(nom, prenom, email, passwd, tel);
    }

    public Admin(int userID, String nom, String prenom, String email, String passwd, String userName, String image, int tel, Boolean isActive, Role role) {
        super(userID, nom, prenom, email, passwd, userName, image, tel, isActive, role);
    }
}
