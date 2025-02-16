package com.esprit.services;

import com.esprit.models.PhotoLieu;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhotoLieuServiceImpl implements IService<PhotoLieu> {
    private Connection connection = DataSource.getInstance().getConnection();

    public PhotoLieuServiceImpl() throws SQLException {
    }

    @Override
    public void ajouter(PhotoLieu photo) {
        String sql = "INSERT INTO photoslieu (lieu_id, url_image) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, photo.getLieuId());
            stmt.setString(2, photo.getUrlImage());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        photo.setId(generatedKeys.getInt(1));
                    }
                }
            }
            System.out.println("Photo ajoutée : " + photo);
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la photo : " + e.getMessage());
        }
    }

    @Override
    public void modifier(PhotoLieu photo) {
        String sql = "UPDATE photoslieu SET lieu_id = ?, url_image = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, photo.getLieuId());
            stmt.setString(2, photo.getUrlImage());
            stmt.setInt(3, photo.getId());
            stmt.executeUpdate();
            System.out.println("Photo modifiée : " + photo);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la photo : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(PhotoLieu photo) {
        String sql = "DELETE FROM photoslieu WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, photo.getId());
            stmt.executeUpdate();
            System.out.println("Photo supprimée : " + photo);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la photo : " + e.getMessage());
        }
    }

    @Override
    public List<PhotoLieu> rechercher() {
        List<PhotoLieu> photos = new ArrayList<>();
        String sql = "SELECT * FROM photoslieu";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                PhotoLieu photo = new PhotoLieu(
                        rs.getInt("id"),
                        rs.getInt("lieu_id"),
                        rs.getString("url_image")
                );
                photos.add(photo);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche des photos : " + e.getMessage());
        }
        return photos;
    }
    //nouvelle methode
    public List<PhotoLieu> rechercherParLieuId(int lieuId) {
        List<PhotoLieu> photos = new ArrayList<>();
        String sql = "SELECT * FROM photoslieu WHERE lieu_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, lieuId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PhotoLieu photo = new PhotoLieu(
                            rs.getInt("id"),
                            rs.getInt("lieu_id"),
                            rs.getString("url_image")
                    );
                    photos.add(photo);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche par lieu : " + e.getMessage());
        }
        return photos;
    }
}