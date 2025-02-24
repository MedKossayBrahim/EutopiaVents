package com.esprit.controllers;

import com.esprit.models.MaterielStats;
import com.esprit.utils.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatistiqueController {

    @FXML
    private BarChart<String, Number> barChartMateriels;
    @FXML
    private LineChart<String, Number> lineChartSaisons;
    @FXML
    private TableView<MaterielStats> tableMateriels;
    @FXML
    private TableColumn<MaterielStats, String> colNomMateriel;
    @FXML
    private TableColumn<MaterielStats, Integer> colNombreUtilisation;

    @FXML
    public void initialize() {
        // Lier les colonnes aux attributs de la classe MaterielStats
        colNomMateriel.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colNombreUtilisation.setCellValueFactory(new PropertyValueFactory<>("nombreUtilisation"));

        loadMaterielStats();
        loadPeakSeasons();
    }


    // 📌 Charger les matériels les plus utilisés
    private void loadMaterielStats() {
        String query = "SELECT m.libelle, COUNT(r.id) AS nombre_reservations " +
                "FROM Reservation r " +
                "JOIN Materiel m ON r.materiel_id = m.id " +
                "GROUP BY m.libelle " +
                "ORDER BY nombre_reservations DESC " +
                "LIMIT 10";

        ObservableList<MaterielStats> materielList = FXCollections.observableArrayList();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top Matériels Utilisés");

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String libelle = rs.getString("libelle");
                int nombreUtilisation = rs.getInt("nombre_reservations");

                // DEBUG: Vérifie si les données sont bien récupérées
                System.out.println("📌 Matériel : " + libelle + " | Utilisations : " + nombreUtilisation);

                // Ajouter au graphique
                series.getData().add(new XYChart.Data<>(libelle, nombreUtilisation));

                // Ajouter au tableau
                materielList.add(new MaterielStats(libelle, nombreUtilisation));
            }

            // Mettre à jour le graphique et le tableau
            barChartMateriels.getData().clear();
            barChartMateriels.getData().add(series);

            tableMateriels.setItems(materielList);
            tableMateriels.refresh();// AJOUTER CETTE LIGNE

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 📌 Charger les périodes de forte réservation (Peak Seasons)
    private void loadPeakSeasons() {
        String query = "SELECT MONTH(date_debut) AS mois, COUNT(*) AS nombre_reservations " +
                "FROM Reservation " +
                "GROUP BY mois " +
                "ORDER BY mois ASC";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Réservations par mois");

            while (rs.next()) {
                int mois = rs.getInt("mois");
                int nombreReservations = rs.getInt("nombre_reservations");

                // Convertir le numéro du mois en nom (ex: 1 -> Janvier)
                String moisNom = getNomMois(mois);

                series.getData().add(new XYChart.Data<>(moisNom, nombreReservations));
            }

            lineChartSaisons.getData().clear();
            lineChartSaisons.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 📌 Convertir numéro de mois en nom
    private String getNomMois(int mois) {
        String[] moisNoms = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return moisNoms[mois - 1]; // Les mois commencent à 1 en SQL
    }
}