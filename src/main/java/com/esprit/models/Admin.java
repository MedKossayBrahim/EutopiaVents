package com.esprit.models;

public class Admin extends User {

    public Admin(String nom, String prenom, String userName, String email, String passwd, int tel) {
        super(nom, userName, email, passwd, tel);
    }

    public Admin(int userID, String nom,  String email, String passwd, String userName, String image, int tel, Boolean isActive, Role role) {
        super(userID, nom, email, passwd, userName, image, tel, isActive, role);
    }
}
