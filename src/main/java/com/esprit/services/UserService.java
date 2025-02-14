package com.esprit.services;


import com.esprit.models.Role;
import com.esprit.models.User;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    Connection connection = DataSource.getInstance().getConnection();

    public User signIn(String login, String passwd) {
        User user = null;
        String req = "SELECT * FROM users WHERE (email = ? OR username = ?) AND password = ?;";

        try ( // Ensure you have a method to get a connection
              PreparedStatement st = connection.prepareStatement(req)) {

            // Set parameters to prevent SQL injection
            st.setString(1, login);
            st.setString(2, login);
            st.setString(3, passwd);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) { // Use rs.next() to check if there is a result
                    user = new User(rs.getInt("userID"), rs.getString("nom"), rs.getString("prenom"), rs.getString("email"), rs.getString("userName"), rs.getString("image"), rs.getString("password"), rs.getInt("phone"), rs.getBoolean("isActive"), Role.valueOf(rs.getString("role")));
                    System.out.println("User logged in: " + user);

                } else {
                    System.out.println("No user found with the provided credentials.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during sign-in: " + e.getMessage());
        }

        return user; // Return the user object (or null if login fails)
    }
}
