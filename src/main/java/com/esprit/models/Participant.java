package com.esprit.models;

public class Participant extends User{

    public Participant(String nom, String prenom, String email, String passwd, int tel) {
        super(nom, prenom, email, passwd, tel);
    }

    public Participant(int userID, String nom, String prenom, String email, String passwd, String userName, String image, int tel, Boolean isActive, Role role) {
        super(userID, nom, prenom, email, passwd, userName, image, tel, isActive, role);
    }

    public Participant() {
        super();
    }
}
