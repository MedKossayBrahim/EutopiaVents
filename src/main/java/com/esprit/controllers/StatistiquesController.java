package com.esprit.controllers;

import com.esprit.services.StatistiquesServiceImpl;
import com.esprit.services.ReservationServiceImpl;
import com.esprit.services.LieuServiceImpl;
import com.esprit.models.reservation1;
import com.esprit.models.Lieu;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.PageSize;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class StatistiquesController implements Initializable {

    @FXML
    private WebView webView;
    @FXML
    private Button exportButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button backButton;
    @FXML
    private Label lastUpdateLabel;
    @FXML
    private Label totalSallesLabel;
    @FXML
    private Label totalReservationsLabel;
    @FXML
    private Label revenuTotalLabel;

    private StatistiquesServiceImpl statistiquesService;
    private ReservationServiceImpl reservationService;
    private LieuServiceImpl lieuService;

    @FXML
    public void goBack() {
        try {
            // Get the current stage
            javafx.scene.Node source = backButton;
            javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
            
            // Get the current scene's root
            javafx.scene.Parent currentRoot = source.getScene().getRoot();
            
            // Find the navbar in the current scene
            javafx.scene.layout.VBox navbar = null;
            if (currentRoot instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox container = (javafx.scene.layout.HBox) currentRoot;
                for (javafx.scene.Node node : container.getChildren()) {
                    if (node instanceof javafx.scene.layout.VBox && node.getId() != null && node.getId().equals("navbar")) {
                        navbar = (javafx.scene.layout.VBox) node;
                        break;
                    }
                }
            }
            
            // Load the main menu view
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/MainMenu.fxml"));
            javafx.scene.Parent mainMenuContent = loader.load();
            
            // Create a container with navbar and main menu content
            javafx.scene.layout.HBox container = new javafx.scene.layout.HBox();
            container.setSpacing(0);
            container.setStyle("-fx-background-color: white;");
            
            if (navbar != null) {
                // If navbar was found, reuse it
                container.getChildren().addAll(navbar, mainMenuContent);
            } else {
                // If navbar wasn't found, load MainMenu.fxml directly
                container.getChildren().add(mainMenuContent);
            }
            
            javafx.scene.layout.HBox.setHgrow(mainMenuContent, javafx.scene.layout.Priority.ALWAYS);
            
            // Set the new scene
            javafx.scene.Scene scene = new javafx.scene.Scene(container);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de navigation", "Impossible de retourner à la page précédente: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            statistiquesService = new StatistiquesServiceImpl();
            reservationService = new ReservationServiceImpl();
            lieuService = new LieuServiceImpl();
            setupButtons();
            loadStatistiques();
            updateLastUpdateLabel();
            updateStatCards();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur d'initialisation", e.getMessage());
        }
    }

    private void setupButtons() {
        refreshButton.setOnAction(e -> {
            loadStatistiques();
            updateLastUpdateLabel();
            updateStatCards();
        });

        exportButton.setOnAction(e -> exportStatistiques());
    }

    private void updateLastUpdateLabel() {
        String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        );
        lastUpdateLabel.setText(timestamp);
    }

    private void updateStatCards() {
        try {
            String statistiquesJson = statistiquesService.getStatistiquesJson();
            JSONObject stats = new JSONObject(statistiquesJson);
            JSONObject general = stats.getJSONObject("general");

            totalSallesLabel.setText(String.valueOf(general.getInt("nombreTotal")));

            java.util.List<reservation1> reservations = reservationService.rechercher();
            totalReservationsLabel.setText(String.valueOf(reservations.size()));

            double revenuTotal = calculerRevenuTotal(reservations);
            revenuTotalLabel.setText(String.format("%.2f DT", revenuTotal));

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de mise à jour", e.getMessage());
        }
    }

    private double calculerRevenuTotal(java.util.List<reservation1> reservations) {
        try {
            double revenuTotal = 0.0;

            for (reservation1 reservation : reservations) {
                int lieuId = reservation.getIdLieu();
                Lieu lieu = lieuService.getLieuById(lieuId);
                if (lieu == null) continue;

                double prixLieu = lieu.getPrix();
                if (prixLieu <= 0) continue;

                LocalDateTime debut = reservation.getDateDebut();
                LocalDateTime fin = reservation.getDateFin();

                long jours = Math.max(1, ChronoUnit.DAYS.between(debut, fin));

                if (jours == 1 && debut.toLocalDate().equals(fin.toLocalDate())) {
                    long heures = ChronoUnit.HOURS.between(debut, fin);
                    if (heures < 24) {
                        revenuTotal += (prixLieu * heures) / 24.0;
                    } else {
                        revenuTotal += prixLieu;
                    }
                } else {
                    revenuTotal += prixLieu * jours;
                }
            }

            return revenuTotal;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private void loadStatistiques() {
        try {
            String statistiquesJson = statistiquesService.getStatistiquesJson();
            String html = generateHtml(statistiquesJson);
            webView.getEngine().loadContent(html);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", e.getMessage());
        }
    }

    private void exportStatistiques() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les statistiques");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
            if (file != null) {
                generatePDF(file, statistiquesService.getStatistiquesJson());
                showInfoAlert("Exportation réussie",
                        "Les statistiques ont été exportées en PDF avec succès !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur d'exportation", e.getMessage());
        }
    }

    private void generatePDF(File file, String statistiquesJson) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        document.open();

        // En-tête
        Paragraph title = new Paragraph("Rapport des Statistiques", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Paragraph date = new Paragraph("Généré le : " + dateFormat.format(new Date()), smallFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);
        document.add(new Paragraph("\n"));

        // Informations financières
        document.add(new Paragraph("Informations Financières", subtitleFont));
        PdfPTable tableFinancial = new PdfPTable(2);
        tableFinancial.setWidthPercentage(100);
        tableFinancial.setSpacingBefore(10f);
        tableFinancial.setSpacingAfter(10f);

        addRow(tableFinancial, "Nombre total de réservations", totalReservationsLabel.getText());
        addRow(tableFinancial, "Revenu total", revenuTotalLabel.getText());
        document.add(tableFinancial);
        document.add(new Paragraph("\n"));

        // Statistiques générales
        JSONObject stats = new JSONObject(statistiquesJson);
        JSONObject general = stats.getJSONObject("general");

        document.add(new Paragraph("Statistiques Générales", subtitleFont));
        PdfPTable tableGeneral = new PdfPTable(2);
        tableGeneral.setWidthPercentage(100);
        tableGeneral.setSpacingBefore(10f);
        tableGeneral.setSpacingAfter(10f);

        addRow(tableGeneral, "Nombre total de lieux", String.valueOf(general.getInt("nombreTotal")));
        addRow(tableGeneral, "Capacité minimale", String.valueOf(general.getInt("capaciteMin")));
        addRow(tableGeneral, "Capacité maximale", String.valueOf(general.getInt("capaciteMax")));
        addRow(tableGeneral, "Capacité moyenne", String.valueOf(general.getInt("capaciteMoyenne")));
        addRow(tableGeneral, "Capacité médiane", String.valueOf(general.getInt("capaciteMediane")));
        addRow(tableGeneral, "Capacité totale", String.valueOf(general.getInt("capaciteTotale")));

        document.add(tableGeneral);
        document.add(new Paragraph("\n"));

        // Statistiques par catégorie
        document.add(new Paragraph("Statistiques par Catégorie", subtitleFont));
        PdfPTable tableCategories = new PdfPTable(3);
        tableCategories.setWidthPercentage(100);
        tableCategories.setSpacingBefore(10f);
        tableCategories.setSpacingAfter(10f);

        tableCategories.addCell(createCell("Catégorie", true));
        tableCategories.addCell(createCell("Nombre de lieux", true));
        tableCategories.addCell(createCell("Capacité moyenne", true));

        JSONArray categories = stats.getJSONArray("categories");
        for (int i = 0; i < categories.length(); i++) {
            JSONObject cat = categories.getJSONObject(i);
            tableCategories.addCell(createCell(cat.getString("categorie"), false));
            tableCategories.addCell(createCell(String.valueOf(cat.getInt("nombre")), false));
            tableCategories.addCell(createCell(String.valueOf(cat.getInt("capaciteMoyenne")), false));
        }

        document.add(tableCategories);

        // Pied de page
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Fin du rapport", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    private PdfPCell createCell(String content, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(content));
        cell.setPadding(5);
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        return cell;
    }

    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(createCell(label, false));
        table.addCell(createCell(value, false));
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String generateHtml(String statistiquesJson) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <script type="text/javascript">
                    google.charts.load('current', {'packages':['corechart']});
                    google.charts.setOnLoadCallback(drawCharts);
                    
                    const statistiques = """ + statistiquesJson + """
                    ;
                    
                    function drawCharts() {
                        drawGeneralStats();
                        drawCategoriesChart();
                        drawDistributionChart();
                        drawEvolutionChart();
                    }
                    
                    function drawGeneralStats() {
                        const stats = statistiques.general;
                        document.getElementById('generalStats').innerHTML = `
                            <div class="stats-card">
                                <h3>Statistiques Générales</h3>
                                <table class="stats-table">
                                    <tr><td>Nombre total de lieux</td><td>${stats.nombreTotal}</td></tr>
                                    <tr><td>Capacité minimale</td><td>${stats.capaciteMin}</td></tr>
                                    <tr><td>Capacité maximale</td><td>${stats.capaciteMax}</td></tr>
                                    <tr><td>Capacité moyenne</td><td>${stats.capaciteMoyenne}</td></tr>
                                    <tr><td>Capacité médiane</td><td>${stats.capaciteMediane}</td></tr>
                                    <tr><td>Capacité totale</td><td>${stats.capaciteTotale}</td></tr>
                                </table>
                            </div>`;
                    }
                    
                    function drawCategoriesChart() {
                        const data = new google.visualization.DataTable();
                        data.addColumn('string', 'Catégorie');
                        data.addColumn('number', 'Nombre de lieux');
                        data.addRows(statistiques.categories.map(cat => [cat.categorie, cat.nombre]));
                        
                        const options = {
                            title: 'Répartition par catégorie',
                            pieHole: 0.4,
                            backgroundColor: 'transparent',
                            chartArea: {width: '80%', height: '80%'}
                        };
                        
                        const chart = new google.visualization.PieChart(document.getElementById('categoriesChart'));
                        chart.draw(data, options);
                    }
                    
                    function drawDistributionChart() {
                        const data = new google.visualization.DataTable();
                        data.addColumn('string', 'Tranche de capacité');
                        data.addColumn('number', 'Nombre de lieux');
                        data.addRows(statistiques.distribution.map(d => [d.tranche, d.nombre]));
                        
                        const options = {
                            title: 'Distribution des capacités',
                            legend: { position: 'none' },
                            backgroundColor: 'transparent',
                            bars: 'vertical'
                        };
                        
                        const chart = new google.visualization.ColumnChart(document.getElementById('distributionChart'));
                        chart.draw(data, options);
                    }
                    
                    function drawEvolutionChart() {
                        const data = new google.visualization.DataTable();
                        data.addColumn('string', 'Mois');
                        data.addColumn('number', 'Réservations');
                        data.addColumn('number', 'Revenu');
                        
                        data.addRows([
                            ['Jan', 10, 1000],
                            ['Fév', 15, 1500],
                            ['Mar', 20, 2000]
                        ]);
                        
                        const options = {
                            title: 'Évolution des réservations et revenus',
                            curveType: 'function',
                            backgroundColor: 'transparent',
                            series: {
                                0: {targetAxisIndex: 0},
                                1: {targetAxisIndex: 1}
                            },
                            vAxes: {
                                0: {title: 'Nombre de réservations'},
                                1: {title: 'Revenu (DT)'}
                            }
                        };
                        
                        const chart = new google.visualization.LineChart(document.getElementById('evolutionChart'));
                        chart.draw(data, options);
                    }
                </script>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 20px;
                        background-color: transparent;
                    }
                    .stats-card {
                        background: white;
                        border-radius: 8px;
                        padding: 20px;
                        margin-bottom: 20px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .stats-table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    .stats-table td {
                        padding: 8px;
                        border-bottom: 1px solid #eee;
                    }
                    .stats-table tr:last-child td {
                        border-bottom: none;
                    }
                    .chart-container {
                        background: white;
                        border-radius: 8px;
                        padding: 20px;
                        margin-bottom: 20px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        height: 300px;
                    }
                </style>
            </head>
            <body>
                <div id="generalStats"></div>
                <div id="categoriesChart" class="chart-container"></div>
                <div id="distributionChart" class="chart-container"></div>
                <div id="evolutionChart" class="chart-container"></div>
            </body>
            </html>
        """;
    }

    @FXML
    public void refreshData() {
        try {
            // Show loading indicator
            showInfoAlert("Rafraîchissement", "Mise à jour des données en cours...");
            
            // Reload all data
            loadStatistiques();
            
            // Update last update time
            updateLastUpdateLabel();
            
            showInfoAlert("Succès", "Les données ont été rafraîchies avec succès!");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de rafraîchir les données: " + e.getMessage());
        }
    }
    
    @FXML
    public void exportData() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les statistiques");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("EutopiaVents_Statistiques_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf");
            
            File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
            if (file != null) {
                exportStatisticsToPdf(file);
                showInfoAlert("Succès", "Les statistiques ont été exportées avec succès!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'exporter les données: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void exportStatisticsToPdf(File file) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            
            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Rapport de Statistiques EutopiaVents", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Add date
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY);
            Paragraph date = new Paragraph("Généré le: " + lastUpdateLabel.getText(), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);
            
            // Add summary statistics
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph summaryTitle = new Paragraph("Résumé des Statistiques", sectionFont);
            summaryTitle.setSpacingAfter(10);
            document.add(summaryTitle);
            
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);
            
            // Add header row
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            PdfPCell headerCell = new PdfPCell(new Phrase("Métrique", headerFont));
            headerCell.setBackgroundColor(new BaseColor(80, 50, 80)); // #503250
            headerCell.setPadding(8);
            summaryTable.addCell(headerCell);
            
            headerCell = new PdfPCell(new Phrase("Valeur", headerFont));
            headerCell.setBackgroundColor(new BaseColor(80, 50, 80)); // #503250
            headerCell.setPadding(8);
            summaryTable.addCell(headerCell);
            
            // Add data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            
            PdfPCell cell = new PdfPCell(new Phrase("Total des Salles", cellFont));
            cell.setPadding(8);
            summaryTable.addCell(cell);
            
            cell = new PdfPCell(new Phrase(totalSallesLabel.getText(), cellFont));
            cell.setPadding(8);
            summaryTable.addCell(cell);
            
            cell = new PdfPCell(new Phrase("Total des Réservations", cellFont));
            cell.setPadding(8);
            summaryTable.addCell(cell);
            
            cell = new PdfPCell(new Phrase(totalReservationsLabel.getText(), cellFont));
            cell.setPadding(8);
            summaryTable.addCell(cell);
            
            cell = new PdfPCell(new Phrase("Revenu Total", cellFont));
            cell.setPadding(8);
            summaryTable.addCell(cell);
            
            cell = new PdfPCell(new Phrase(revenuTotalLabel.getText(), cellFont));
            cell.setPadding(8);
            summaryTable.addCell(cell);
            
            document.add(summaryTable);
            
            // Add footer
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Ce rapport a été généré automatiquement par l'application EutopiaVents.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le PDF: " + e.getMessage());
        }
    }
}