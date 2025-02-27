package com.esprit.models;

public class Participant extends User{

    public Participant(String nom,String userName , String email, String passwd, int tel) {
        super(nom,userName, email, passwd, tel);
    }

    public Participant(int userID, String nom, String email, String passwd, String userName, String image, int tel, Boolean isActive, Role role) {
        super(userID, nom, email, passwd, userName, image, tel, isActive, role);
    }


    public Participant(User user) {
        super(
                user.getUserID(),
                user.getFullname(),
                user.getEmail(),
                user.getPasswd(),
                user.getUserName(),
                user.getImage(),
                user.getPhone(),
                user.getActive(),
                Role.Participant // Ensures role is set to Participant
        );
    }
}
