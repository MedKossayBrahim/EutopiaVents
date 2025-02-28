package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.services.ParticipantService;
import com.esprit.services.UserService;
import com.esprit.tests.Eutopia;
import com.esprit.utils.DataReceiver;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.SQLException;

public class NewPassView implements DataReceiver<String> {
    public TextField newPass;
    public TextField confNewPass;
    UserService us = new ParticipantService();
    String email;
    //User currentUser = Eutopia.getCurrentUser();

    public NewPassView() throws SQLException {
    }


    public void passUpdate() throws IOException {

//        System.out.println(currentUser);
        if (newPass.getText().equals(confNewPass.getText()) && newPass.getLength() > 5){
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
            us.updatePass(email, newPass.getText());
            Eutopia.getSceneManager().switchScene("/login-view.fxml",null);


        }
        System.out.println("aaa chbik");
    }

    public void back(MouseEvent mouseEvent) throws IOException {
        Eutopia.getSceneManager().switchScene("/login-view.fxml",null);
    }

    @Override
    public void setData(String data) {
        email=data;

    }
}
