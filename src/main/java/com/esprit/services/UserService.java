package com.esprit.services;


import com.esprit.models.Role;
import com.esprit.models.User;
import com.esprit.utils.DataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    Connection connection = DataSource.getInstance().getConnection();

    public UserService() throws SQLException {
    }

    public User signIn(String login, String passwd) {
        User user = null;
        String req = "SELECT * FROM users WHERE email = ? OR username = ?;"; // Query to find the user

        try (PreparedStatement st = connection.prepareStatement(req)) {
            // Set parameters to prevent SQL injection
            st.setString(1, login);
            st.setString(2, login);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) { // Check if a user was found
                    String storedHashedPassword = rs.getString("password"); // Get the stored hashed password

                    // Compare the input password with the stored hashed password
                    if (BCrypt.checkpw(passwd, storedHashedPassword)) {
                        // Passwords match, create the User object
                        user = new User(rs.getInt("userID"), rs.getString("fullName"), rs.getString("email"), storedHashedPassword, rs.getString("userName"), rs.getString("image"),
                                // Use the stored hashed password
                                rs.getInt("phone"), rs.getBoolean("isActive"), Role.valueOf(rs.getString("role")));
                        System.out.println("User logged in: " + user);
                    } else {
                        System.out.println("Invalid password.");
                    }
                } else {
                    System.out.println("No user found with the provided credentials.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during sign-in: " + e.getMessage());
        }

        return user; // Return the user object (or null if login fails)
    }
    public boolean userExistsByEmail(String email) {
        String req = "SELECT COUNT(*) FROM users WHERE email = ?;";
        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // If count > 0, user exists
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking user existence: " + e.getMessage());
        }
        return false;
    }

    public void updatePass(String email, String password) {
        // Hash the password before storing it (use a secure hashing algorithm like BCrypt)
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String req = "UPDATE users SET password = ? WHERE email = ?;";

        try (PreparedStatement st = connection.prepareStatement(req)) {
            // Set parameters to prevent SQL injection
            st.setString(1, hashedPassword); // Use the hashed password
            st.setString(2, email);

            // Execute the update
            int rowsUpdated = st.executeUpdate(); // Use executeUpdate() for UPDATE queries

            if (rowsUpdated > 0) {
                System.out.println("Password updated successfully for email: " + email);
            } else {
                System.out.println("No user found with email: " + email);
            }
        } catch (SQLException e) {
            System.out.println("Error updating password: " + e.getMessage());
        }
    }

}
