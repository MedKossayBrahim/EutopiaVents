package com.esprit.controllers;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import com.esprit.services.StatistiquesServiceImpl;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class StatistiquesController implements Initializable {

    @FXML
    private WebView webView;
    private StatistiquesServiceImpl statistiquesService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            statistiquesService = new StatistiquesServiceImpl();
            loadStatistiques();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStatistiques() {
        String statistiquesJson = statistiquesService.getStatistiquesJson();
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <script>
                    google.charts.load('current', {'packages':['corechart', 'bar']});
                    google.charts.setOnLoadCallback(drawCharts);
                    
                    const statistiques = %s;
                    
                    function drawCharts() {
                        drawGeneralStats();
                        drawCategoriesChart();
                        drawDistributionChart();
                        drawEvolutionChart();
                    }
                    
                    function drawGeneralStats() {
                        const stats = statistiques.general;
                        const html = `
                            <h3>Statistiques Générales</h3>
                            <table class="stats-table">
                                <tr><th>Métrique</th><th>Valeur</th></tr>
                                <tr><td>Nombre total de lieux</td><td>${stats.nombreTotal}</td></tr>
                                <tr><td>Capacité minimale</td><td>${stats.capaciteMin}</td></tr>
                                <tr><td>Capacité maximale</td><td>${stats.capaciteMax}</td></tr>
                                <tr><td>Capacité moyenne</td><td>${stats.capaciteMoyenne}</td></tr>
                                <tr><td>Capacité médiane</td><td>${stats.capaciteMediane}</td></tr>
                                <tr><td>Capacité totale</td><td>${stats.capaciteTotale}</td></tr>
                            </table>`;
                        document.getElementById('generalStats').innerHTML = html;
                    }
                    
                    function drawCategoriesChart() {
                        const data = new google.visualization.DataTable();
                        data.addColumn('string', 'Catégorie');
                        data.addColumn('number', 'Nombre de lieux');
                        data.addRows(statistiques.categories.map(cat => [cat.categorie, cat.nombre]));
                        
                        const options = {
                            title: 'Répartition par catégorie',
                            pieHole: 0.4,
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
                            bars: 'vertical'
                        };
                        
                        const chart = new google.visualization.ColumnChart(document.getElementById('distributionChart'));
                        chart.draw(data, options);
                    }
                    
                    function drawEvolutionChart() {
                        const data = new google.visualization.DataTable();
                        data.addColumn('number', 'Index');
                        data.addColumn('number', 'Capacité');
                        data.addColumn({type: 'string', role: 'tooltip'});
                        data.addRows(statistiques.evolution.map(e => [
                            e.index, 
                            e.capacite, 
                            `${e.nom}: ${e.capacite} places`
                        ]));
                        
                        const options = {
                            title: 'Évolution des capacités',
                            legend: { position: 'none' },
                            curveType: 'function'
                        };
                        
                        const chart = new google.visualization.LineChart(document.getElementById('evolutionChart'));
                        chart.draw(data, options);
                    }
                </script>
                <style>
                    .stats-container { margin: 20px; font-family: Arial, sans-serif; }
                    .stats-table { 
                        width: 100%%; 
                        border-collapse: collapse; 
                        margin: 15px 0; 
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                    }
                    .stats-table th, .stats-table td { 
                        padding: 12px; 
                        border: 1px solid #ddd; 
                        text-align: left;
                    }
                    .stats-table th { 
                        background-color: #f8f9fa; 
                        color: #495057;
                    }
                    .stats-table tr:nth-child(even) { background-color: #f8f9fa; }
                    .stats-table tr:hover { background-color: #f2f2f2; }
                    .chart-container { 
                        margin: 20px 0; 
                        height: 400px; 
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                        padding: 20px;
                        border-radius: 8px;
                    }
                </style>
            </head>
            <body>
                <div class="stats-container">
                    <div id="generalStats"></div>
                    <div id="categoriesChart" class="chart-container"></div>
                    <div id="distributionChart" class="chart-container"></div>
                    <div id="evolutionChart" class="chart-container"></div>
                </div>
            </body>
            </html>
            """.formatted(statistiquesJson);

        webView.getEngine().loadContent(html);
    }
}