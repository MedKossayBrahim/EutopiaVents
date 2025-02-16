package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.services.ParticipantService;
import com.esprit.services.UserService;
import com.esprit.tests.Eutopia;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;

public class NewPassView {
    public TextField newPass;
    public TextField confNewPass;
    UserService us = new ParticipantService();
    User currentUser = Eutopia.getCurrentUser();

    public NewPassView() throws SQLException {
    }


    public void passUpdate() {

        System.out.println(currentUser);
        if (newPass.getText().equals(confNewPass.getText())){
            System.out.println("equals pass");
//            if (currentUser.getRole() == Role.Participant) {
//                System.out.println("role is Participant");
//
//                ps.modifier(new Participant(
//                        currentUser.getUserID(),
//                        currentUser.getNom(),
//                        currentUser.getPrenom(),
//                        currentUser.getEmail(),
//                        newPass.getText(),
//                        currentUser.getUserName(),
//                        currentUser.getImage(),
//                        currentUser.getPhone(),
//                        currentUser.getActive(),
//                        currentUser.getRole()
//
//                ));
//                System.out.println("passwd updated ");
//            }
            us.updatePass(currentUser.getUserID(), newPass.getText());


        }
        System.out.println("aaa chbik");
    }

    public void back(MouseEvent mouseEvent) {
        Eutopia.getSceneManager().goBack();
    }
}
