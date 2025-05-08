package com.esprit.controllers;

import com.esprit.models.Participant;
import com.esprit.services.ParticipantService;
import com.esprit.tests.Eutopia;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Pattern;

public class SignUpView {

    private static final String EMAIL_API_KEY = "8fabb3c2c2e34d11a55143617d80bea1";
    private static final String PHONE_API_KEY = "658f3a47dd40479faf06166a970c60cd";

    public Group back;
    public Text login;

    ParticipantService ps = new ParticipantService();

    @FXML
    private TextField email;
    @FXML
    private TextField nom;
    @FXML
    private TextField userName;
    @FXML
    private TextField passwd;
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
        // Input validation
        if (nom.getText().isEmpty() || userName.getText().isEmpty() || email.getText().isEmpty()
                || passwd.getText().isEmpty() || tel.getText().isEmpty()) {
            showAlert("Error", "All fields must be filled.");
            return;
        }

        if (!isValidEmail(email.getText())) {
            showAlert("Error", "Invalid email format.");
            return;
        }

        if (!isValidPassword(passwd.getText())) {
            showAlert("Error", "Password must be at least 6 characters long.");
            return;
        }

        if (!isValidPhoneNumber(tel.getText())) {
            showAlert("Error", "Invalid phone number. Must be exactly 8 digits.");
            return;
        }

        try {
            // Check if username already exists
            if (ps.isUsernameExists(userName.getText())) {
                showAlert("Error", "This username is already taken. Please choose another one.");
                return;
            }

            if (!validateEmailWithAPI(email.getText())) {
                showAlert("Error", "Invalid email address.");
                return;
            }

            if (!validatePhoneWithAPI(tel.getText())) {
                showAlert("Error", "Invalid phone number.");
                return;
            }

            // Hash the password using BCrypt
            String hashedPassword = BCrypt.hashpw(passwd.getText(), BCrypt.gensalt());

            ps.ajouter(new Participant(nom.getText(), userName.getText(), email.getText(), hashedPassword,
                    Integer.parseInt(tel.getText())));

            showAlert("Success", "Account created successfully!");

        } catch (Exception e) {
            showAlert("Error", "Failed to create account: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("\\d{8}"); // Ensures exactly 8 digits
    }

    private boolean validateEmailWithAPI(String email) {
        try {
            String apiUrl = "https://emailvalidation.abstractapi.com/v1/?api_key=" + EMAIL_API_KEY + "&email=" + email;
            Content content = Request.get(apiUrl).execute().returnContent();

            // Parse the JSON response
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(content.asString());

            // Extracting the relevant fields from the response
            boolean isValidFormat = (boolean) ((JSONObject) jsonResponse.get("is_valid_format")).get("value");
            boolean isDeliverable = "DELIVERABLE".equals(jsonResponse.get("deliverability"));
            boolean isDisposableEmail = (boolean) ((JSONObject) jsonResponse.get("is_disposable_email")).get("value");

            // Print debug information
            System.out.println("Valid Format: " + isValidFormat);
            System.out.println("Deliverable: " + isDeliverable);
            System.out.println("Disposable Email: " + isDisposableEmail);

            // Return true only if the email is valid, deliverable, and not disposable
            return isValidFormat && isDeliverable && !isDisposableEmail;

        } catch (IOException e) {
            System.out.println("Email validation error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Error parsing email validation response: " + e.getMessage());
            return false;
        }
    }

    private boolean validatePhoneWithAPI(String phone) {
        try {
            String apiUrl = "https://phonevalidation.abstractapi.com/v1/?api_key=" + PHONE_API_KEY + "&phone=+216"
                    + phone;
            Content content = Request.get(apiUrl).execute().returnContent();
            return content.asString().contains("\"valid\":true");
        } catch (IOException e) {
            System.out.println("Phone validation error: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void generateUsername() {
        String fullName = nom.getText().trim();
        if (!fullName.isEmpty()) {
            String baseUsername = fullName.replaceAll("\\s+", "").toLowerCase(); // Remove spaces, convert to lowercase
            int randomNumber = new Random().nextInt(900) + 100; // Generate a random 3-digit number
            userName.setText(baseUsername + randomNumber);
        }
    }

    @FXML
    private void initialize() {
        // Automatically generate a username when the name field changes
        nom.textProperty().addListener((observable, oldValue, newValue) -> generateUsername());

        // Ensure tel TextField only allows numbers and a max length of 8
        tel.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Only allows digits
                tel.setText(oldValue); // Revert to previous valid input
            }
            if (newValue.length() > 8) { // Restrict max length to 8
                tel.setText(oldValue);
            }
        });
    }

    public void back(MouseEvent mouseEvent) {
        Eutopia.getSceneManager().goBack();
    }

    public void login(MouseEvent mouseEvent) throws IOException {
        Eutopia.getSceneManager().switchScene("/login-view.fxml", null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
