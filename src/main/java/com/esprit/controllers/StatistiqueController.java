package com.esprit.controllers;

import com.esprit.utils.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class StatistiqueController {

    @FXML
    private LineChart<String, Number> lineChartSaisons;
    @FXML
    private LineChart<String, Number> lineChartRevenues;
    @FXML
    private TableView<MonthlyRevenue> tableRevenues;
    @FXML
    private TableColumn<MonthlyRevenue, String> colMois;
    @FXML
    private TableColumn<MonthlyRevenue, String> colRevenu;
    @FXML
    private Label lblTotalRevenue;

    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.FRANCE);

    public static class MonthlyRevenue {
        private final String mois;
        private final String revenu;

        public MonthlyRevenue(String mois, double revenu, NumberFormat format) {
            this.mois = mois;
            this.revenu = format.format(revenu) + " TND";
        }

        public String getMois() { return mois; }
        public String getRevenu() { return revenu; }
    }

    @FXML
    public void initialize() {
        currencyFormat.setMaximumFractionDigits(3);
        currencyFormat.setMinimumFractionDigits(3);

        // Configuration des colonnes du tableau
        colMois.setCellValueFactory(new PropertyValueFactory<>("mois"));
        colRevenu.setCellValueFactory(new PropertyValueFactory<>("revenu"));

        // Style des graphiques
        lineChartSaisons.setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 2px;");
        lineChartRevenues.setStyle("-fx-stroke: #4CAF50; -fx-stroke-width: 2px;");

        // Activer les symboles sur les points
        lineChartSaisons.setCreateSymbols(true);
        lineChartRevenues.setCreateSymbols(true);

        loadPeakSeasons();
        loadMonthlyRevenue();
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
                String moisNom = getNomMois(mois);
                series.getData().add(new XYChart.Data<>(moisNom, nombreReservations));
            }

            lineChartSaisons.getData().clear();
            lineChartSaisons.getData().add(series);

            // Appliquer le style à la série
            series.getNode().setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 2px;");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 📌 Charger les revenus mensuels
    private void loadMonthlyRevenue() {
        String query = "SELECT MONTH(r.date_debut) AS mois, SUM(m.prix) AS revenu_total " +
                "FROM Reservation r " +
                "JOIN Materiel m ON r.materiel_id = m.id " +
                "GROUP BY mois " +
                "ORDER BY mois ASC";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenus par mois (TND)");
            double totalRevenue = 0;

            ObservableList<MonthlyRevenue> revenueData = FXCollections.observableArrayList();

            while (rs.next()) {
                int mois = rs.getInt("mois");
                double revenu = rs.getDouble("revenu_total");
                totalRevenue += revenu;
                String moisNom = getNomMois(mois);

                // Ajouter au graphique
                series.getData().add(new XYChart.Data<>(moisNom, revenu));

                // Ajouter au tableau
                revenueData.add(new MonthlyRevenue(moisNom, revenu, currencyFormat));
            }

            // Mettre à jour le graphique
            lineChartRevenues.getData().clear();
            lineChartRevenues.getData().add(series);

            // Appliquer le style à la série
            series.getNode().setStyle("-fx-stroke: #4CAF50; -fx-stroke-width: 2px;");

            // Mettre à jour le tableau
            tableRevenues.setItems(revenueData);

            // Mettre à jour le label du revenu total
            lblTotalRevenue.setText(String.format("Revenu Total: %s TND", currencyFormat.format(totalRevenue)));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 📌 Convertir numéro de mois en nom
    private String getNomMois(int mois) {
        String[] moisNoms = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return moisNoms[mois - 1];
    }
}