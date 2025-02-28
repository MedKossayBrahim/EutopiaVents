package com.esprit.services;


import com.esprit.models.Organisateur;
import com.esprit.models.Role;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrganisateurService implements IService<Organisateur> {
    Connection connection = DataSource.getInstance().getConnection();

    public OrganisateurService() throws SQLException {
    }

    @Override
    public void ajouter(Organisateur organisateur) {

        String req = "INSERT INTO users (fullName, userName, phone, email, password, image, isActive, role) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters to prevent SQL injection
            st.setString(1, organisateur.getFullname());
            st.setString(2, organisateur.getUserName());
            st.setInt(3, organisateur.getphone());
            st.setString(4, organisateur.getEmail());
            st.setString(5, organisateur.getPasswd());
            st.setString(6, "null"); // Assuming image is optional and can be null
            st.setBoolean(7, true); // Assuming isActive is a string (adjust if it's a boolean)
            st.setString(8, "organisateur"); // Assuming role is a string

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("organisateur ajouté avec succès.");
            } else {
                System.out.println("Échec de l'ajout du organisateur.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du organisateur: " + e.getMessage());
        }


    }

    @Override
    public void modifier(Organisateur organisateur) {

        // SQL query to update the organisateur's details
        String req = "UPDATE users SET fullName = ?, userName = ?, phone = ?, email = ?, password = ?, image = ?, isActive = ?, role = ? WHERE userID = ?";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters for the update query
            st.setString(1, organisateur.getFullname());
            st.setString(2, organisateur.getUserName());
            st.setInt(3, organisateur.getphone());
            st.setString(4, organisateur.getEmail());
            st.setString(5, organisateur.getPasswd());
            st.setString(6, organisateur.getImage()); // Assuming image is a string (can be null)
            st.setBoolean(7, organisateur.getActive()); // Assuming isActive is a boolean
            st.setString(8, organisateur.getRole().toString()); // Assuming role is an enum
            st.setInt(9, organisateur.getUserID()); // userID is used to identify the organisateur to update

            // Execute the update query
            int rowsAffected = st.executeUpdate();

            // Check if the update was successful
            if (rowsAffected > 0) {
                System.out.println("organisateur modifié avec succès.");
            } else {
                System.out.println("Aucun organisateur trouvé avec l'ID: " + organisateur.getUserID());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du organisateur: " + e.getMessage());
        }


    }

    @Override
    public void supprimer(Organisateur organisateur) {
        // SQL query to delete the organisateur
        String req = "DELETE FROM users WHERE userID = ?";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set the userID parameter for the delete query
            st.setInt(1, organisateur.getUserID());

            // Execute the delete query
            int rowsAffected = st.executeUpdate();

            // Check if the deletion was successful
            if (rowsAffected > 0) {
                System.out.println("organisateur supprimé avec succès.");
            } else {
                System.out.println("Aucun organisateur trouvé avec l'ID: " + organisateur.getUserID());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du organisateur: " + e.getMessage());
        }
    }


    @Override
    public List<Organisateur> rechercher() {
        String req = "SELECT * FROM users";

        // List to store the organisateurs
        List<Organisateur> organisateurs = new ArrayList<>();

        try (// Ensure you have a method to get a connection
             Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(req)) {

            // Iterate through the result set and create organisateur objects
            while (rs.next()) {
                // Create a organisateur object using the parameterized constructor
                Organisateur organisateurTEMP = new Organisateur(rs.getInt("userID"), rs.getString("nom"), rs.getString("prenom"), rs.getString("email"), rs.getString("password"), // Ensure this is hashed
                        rs.getString("userName"), rs.getString("image"), rs.getInt("phone"), rs.getBoolean("isActive"), Role.valueOf(rs.getString("role")) // Assuming Role is an enum
                );

                // Add the organisateur to the list
                organisateurs.add(organisateurTEMP);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des organisateurs: " + e.getMessage());
        }

        // Return the list of organisateurs
        return organisateurs;
    }
}
