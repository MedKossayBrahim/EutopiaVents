package com.esprit.controllers;

import com.esprit.models.Participant;
import com.esprit.models.User;
import com.esprit.services.ParticipantService;
import com.esprit.tests.Eutopia;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;

public class editProfile implements Initializable {
    @FXML
    private TextField nom, username, email, password, phone;

    @FXML
    private ImageView photo;

    @FXML
    private Button editbtn, save;

    private File selectedImageFile;
    private String imagePath;
    private ParticipantService ps = new ParticipantService();
    private User currentUser;

    public editProfile() throws SQLException {
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = Eutopia.getCurrentUser();

        if (currentUser != null) {
            nom.setText(currentUser.getFullname());
            username.setText(currentUser.getUserName());
            email.setText(currentUser.getEmail());
            phone.setText(String.valueOf(currentUser.getPhone()));

            if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                imagePath = currentUser.getImage();
                photo.setImage(new Image(imagePath));
            }
        } else {
            System.err.println("No user is currently logged in.");
        }

        // Disable fields initially and hide save button
        setEditableFields(false);
        save.setVisible(false);
    }

    public void chooseImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            // Validate file size (e.g., 5MB limit)
            long fileSize = file.length();
            if (fileSize > 5 * 1024 * 1024) { // 5MB
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Image size must be less than 5MB.");
                alert.show();
                return;
            }

            selectedImageFile = file;
            // Preview the selected image locally
            photo.setImage(new Image(file.toURI().toString()));

            try {
                // Copy image to server and get URL
                imagePath = copyImageToServer();
            } catch (IOException e) {
                e.printStackTrace();
                // Show error alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Error uploading image!");
                alert.show();
            }
        }
    }

    private String copyImageToServer() throws IOException {
        if (selectedImageFile == null) {
            throw new IllegalArgumentException("No image selected.");
        }

        // Generate unique filename to prevent conflicts
        String originalFileName = selectedImageFile.getName();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // Server directory path (configurable)
        String destinationDir = System.getProperty("image.upload.dir", "C:/xampp/htdocs/img/");
        Path destinationPath = Paths.get(destinationDir + uniqueFileName);

        // Ensure directory exists
        Files.createDirectories(destinationPath.getParent());

        // Copy file to server
        Files.copy(selectedImageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Return accessible URL (configurable)
        String baseUrl = System.getProperty("image.base.url", "http://localhost/img/");
        return baseUrl + uniqueFileName;
    }

    public void save(ActionEvent actionEvent) {
        if (currentUser == null) {
            System.out.println("No user is currently logged in.");
            return;
        }

        try {
            currentUser.setFullname(nom.getText());
            currentUser.setUserName(username.getText());
            currentUser.setEmail(email.getText());

            if (!password.getText().isEmpty()) {
                currentUser.setPasswd(BCrypt.hashpw(password.getText(), BCrypt.gensalt()));
            }

            currentUser.setPhone(Integer.parseInt(phone.getText()));
            currentUser.setImage(imagePath);

            Participant user = new Participant(currentUser);
            user.setRole(currentUser.getRole());
            ps.modifier(user);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Profile updated successfully!");
            alert.show();

            // Disable fields and hide save button after saving
            setEditableFields(false);
            save.setVisible(false);
        } catch (Exception e) {
            System.out.println(e);
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error updating profile: " + e.getMessage());
            alert.show();
        }
    }

    public void editbtn(ActionEvent actionEvent) {
        // Enable fields and show save button
        setEditableFields(true);
        save.setVisible(true);
    }

    private void setEditableFields(boolean status) {
        nom.setEditable(status);
        username.setEditable(status);
        email.setEditable(status);
        password.setEditable(status);
        phone.setEditable(status);
    }
}