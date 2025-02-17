package com.esprit.controllers;


import com.esprit.models.User;
import com.esprit.services.UserService;
import com.esprit.tests.Eutopia;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;


public class LoginView {

    public Text signUP;
    public Text forgotPass;
    public TextField VloginPasswd;
    public CheckBox rememberMe;
    UserService us = new UserService();
    @FXML
    private Button login;
    @FXML
    private TextField loginEmail;
    @FXML
    private PasswordField loginPasswd;
    @FXML
    private Text welcome;
    private boolean isPasswordVisible = false;
    private Eutopia application;
    @FXML
    private ImageView togglePasswordIcon;
    @FXML
    private HBox passwordContainer;
    private TextField passwordTextField;

    public LoginView() throws SQLException {
    }


public void setApplication(Eutopia application) {
    this.application = application;
    // Set the stage size based on the screen size
    javafx.stage.Stage stage = (javafx.stage.Stage) login.getScene().getWindow();
    stage.setWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() * 0.8); // 80% of screen width
    stage.setHeight(javafx.stage.Screen.getPrimary().getVisualBounds().getHeight() * 0.8); // 80% of screen height
}


    @FXML
    private void login() throws IOException {
        User user = null;
        String password = isPasswordVisible ? passwordTextField.getText() : loginPasswd.getText();
        
        user = us.signIn(loginEmail.getText(), password);
        if (user != null) {
            try {
                // Create JSON object with user data
                JSONObject sessionData = new JSONObject();
                sessionData.put("userID", user.getUserID());
                sessionData.put("nom", user.getNom());
                sessionData.put("prenom", user.getPrenom());
                sessionData.put("email", user.getEmail());
                sessionData.put("passwd", user.getPasswd());
                sessionData.put("userName", user.getUserName());
                sessionData.put("image", user.getImage());
                sessionData.put("phone", user.getPhone());
                // Convert role to string to ensure proper JSON formatting
                sessionData.put("role", user.getRole().toString());

                // Write to user_session.json
                Path sessionPath = Paths.get("user_session.json");
                try (FileWriter file = new FileWriter(sessionPath.toFile())) {
                    file.write(sessionData.toJSONString());
                    file.flush();
                    System.out.println("Session file created successfully");
                }

                if (rememberMe.isSelected()) {
                    Eutopia.setCurrentUser(user);
                    UserSession.saveUser(user);
                    System.out.println("user saved");
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/events-view.fxml"));
                Parent root = loader.load();
                loginEmail.getScene().setRoot(root);
                
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Error creating session file");
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("alert");
            alert.setContentText("login not found!!");
            alert.show();
        }
    }

    @FXML
    private void signUp() throws IOException {
        Eutopia.getSceneManager().switchScene("/signUp-view.fxml", null);


    }

    @FXML
    private void forgetPass() throws IOException {
        Eutopia.getSceneManager().switchScene("/otp-view.fxml", null);

    }

    @FXML
    private void togglePasswordVisibility() {
        // Create TextField if it doesn't exist
        if (passwordTextField == null) {
            passwordTextField = new TextField();
            passwordTextField.setPrefHeight(38.0);
            passwordTextField.setPrefWidth(320.0);
            passwordTextField.setPromptText("Password");
        }

        if (isPasswordVisible) {
            // Switch back to PasswordField
            passwordTextField.setText("");
            passwordContainer.getChildren().set(1, loginPasswd);
            togglePasswordIcon.setImage(new Image(getClass().getResourceAsStream("/icons/hide.png")));
        } else {
            // Switch to TextField
            passwordTextField.setText(loginPasswd.getText());
            passwordContainer.getChildren().set(1, passwordTextField);
            togglePasswordIcon.setImage(new Image(getClass().getResourceAsStream("/icons/show.png")));
        }
        
        isPasswordVisible = !isPasswordVisible;
    }


}

