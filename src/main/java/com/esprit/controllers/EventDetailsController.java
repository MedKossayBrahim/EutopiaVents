package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.models.Reservations;
import com.esprit.models.User;
import com.esprit.services.ReservationsService;
import com.esprit.services.EvenementService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.application.Platform;

public class EventDetailsController {

    @FXML
    private Label titreLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label lieuLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private ImageView eventImageView;
    @FXML
    private Button reserverButton;
    @FXML
    private VBox weatherContainer;
    @FXML
    private Label weatherTitleLabel;
    @FXML
    private HBox weatherInfoBox;
    @FXML
    private ImageView weatherIconView;
    @FXML
    private Label temperatureLabel;
    @FXML
    private Label weatherDescLabel;

    private EvenementService evenementService = new EvenementService();
    private ReservationsService reservationsService = new ReservationsService();
    private int evenementId;
    
    // Clé API gratuite pour OpenWeatherMap
    private static final String WEATHER_API_KEY = "ce1e83b98e4a7818e10458c90c240ba2";

    public EventDetailsController() throws SQLException {
    }

    public void afficherDetails(int evenementId) {
        this.evenementId = evenementId;
        Evenement evenement = evenementService.rechercherParId(evenementId);

        if (evenement != null) {
            titreLabel.setText(evenement.getTitre());
            descriptionLabel.setText(evenement.getDescription());
            String lieuAffiche = evenement.getLieuId() > 0 ? evenement.getLieuNom() : evenement.getLieu_proprietaire();
            lieuLabel.setText(lieuAffiche);

            String dateDebut = evenement.getDateDebut().split(" ")[0];
            String dateFin = evenement.getDateFin().split(" ")[0];
            String heureDebut = evenement.getDateDebut().split(" ")[1];
            String heureFin = evenement.getDateFin().split(" ")[1];

            dateLabel.setText("Date : " + dateDebut + " à " + dateFin);
            timeLabel.setText("Horaires : " + heureDebut + " - " + heureFin);

            if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
                try {
                    Image image = new Image(evenement.getImage());
                    eventImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                }
            }

            reserverButton.setText(String.format("Réserver maintenant (%.2f TND)", evenement.getPrix()));

            // Vérifier si un utilisateur est connecté
            User currentUser = Eutopia.getCurrentUser();
            if (currentUser == null) {
                reserverButton.setDisable(true);
                reserverButton.setText("Connectez-vous pour réserver");
            }
            
            // Charger les informations météo
            loadWeatherInfo(lieuAffiche, dateDebut);
        } else {
            System.err.println("Événement non trouvé avec l'ID : " + evenementId);
        }
    }
    
    private void loadWeatherInfo(String lieu, String dateStr) {
        // Afficher les données par défaut en attendant
        displayFallbackWeather();
        
        // Nettoyer le lieu et utiliser Tunis par défaut si nécessaire
        String cleanLieu = "Tunis";
        if (lieu != null && !lieu.trim().isEmpty()) {
            cleanLieu = lieu.trim().replace("\"", "").replace(" ", "+");
        }
        
        final String finalLieu = cleanLieu;
        final String API_KEY = "ce1e83b98e4a7818e10458c90c240ba2";
        
        // Lancer une requête en arrière-plan
        new Thread(() -> {
            try {
                // Obtenir les coordonnées
                String geocodingUrl = "http://api.openweathermap.org/geo/1.0/direct?q=" + 
                                     finalLieu + ",TN&limit=1&appid=" + API_KEY;
                
                String response = getJsonResponse(geocodingUrl);
                JSONArray jsonArray = new JSONArray(response);
                
                if (jsonArray.length() == 0) return;
                
                JSONObject location = jsonArray.getJSONObject(0);
                double lat = location.getDouble("lat");
                double lon = location.getDouble("lon");
                
                // Obtenir la date de l'événement
                LocalDate eventDate = LocalDate.parse(dateStr);
                LocalDate today = LocalDate.now();
                
                // Choisir l'URL appropriée selon la date
                String weatherUrl;
                if (eventDate.equals(today)) {
                    // Météo actuelle pour aujourd'hui
                    weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + 
                               "&lon=" + lon + "&units=metric&lang=fr&appid=" + API_KEY;
                } else if (eventDate.isAfter(today) && eventDate.isBefore(today.plusDays(5))) {
                    // Prévisions pour les 5 prochains jours
                    weatherUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + 
                               "&lon=" + lon + "&units=metric&lang=fr&appid=" + API_KEY;
                } else {
                    // Date trop éloignée, afficher un message
                    Platform.runLater(() -> {
                        weatherDescLabel.setText("Prévisions non disponibles pour cette date");
                        weatherContainer.setVisible(true);
                    });
                    return;
                }
                
                String weatherResponse = getJsonResponse(weatherUrl);
                JSONObject weatherData = new JSONObject(weatherResponse);
                
                // Mettre à jour l'interface selon le type de réponse
                if (eventDate.equals(today)) {
                    // Météo actuelle
                    double temp = weatherData.getJSONObject("main").getDouble("temp");
                    String description = weatherData.getJSONArray("weather")
                                                  .getJSONObject(0).getString("description");
                    String icon = weatherData.getJSONArray("weather")
                                           .getJSONObject(0).getString("icon");
                    
                    updateWeatherUI(temp, description, icon);
                } else {
                    // Prévisions
                    JSONArray list = weatherData.getJSONArray("list");
                    // Trouver la prévision la plus proche de la date de l'événement
                    JSONObject forecast = findClosestForecast(list, eventDate);
                    if (forecast != null) {
                        double temp = forecast.getJSONObject("main").getDouble("temp");
                        String description = forecast.getJSONArray("weather")
                                                   .getJSONObject(0).getString("description");
                        String icon = forecast.getJSONArray("weather")
                                            .getJSONObject(0).getString("icon");
                        
                        updateWeatherUI(temp, description, icon);
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Erreur météo: " + e.getMessage());
            }
        }).start();
    }

    private JSONObject findClosestForecast(JSONArray list, LocalDate eventDate) {
        JSONObject closest = null;
        long minDiff = Long.MAX_VALUE;
        
        for (int i = 0; i < list.length(); i++) {
            JSONObject forecast = list.getJSONObject(i);
            String dtTxt = forecast.getString("dt_txt");
            LocalDate forecastDate = LocalDate.parse(dtTxt.split(" ")[0]);
            
            long diff = Math.abs(forecastDate.toEpochDay() - eventDate.toEpochDay());
            if (diff < minDiff) {
                minDiff = diff;
                closest = forecast;
            }
        }
        
        return closest;
    }

    private void updateWeatherUI(double temp, String description, String icon) {
        Platform.runLater(() -> {
            temperatureLabel.setText(Math.round(temp) + "°C");
            weatherDescLabel.setText(description);
            weatherIconView.setImage(new Image("http://openweathermap.org/img/wn/" + icon + "@2x.png"));
            weatherContainer.setVisible(true);
        });
    }

    private void displayFallbackWeather() {
        Platform.runLater(() -> {
            temperatureLabel.setText("22°C");
            weatherDescLabel.setText("Ensoleillé");
            weatherIconView.setImage(new Image("https://openweathermap.org/img/wn/01d@2x.png"));
            weatherContainer.setVisible(true);
        });
    }

    private String getJsonResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();
            
            return response.toString();
        } catch (Exception e) {
            return "[]";  // Retourner un tableau vide en cas d'erreur
        }
    }

    @FXML
    private void reserverEvenement() {
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null) {
            // Afficher une alerte si l'utilisateur n'est pas connecté
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Connexion requise");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez vous connecter pour effectuer une réservation.");
            alert.showAndWait();
            return;
        }

        // Créer une nouvelle réservation avec l'ID de l'utilisateur connecté
        Evenement evenement = evenementService.rechercherParId(evenementId);
        Reservations reservation = new Reservations(0, evenement.getId(), currentUser.getUserID(), 1, evenement.getPrix(), "en_attente");
        reservationsService.ajouter(reservation);

        try {
            // Rediriger vers le panier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Panier.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Panier");
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) reserverButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la redirection vers le panier.");
            alert.showAndWait();
        }
    }
}
