package com.esprit.controllers;

import com.esprit.models.Participant;
import com.esprit.services.ParticipantService;
import com.esprit.tests.MainProgGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class SignUpView {

    public Group back;
    public TextField userName;
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

    @FXML
    void signUp(ActionEvent event) {
        ps.ajouter(new Participant(nom.getText(), prenom.getText(), userName.getText(), email.getText(), passwd.getText(), Integer.parseInt(tel.getText())

        ));


    }

    public void back(MouseEvent mouseEvent) {
        MainProgGUI.getSceneManager().goBack();
    }
}
