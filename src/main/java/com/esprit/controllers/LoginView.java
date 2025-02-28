package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.services.UserService;
import com.esprit.tests.Eutopia;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class LoginView {

    private final UserService userService = new UserService();

    @FXML
    private TextField loginEmail, VloginPasswd; // Corrected variable name
    @FXML
    private PasswordField loginPasswd;
    @FXML
    private CheckBox rememberMe;
    @FXML
    private ImageView togglePasswordIcon;

    private boolean isPasswordVisible = false;

    public LoginView() throws SQLException {
    }

    /**
     * Handles the login process.
     */
    @FXML
    private void handleLogin() throws IOException {
        String email = loginEmail.getText().trim();
        String password = isPasswordVisible ? VloginPasswd.getText() : loginPasswd.getText();

        if (!validateInputs(email, password)) {
            return;
        }

        User user = userService.signIn(email, password);
        if (user != null) {
            createSession(user);
            navigateToEvents();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Login Failed", "Invalid email or password!");
        }
    }

    /**
     * Validates user input fields.
     */
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email field cannot be empty.");
            return false;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password field cannot be empty.");
            return false;
        }

        return true;
    }

    /**
     * Creates a session file with user details.
     */
    private void createSession(User user) {
        try {
            JSONObject sessionData = new JSONObject();
            sessionData.put("userID", user.getUserID());
            sessionData.put("fullName", user.getFullname());
            sessionData.put("email", user.getEmail());
            sessionData.put("passwd", user.getPasswd());
            sessionData.put("userName", user.getUserName());
            sessionData.put("image", user.getImage());
            sessionData.put("phone", user.getPhone());
            sessionData.put("role", user.getRole().toString());

            Path sessionPath = Paths.get("user_session.json");
            try (FileWriter file = new FileWriter(sessionPath.toFile())) {
                file.write(sessionData.toJSONString());
                file.flush();
            }

            Eutopia.setCurrentUser(user);
            if (rememberMe.isSelected()) {
                UserSession.saveUser(user);
            } else {
                UserSession.saveUser(null);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error creating session file.");
        }
    }

    /**
     * Navigates to the events page after successful login.
     */
    private void navigateToEvents() throws IOException {
        Eutopia.getSceneManager().switchScene("/events-view.fxml", null);
    }

    /**
     * Navigates to the sign-up page.
     */
    @FXML
    private void handleSignUp() throws IOException {
        Eutopia.getSceneManager().switchScene("/signUp-view.fxml", null);
    }

    /**
     * Navigates to the forgot password page.
     */
    @FXML
    private void handleForgotPassword() throws IOException {
        Eutopia.getSceneManager().switchScene("/otp-view.fxml", null);
    }

    /**
     * Toggles password visibility between plain text and hidden.
     */
    @FXML
    private void togglePasswordVisibility() {
        if (loginPasswd == null || VloginPasswd == null || togglePasswordIcon == null) {
            return; // Prevent NullPointerException
        }

        if (isPasswordVisible) {
            // Switch back to hidden PasswordField
            loginPasswd.setText(VloginPasswd.getText());
            loginPasswd.setVisible(true);
            VloginPasswd.setVisible(false);
            togglePasswordIcon.setImage(new Image(getClass().getResourceAsStream("/icons/hide.png")));
        } else {
            // Switch to visible TextField
            VloginPasswd.setText(loginPasswd.getText());
            VloginPasswd.setVisible(true);
            loginPasswd.setVisible(false);
            togglePasswordIcon.setImage(new Image(getClass().getResourceAsStream("/icons/show.png")));
        }

        isPasswordVisible = !isPasswordVisible;
    }

    /**
     * Displays an alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
