package com.esprit.controllers;

import com.esprit.tests.Eutopia;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.IOException;

public class OtpView {


    public TextField email;
    public Button submit;

    @FXML
    private Group back;

    @FXML
    private Text welcome;


    public void back(MouseEvent mouseEvent) {
        Eutopia.getSceneManager().goBack();
    }

    public void toEnterOTP(ActionEvent actionEvent) throws IOException {
        Eutopia.getSceneManager().switchScene("/enterOPT-view.fxml", email.getText());
    }
}
