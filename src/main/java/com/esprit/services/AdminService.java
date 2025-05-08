package com.esprit.services;

import com.esprit.models.Admin;
import com.esprit.models.Role;
import com.esprit.utils.DataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService implements IService<Admin> {
    Connection connection = DataSource.getInstance().getConnection();

    public AdminService() throws SQLException {
    }

    @Override
    public void ajouter(Admin admin) {
        String req = "INSERT INTO users (fullName, userName, phone, email, password, image, isActive, role) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(req)) {
            // Hash the password before storing
            String hashedPassword = BCrypt.hashpw(admin.getPasswd(), BCrypt.gensalt());

            st.setString(1, admin.getFullname());
            st.setString(2, admin.getUserName());
            st.setInt(3, admin.getphone());
            st.setString(4, admin.getEmail());
            st.setString(5, hashedPassword);
            st.setString(6, "http://localhost/img/default.png");
            st.setBoolean(7, true);
            st.setString(8, "admin");

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("admin ajouté avec succès.");
            } else {
                System.out.println("Échec de l'ajout du admin.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du admin: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Admin admin) {

        // SQL query to update the admin's details
        String req = "UPDATE users SET fullName = ?, userName = ?, phone = ?, email = ?, password = ?, image = ?, isActive = ?, role = ? WHERE userID = ?";

        try ( // Ensure you have a method to get a connection
                PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters for the update query
            st.setString(1, admin.getFullname());
            st.setString(2, admin.getUserName());
            st.setInt(3, admin.getphone());
            st.setString(4, admin.getEmail());
            st.setString(5, admin.getPasswd());
            st.setString(6, admin.getImage()); // Assuming image is a string (can be null)
            st.setBoolean(7, admin.getActive()); // Assuming isActive is a boolean
            st.setString(8, admin.getRole().toString()); // Assuming role is an enum
            st.setInt(9, admin.getUserID()); // userID is used to identify the admin to update

            // Execute the update query
            int rowsAffected = st.executeUpdate();

            // Check if the update was successful
            if (rowsAffected > 0) {
                System.out.println("admin modifié avec succès.");
            } else {
                System.out.println("Aucun admin trouvé avec l'ID: " + admin.getUserID());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du admin: " + e.getMessage());
        }

    }

    @Override
    public void supprimer(Admin admin) {
        // SQL query to delete the admin
        String req = "DELETE FROM users WHERE userID = ?";

        try ( // Ensure you have a method to get a connection
                PreparedStatement st = connection.prepareStatement(req)) {

            // Set the userID parameter for the delete query
            st.setInt(1, admin.getUserID());

            // Execute the delete query
            int rowsAffected = st.executeUpdate();

            // Check if the deletion was successful
            if (rowsAffected > 0) {
                System.out.println("admin supprimé avec succès.");
            } else {
                System.out.println("Aucun admin trouvé avec l'ID: " + admin.getUserID());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du admin: " + e.getMessage());
        }
    }

    @Override
    public List<Admin> rechercher() {
        String req = "SELECT * FROM users";

        // List to store the admins
        List<Admin> admins = new ArrayList<>();

        try (// Ensure you have a method to get a connection
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(req)) {

            // Iterate through the result set and create admin objects
            while (rs.next()) {
                // Create a admin object using the parameterized constructor
                Admin adminTEMP = new Admin(rs.getInt("userID"), rs.getString("fullName"), rs.getString("email"),
                        rs.getString("password"), // Ensure this is hashed
                        rs.getString("userName"), rs.getString("image"), rs.getInt("phone"), rs.getBoolean("isActive"),
                        Role.valueOf(rs.getString("role")) // Assuming Role is an enum
                );

                // Add the admin to the list
                admins.add(adminTEMP);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des admins: " + e.getMessage());
        }

        // Return the list of admins
        return admins;
    }
}
