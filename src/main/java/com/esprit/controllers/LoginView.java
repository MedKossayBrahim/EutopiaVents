package com.esprit.controllers;


import com.esprit.models.User;
import com.esprit.services.UserService;
import com.esprit.tests.MainProgGUI;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.IOException;


public class LoginView {

    public Text signUP;
    public Text forgotPass;
    public TextField VloginPasswd;
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


    @FXML
    private void login() throws IOException {
        User user = null;
        String passwd;
        if (isPasswordVisible) {
            passwd = VloginPasswd.getText();
        } else {
            passwd = loginPasswd.getText();
        }
        user =  us.signIn(loginEmail.getText(), passwd);
        if (user != null) {
            UserSession.saveUser(user);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/otp-view.fxml"));
            Parent root = loader.load();
            loginEmail.getScene().setRoot(root);
        } else {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("alert");
            alert.setContentText("login not found!!");
            alert.show();
        }


        System.out.println(loginEmail.getText() + loginPasswd.getText());
    }

    @FXML
    private void signUp() throws IOException {
        MainProgGUI.getSceneManager().switchScene("/signUp-view.fxml",null);


    }

    @FXML
    private void forgetPass() throws IOException {
        MainProgGUI.getSceneManager().switchScene("/otp-view.fxml",null);

    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible; // Toggle state

        if (isPasswordVisible) {
            VloginPasswd.setText(loginPasswd.getText());
            VloginPasswd.setVisible(true);
            loginPasswd.setVisible(false);
            //toggleButton.setText("🙈"); // Hide password
        } else {
            loginPasswd.setText(VloginPasswd.getText());
            loginPasswd.setVisible(true);
            VloginPasswd.setVisible(false);
            //toggleButton.setText("👁"); // Show password
        }
    }


}

