package com.esprit.services;

import com.esprit.models.Role;
import com.esprit.models.User;
import com.esprit.utils.DataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    Connection connection = DataSource.getInstance().getConnection();

    public UserService() throws SQLException {
    }

    public User signIn(String login, String passwd) {
        User user = null;
        String req = "SELECT * FROM users WHERE (email = ? OR username = ?) AND isActive = TRUE;";

        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setString(1, login);
            st.setString(2, login);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password");

                    // Check if the hash is in Symfony format (starts with $2y$)
                    boolean isSymfonyHash = storedHashedPassword.startsWith("$2y$");

                    // Convert Symfony hash to BCrypt format if needed
                    if (isSymfonyHash) {
                        storedHashedPassword = storedHashedPassword.replace("$2y$", "$2a$");
                    }

                    // Compare the input password with the stored hashed password
                    if (BCrypt.checkpw(passwd, storedHashedPassword)) {
                        user = new User(
                                rs.getInt("userID"),
                                rs.getString("fullName"),
                                rs.getString("email"),
                                storedHashedPassword,
                                rs.getString("userName"),
                                rs.getString("image"),
                                rs.getInt("phone"),
                                rs.getBoolean("isActive"),
                                Role.valueOf(rs.getString("role")));
                        System.out.println("User logged in: " + user);
                    } else {
                        System.out.println("Invalid password.");
                    }
                } else {
                    System.out.println("No active user found with the provided credentials.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during sign-in: " + e.getMessage());
        }

        return user;
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
        // Hash the password using BCrypt with cost factor 10 and convert to Symfony
        // format
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        // Convert $2a$ to $2y$ for Symfony compatibility
        hashedPassword = hashedPassword.replace("$2a$", "$2y$");

        String req = "UPDATE users SET password = ? WHERE email = ?;";

        try (PreparedStatement st = connection.prepareStatement(req)) {
            st.setString(1, hashedPassword);
            st.setString(2, email);

            int rowsUpdated = st.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Password updated successfully for email: " + email);
            } else {
                System.out.println("No user found with email: " + email);
            }
        } catch (SQLException e) {
            System.out.println("Error updating password: " + e.getMessage());
        }
    }

    public List<User> getAllNonAdminUsers() throws SQLException {
        String query = "SELECT * FROM users WHERE role != 'admin'";
        List<User> users = new ArrayList<>();

        try (PreparedStatement st = connection.prepareStatement(query);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("userID"),
                        rs.getString("fullName"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("userName"),
                        rs.getString("image"),
                        rs.getInt("phone"),
                        rs.getBoolean("isActive"),
                        Role.valueOf(rs.getString("role")));
                users.add(user);
            }
        }
        return users;
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        String query = "UPDATE users SET isActive = ? WHERE userID = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setBoolean(1, isActive);
            st.setInt(2, userId);
            st.executeUpdate();
        }
    }

}
