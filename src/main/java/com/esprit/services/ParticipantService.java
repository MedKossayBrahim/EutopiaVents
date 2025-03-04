package com.esprit.services;

import com.esprit.models.Participant;
import com.esprit.models.Role;
import com.esprit.tests.Eutopia;
import com.esprit.utils.DataSource;
import javafx.scene.control.Alert;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticipantService extends UserService implements IService<Participant> {

    Connection connection = DataSource.getInstance().getConnection();

    public ParticipantService() throws SQLException {
    }

    public boolean hasExistingRequest(int userId) {
        String req = "SELECT * FROM request WHERE userID = ?";
        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            return rs.next(); // Returns true if a request exists
        } catch (SQLException e) {
            System.out.println("Error checking request: " + e.getMessage());
            return false;
        }
    }

    public void sendRequest(int id) throws SQLException {
        // First check if user already has a request
        if (hasExistingRequest(id)) {
            throw new SQLException("You have already sent a request to become an organisateur");
        }

        String req = "INSERT INTO request (userID) VALUES (?)";
        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setInt(1, id);
            int rowsAffected = st.executeUpdate();

            if (rowsAffected <= 0) {
                throw new SQLException("Failed to send request");
            }
        }
    }

    public void updateRoleToOrganisateur(int userID) {
        String req = "UPDATE users SET role = ? WHERE userID = ?";

        try (
                PreparedStatement st = connection.prepareStatement(req)) {

            st.setString(1, "organisateur"); // New role
            st.setInt(2, userID); // userID is used to identify the user to update

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Role updated to 'organisateur' for user with ID: " + userID);
            } else {
                System.out.println("No user found with ID: " + userID);
            }
        } catch (SQLException e) {
            System.out.println("Error updating role to 'organisateur': " + e.getMessage());
        }
    }

    @Override
    public void ajouter(Participant participant) {

        String req = "INSERT INTO users (fullName, userName, phone, email, password, image, isActive, role) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters to prevent SQL injection
            st.setString(1, participant.getFullname());
            st.setString(2, participant.getUserName());
            st.setInt(3, participant.getphone());
            st.setString(4, participant.getEmail());
            st.setString(5, BCrypt.hashpw(participant.getPasswd(), BCrypt.gensalt()));
            st.setString(6, participant.getImage()); // Assuming image is optional and can be null
            st.setBoolean(7, true); // Assuming isActive is a string (adjust if it's a boolean)
            st.setString(8, "participant"); // Assuming role is a string

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Participant ajouté avec succès.");
                showAlert("success ", "user created");

                Eutopia.getSceneManager().switchScene("/login-view.fxml", null); // Start at Page1.fxml

            } else {
                System.out.println("Échec de l'ajout du participant.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("userName")) {
                showAlert("Error", "UserName already exists");

            } else if (e.getMessage().contains("email")) {
                showAlert("Error", "email already exists");

            }
            System.out.println("Erreur lors de l'ajout du participant: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void modifier(Participant participant) {

        // SQL query to update the participant's details
        String req = "UPDATE users SET fullName = ?, userName = ?, phone = ?, email = ?, password = ?, image = ?, isActive = ?, role = ? WHERE userID = ?";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters for the update query
            st.setString(1, participant.getFullname());
            st.setString(2, participant.getUserName());
            st.setInt(3, participant.getphone());
            st.setString(4, participant.getEmail());
            st.setString(5, participant.getPasswd());
            st.setString(6, participant.getImage()); // Assuming image is a string (can be null)
            st.setBoolean(7, participant.getActive()); // Assuming isActive is a boolean
            st.setString(8, participant.getRole().toString()); // Assuming role is an enum
            st.setInt(9, participant.getUserID()); // userID is used to identify the participant to update

            // Execute the update query
            int rowsAffected = st.executeUpdate();

            // Check if the update was successful
            if (rowsAffected > 0) {
                System.out.println("Participant modifié avec succès.");

                Eutopia.getSceneManager().switchScene("/events-view.fxml", null);
            } else {
                System.out.println("Aucun participant trouvé avec l'ID: " + participant.getUserID());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du participant: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void supprimer(Participant participant) {
        // SQL query to delete the participant
        String req = "DELETE FROM users WHERE userID = ?";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set the userID parameter for the delete query
            st.setInt(1, participant.getUserID());

            // Execute the delete query
            int rowsAffected = st.executeUpdate();

            // Check if the deletion was successful
            if (rowsAffected > 0) {
                System.out.println("Participant supprimé avec succès.");
            } else {
                System.out.println("Aucun participant trouvé avec l'ID: " + participant.getUserID());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du participant: " + e.getMessage());
        }
    }

    @Override
    public List<Participant> rechercher() {
        String req = "SELECT * FROM users";

        // List to store the participants
        List<Participant> participants = new ArrayList<>();

        try (// Ensure you have a method to get a connection
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            // Iterate through the result set and create Participant objects
            while (rs.next()) {
                // Create a Participant object using the parameterized constructor
                Participant participantTEMP = new Participant(rs.getInt("userID"), rs.getString("fullName"),
                        rs.getString("email"), rs.getString("password"), // Ensure this is hashed
                        rs.getString("userName"), rs.getString("image"), rs.getInt("phone"), rs.getBoolean("isActive"),
                        Role.valueOf(rs.getString("role")) // Assuming Role is an enum
                );

                // Add the participant to the list
                participants.add(participantTEMP);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des participants: " + e.getMessage());
        }

        // Return the list of participants
        return participants;
    }

    public void deleteRequest(int userId) throws SQLException {
        String req = "DELETE FROM request WHERE userID = ?";
        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setInt(1, userId);
            st.executeUpdate();
        }
    }

    public List<Map<String, Object>> getUserRequests() {
        String query = "SELECT u.userID, u.fullName, u.userName, u.email, r.created_at " +
                "FROM users u " +
                "JOIN request r ON u.userID = r.userID";

        List<Map<String, Object>> userRequests = new ArrayList<>();

        try (PreparedStatement st = connection.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("userID", rs.getInt("userID"));
                row.put("fullName", rs.getString("fullName"));
                row.put("userName", rs.getString("userName"));
                row.put("email", rs.getString("email"));
                row.put("createdAt", rs.getDate("created_at"));
                userRequests.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user requests: " + e.getMessage());
        }

        return userRequests;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isUsernameExists(String username) {
        String req = "SELECT COUNT(*) FROM users WHERE userName = ?";
        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
        }
        return false;
    }

    public boolean isUsernameExistsExcept(String username, int currentUserId) {
        String req = "SELECT COUNT(*) FROM users WHERE userName = ? AND userID != ?";
        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setString(1, username);
            st.setInt(2, currentUserId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
        }
        return false;
    }
}