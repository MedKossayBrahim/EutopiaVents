package com.esprit.controllers;

import com.esprit.models.Participant;
import com.esprit.models.Role;
import com.esprit.models.User;
import com.esprit.services.ParticipantService;
import com.esprit.tests.Eutopia;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class editProfile implements Initializable {
    @FXML
    private TextField nom, prenom, username, email, password, phone;

    @FXML
    private ImageView photo;

    @FXML
    private Button editbtn, save;

    private String imagePath;
    private ParticipantService ps = new ParticipantService();
    private User currentUser = Eutopia.getCurrentUser();

    public editProfile() throws SQLException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = Eutopia.getCurrentUser();

        if (currentUser != null) {
            nom.setText(currentUser.getNom());
            prenom.setText(currentUser.getPrenom());
            username.setText(currentUser.getUserName());
            email.setText(currentUser.getEmail());
            phone.setText(String.valueOf(currentUser.getPhone()));

            if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                imagePath = currentUser.getImage();
                photo.setImage(new Image(imagePath));
            }
        }

        // Disable fields initially and hide save button
        setEditableFields(false);
        save.setVisible(false);
    }

    public void chooseImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            imagePath = file.toURI().toString();
            photo.setImage(new Image(imagePath));
        }
    }

    public void save(ActionEvent actionEvent) {
        if (currentUser == null) {
            System.out.println("No user is currently logged in.");
            return;
        }

        currentUser.setNom(nom.getText());
        currentUser.setPrenom(prenom.getText());
        currentUser.setUserName(username.getText());
        currentUser.setEmail(email.getText());

        if (!password.getText().isEmpty()) {
            currentUser.setPasswd(BCrypt.hashpw(password.getText(), BCrypt.gensalt()));
        }

        currentUser.setPhone(Integer.parseInt(phone.getText()));
        currentUser.setImage(imagePath);

        Participant user = new Participant(currentUser);
        ps.modifier(user);

        // Disable fields and hide save button after saving
        setEditableFields(false);
        save.setVisible(false);
    }

    public void editbtn(ActionEvent actionEvent) {
        // Enable fields and show save button
        setEditableFields(true);
        save.setVisible(true);
    }

    private void setEditableFields(boolean status) {
        nom.setEditable(status);
        prenom.setEditable(status);
        username.setEditable(status);
        email.setEditable(status);
        password.setEditable(status);
        phone.setEditable(status);
    }
}
