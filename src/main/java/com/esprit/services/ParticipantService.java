package com.esprit.services;


import com.esprit.models.Participant;
import com.esprit.models.Role;
import com.esprit.tests.Eutopia;
import com.esprit.utils.DataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class ParticipantService extends UserService implements IService<Participant> {

    Connection connection = DataSource.getInstance().getConnection();

    public ParticipantService() throws SQLException {
    }

    @Override
    public void ajouter(Participant participant) {

        String req = "INSERT INTO users (nom, prenom, userName, phone, email, password, image, isActive, role) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters to prevent SQL injection
            st.setString(1, participant.getNom());
            st.setString(2, participant.getPrenom());
            st.setString(3, participant.getUserName());
            st.setInt(4, participant.getphone());
            st.setString(5, participant.getEmail());
            st.setString(6, BCrypt.hashpw(participant.getPasswd(), BCrypt.gensalt()));
            st.setString(7, participant.getImage()); // Assuming image is optional and can be null
            st.setBoolean(8, true); // Assuming isActive is a string (adjust if it's a boolean)
            st.setString(9, "participant"); // Assuming role is a string

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Participant ajouté avec succès.");
                Eutopia.getSceneManager().switchScene("/login-view.fxml", null); // Start at Page1.fxml

            } else {
                System.out.println("Échec de l'ajout du participant.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du participant: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void modifier(Participant participant) {

        // SQL query to update the participant's details
        String req = "UPDATE users SET nom = ?, prenom = ?, userName = ?, phone = ?, email = ?, password = ?, image = ?, isActive = ?, role = ? WHERE userID = ?";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters for the update query
            st.setString(1, participant.getNom());
            st.setString(2, participant.getPrenom());
            st.setString(3, participant.getUserName());
            st.setInt(4, participant.getphone());
            st.setString(5, participant.getEmail());
            st.setString(6, participant.getPasswd());
            st.setString(7, participant.getImage()); // Assuming image is a string (can be null)
            st.setBoolean(8, participant.getActive()); // Assuming isActive is a boolean
            st.setString(9, participant.getRole().toString()); // Assuming role is an enum
            st.setInt(10, participant.getUserID()); // userID is used to identify the participant to update

            // Execute the update query
            int rowsAffected = st.executeUpdate();

            // Check if the update was successful
            if (rowsAffected > 0) {
                System.out.println("Participant modifié avec succès.");
                Eutopia.getSceneManager().goBack();
            } else {
                System.out.println("Aucun participant trouvé avec l'ID: " + participant.getUserID());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du participant: " + e.getMessage());
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
             Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(req)) {

            // Iterate through the result set and create Participant objects
            while (rs.next()) {
                // Create a Participant object using the parameterized constructor
                Participant participantTEMP = new Participant(rs.getInt("userID"), rs.getString("nom"), rs.getString("prenom"), rs.getString("email"), rs.getString("password"), // Ensure this is hashed
                        rs.getString("userName"), rs.getString("image"), rs.getInt("phone"), rs.getBoolean("isActive"), Role.valueOf(rs.getString("role")) // Assuming Role is an enum
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
}
