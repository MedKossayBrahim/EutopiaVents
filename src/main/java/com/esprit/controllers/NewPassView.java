package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.services.ParticipantService;
import com.esprit.services.UserService;
import com.esprit.tests.Eutopia;
import com.esprit.utils.DataReceiver;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

public class NewPassView implements DataReceiver<String> {
    public TextField newPass;
    public TextField confNewPass;
    UserService us = new ParticipantService();
    String email;
    // User currentUser = Eutopia.getCurrentUser();

    public NewPassView() throws SQLException {
    }

    public void passUpdate() throws IOException {
        if (newPass.getText().equals(confNewPass.getText()) && newPass.getLength() > 5) {
            System.out.println("equals pass");
            // Hash the password before updating
            String hashedPassword = BCrypt.hashpw(newPass.getText(), BCrypt.gensalt());
            us.updatePass(email, hashedPassword);
            Eutopia.getSceneManager().switchScene("/login-view.fxml", null);
        }
        System.out.println("aaa chbik");
    }

    public void back(MouseEvent mouseEvent) throws IOException {
        Eutopia.getSceneManager().switchScene("/login-view.fxml", null);
    }

    @Override
    public void setData(String data) {
        email = data;

    }
}
