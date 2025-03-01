package com.esprit.controllers;

import com.esprit.models.Participant;
import com.esprit.services.ParticipantService;
import com.esprit.tests.Eutopia;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.SQLException;

public class SignUpView {

    public Group back;
    public TextField userName;
    public Text login;
    ParticipantService ps = new ParticipantService();
    @FXML
    private TextField email;
    @FXML
    private TextField nom;
    @FXML
    private TextField passwd;
    @FXML
    private TextField prenom;
    @FXML
    private Button submit;
    @FXML
    private TextField tel;
    @FXML
    private Text welcome;

    public SignUpView() throws SQLException {
    }

    @FXML
    void signUp(ActionEvent event) {
        ps.ajouter(new Participant(nom.getText(), prenom.getText(), userName.getText(), email.getText(), passwd.getText(), Integer.parseInt(tel.getText())

        ));


    }

    public void back(MouseEvent mouseEvent) {
        Eutopia.getSceneManager().goBack();
    }

    public void login(MouseEvent mouseEvent) throws IOException {
        Eutopia.getSceneManager().switchScene("/login-view.fxml",null);
    }
}
