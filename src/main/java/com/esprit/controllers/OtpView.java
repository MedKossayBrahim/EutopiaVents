package com.esprit.controllers;

import com.esprit.services.UserService;
import com.esprit.tests.Eutopia;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class OtpView {


    public TextField email;
    public Button submit;
    UserService us = new UserService();
    @FXML
    private Group back;
    @FXML
    private Text welcome;

    public OtpView() throws SQLException {
    }


    public void back(MouseEvent event) {
        System.out.println("back is pushed");
        Eutopia.getSceneManager().goBack();
    }

    public void toEnterOTP(ActionEvent actionEvent) throws IOException {

        if (isValidEmail(email.getText())) {
            if (us.userExistsByEmail(email.getText())) {
                Eutopia.getSceneManager().switchScene("/enterOPT-view.fxml", email.getText());

            } else {
                showAlert("User not found", "we can't find a user with that email address");

            }

        } else {
            showAlert("Invalid email", "Please enter a valid email");
        }


    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
