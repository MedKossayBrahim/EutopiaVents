package com.esprit.controllers;

import com.esprit.models.Lieu;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.maps.render.MapsRenderClient;
import com.azure.maps.render.MapsRenderClientBuilder;
import com.azure.maps.render.models.MapTileOptions;
import com.azure.maps.render.models.TileIndex;
import com.azure.maps.render.models.TilesetId;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.control.Button;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import javafx.scene.control.Alert;

public class LieuDetailsController {
    @FXML private Label nomLabel;
    @FXML private Label adresseLabel;
    @FXML private Label villeLabel;
    @FXML private Label codePostalLabel;
    @FXML private Label capaciteLabel;
    @FXML private Label prixLabel;
    @FXML private Label categorieLabel;
    @FXML private ImageView imageView;
    @FXML
    private Canvas mapCanvas;
    @FXML private Button directionsButton;
    @FXML
    private WebView hiddenWebView;
    private double userLatitude = 0;
    private double userLongitude = 0;
    private List<double[]> routePoints = new ArrayList<>();
    private boolean isShowingDirections = false;
    private boolean isTracking = false;
    private javafx.animation.AnimationTimer locationUpdateTimer;

    private final String SUBSCRIPTION_KEY = "BwF2cQrllsSHb42pApInB5LrLbWXzK6abgXyKtPxmT83BLxrbCZSJQQJ99BBACYeBjF8Ca2JAAAgAZMP22qF"; // Replace with your actual key
    private MapsRenderClient renderClient;
    private ExecutorService executorService;

    // Map state
    private double centerLatitude = 36.8065; // Default to Tunisia
    private double centerLongitude = 10.1815;
    private int zoomLevel = 15; // Closer zoom for better location view
    private double dragStartX;
    private double dragStartY;


    // Constants
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 18;
    private Lieu lieu;

    private double markerLatitude;
    private double markerLongitude;
    private String locationName;

    private double[] mapBounds;

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
        if (lieu != null) {
            loadData();
        }
    }

    private void loadData() {
        if (lieu == null) return;

        nomLabel.setText(lieu.getNom());
        adresseLabel.setText(lieu.getAdresse());
        villeLabel.setText(lieu.getVille());
        codePostalLabel.setText(lieu.getCodePostal());
        capaciteLabel.setText(String.valueOf(lieu.getCapacite()));
        prixLabel.setText(String.format("%.2f DT", lieu.getPrix()));
        categorieLabel.setText(lieu.getCategorie().getNom());
        
        // Geocode the address and initialize map
        String fullAddress = String.format("%s, %s %s, Tunisia", 
            lieu.getAdresse(), 
            lieu.getVille(), 
            lieu.getCodePostal());
            
        geocodeAddress(fullAddress);

        try {
            String imageUrl = lieu.getImage();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = "/Images/defaultPlace.png";
            }
            Image image = new Image(imageUrl);
            if (image.isError()) {
                throw new Exception("Error loading image");
            }
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/Images/defaultPlace.png")));
        }
    }

    private void geocodeAddress(String address) {
        try {
            // Create URL for Azure Maps geocoding service
            String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
            String geocodeUrl = String.format(
                "https://atlas.microsoft.com/search/address/json?subscription-key=%s&api-version=1.0&query=%s&limit=1",
                SUBSCRIPTION_KEY,
                encodedAddress
            );

            // Make async HTTP request
            CompletableFuture.supplyAsync(() -> {
                try {
                    URL url = new URL(geocodeUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }, executorService).thenAccept(response -> {
                if (response != null) {
                    try {
                        // Parse JSON response
                        org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
                        org.json.JSONArray results = jsonResponse.getJSONArray("results");
                        
                        if (results.length() > 0) {
                            org.json.JSONObject result = results.getJSONObject(0);
                            org.json.JSONObject position = result.getJSONObject("position");
                            
                            // Get coordinates
                            markerLatitude = position.getDouble("lat");
                            markerLongitude = position.getDouble("lon");
                            centerLatitude = markerLatitude;
                            centerLongitude = markerLongitude;
                            locationName = lieu.getNom();
                            
                            // Update map on JavaFX thread
                            javafx.application.Platform.runLater(this::renderMap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Fallback to default coordinates if geocoding fails
                        markerLatitude = centerLatitude = 36.8065; // Default to Tunisia
                        markerLongitude = centerLongitude = 10.1815;
                        locationName = lieu.getNom();
                        javafx.application.Platform.runLater(this::renderMap);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default coordinates
            markerLatitude = centerLatitude = 36.8065; // Default to Tunisia
            markerLongitude = centerLongitude = 10.1815;
            locationName = lieu.getNom();
            renderMap();
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) nomLabel.getScene().getWindow()).close();
    }


    //essaie
    @FXML
    public void initialize() {
        // Initialize executor service
        executorService = Executors.newFixedThreadPool(4);

        // Initialize Azure Maps client
        renderClient = new MapsRenderClientBuilder()
                .credential(new AzureKeyCredential(SUBSCRIPTION_KEY))
                .buildClient();

        if (mapCanvas != null) {
            setupCanvas();
            setupEventHandlers();
            
            // Initialize default map bounds (will be updated when location is set)
            mapBounds = new double[]{
                centerLatitude + 0.1,  // North
                centerLongitude - 0.1, // West
                centerLatitude - 0.1,  // South
                centerLongitude + 0.1  // East
            };
            
            initializeLocationServices();
        } else {
            System.err.println("Warning: mapCanvas is null during initialization");
        }

        // Style the directions button
        if (directionsButton != null) {
            directionsButton.setOnMouseEntered(e -> 
                directionsButton.setStyle(directionsButton.getStyle().replace("#f5945c", "#ff7f50"))
            );
            directionsButton.setOnMouseExited(e -> 
                directionsButton.setStyle(directionsButton.getStyle().replace("#ff7f50", "#f5945c"))
            );
        }

        // Ensure WebView is properly initialized
        if (hiddenWebView != null) {
            hiddenWebView.setVisible(false);
            hiddenWebView.setPrefSize(1, 1);
        } else {
            System.err.println("Warning: hiddenWebView is null");
        }
    }

    private void setupCanvas() {
        if (mapCanvas != null) {
            mapCanvas.setWidth(400); // Increased width for better visibility
            mapCanvas.setHeight(300); // Increased height for better visibility
            mapCanvas.setStyle("-fx-background-color: #f0eae4; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
            System.out.println("Canvas size set to: " + mapCanvas.getWidth() + "x" + mapCanvas.getHeight());
        }
    }

    private void setupEventHandlers() {
        mapCanvas.setOnMousePressed(this::handleMousePressed);
        mapCanvas.setOnMouseDragged(this::handleMouseDragged);
        mapCanvas.setOnScroll(this::handleScroll);
    }

    private void handleMousePressed(MouseEvent event) {
        dragStartX = event.getX();
        dragStartY = event.getY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getX() - dragStartX;
        double deltaY = event.getY() - dragStartY;

        // Calculate movement in coordinates
        double pixelsPerLongitude = TILE_SIZE * Math.pow(2, zoomLevel) / 360.0;
        double pixelsPerLatitude = TILE_SIZE * Math.pow(2, zoomLevel) / 170.1022;

        centerLongitude -= deltaX / pixelsPerLongitude;
        centerLatitude += deltaY / pixelsPerLatitude;

        dragStartX = event.getX();
        dragStartY = event.getY();

        renderMap();
    }

    private void handleScroll(ScrollEvent event) {
        int oldZoom = zoomLevel;

        if (event.getDeltaY() > 0 && zoomLevel < MAX_ZOOM) {
            zoomLevel++;
        } else if (event.getDeltaY() < 0 && zoomLevel > MIN_ZOOM) {
            zoomLevel--;
        }

        if (oldZoom != zoomLevel) {
            System.out.println("Zoom level changed to: " + zoomLevel);
            renderMap();
        }
    }

    private void getRouteFromAzure(double fromLat, double fromLon, double toLat, double toLon) {
        try {
            String routeUrl = String.format(
                "https://atlas.microsoft.com/route/directions/json?subscription-key=%s&api-version=1.0&query=%f,%f:%f,%f&routeType=shortest&computeTravelTimeFor=all&traffic=true",
                SUBSCRIPTION_KEY,
                fromLat, fromLon,
                toLat, toLon
            );

            CompletableFuture.supplyAsync(() -> {
                try {
                    URL url = new URL(routeUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }, executorService).thenAccept(response -> {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray routes = jsonResponse.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONArray legs = route.getJSONArray("legs");
                            JSONArray points = legs.getJSONObject(0).getJSONArray("points");
                            
                            routePoints.clear();
                            for (int i = 0; i < points.length(); i++) {
                                JSONObject point = points.getJSONObject(i);
                                routePoints.add(new double[]{
                                    point.getDouble("latitude"),
                                    point.getDouble("longitude")
                                });
                            }
                            
                            javafx.application.Platform.runLater(this::renderMap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLocationTracking() {
        if (locationUpdateTimer != null) {
            locationUpdateTimer.stop();
        }

        locationUpdateTimer = new javafx.animation.AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                // Update every 2 seconds
                if (now - lastUpdate >= 2_000_000_000L) {
                    if (hiddenWebView != null) {
                        try {
                            hiddenWebView.getEngine().executeScript("getLocation()");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    lastUpdate = now;
                }
            }
        };
        locationUpdateTimer.start();
        isTracking = true;
    }

    private void stopLocationTracking() {
        if (locationUpdateTimer != null) {
            locationUpdateTimer.stop();
        }
        isTracking = false;
    }

    @FXML
    private void handleGetDirections() {
        System.out.println("handleGetDirections called");
        try {
            if (!isShowingDirections) {
                isShowingDirections = true;
                
                if (hiddenWebView == null) {
                    System.err.println("Error: hiddenWebView is null");
                    showError("WebView initialization error");
                    return;
                }

                System.out.println("Initializing location request...");
                
                // Set default location for testing (Tunisia)
                userLatitude = 36.8065;
                userLongitude = 10.1815;
                
                // Get route using default location
                System.out.println("Getting route from: " + userLatitude + "," + userLongitude + 
                                 " to: " + markerLatitude + "," + markerLongitude);
                getRouteFromAzure(userLatitude, userLongitude, markerLatitude, markerLongitude);
                
                // Update map view
                updateMapBounds();
                renderMap();
                
                // Start tracking after initial route is shown
                startLocationTracking();
                
                // Try to get actual location
                try {
                    System.out.println("Requesting actual location...");
                    hiddenWebView.getEngine().executeScript("console.log('Calling getLocation()'); getLocation();");
                } catch (Exception e) {
                    System.err.println("Error executing location script: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Stopping directions mode");
                isShowingDirections = false;
                stopLocationTracking();
                // Reset to original view
                centerLatitude = markerLatitude;
                centerLongitude = markerLongitude;
                zoomLevel = 15;
                routePoints.clear();
                renderMap();
            }
        } catch (Exception e) {
            System.err.println("Error in handleGetDirections: " + e.getMessage());
            e.printStackTrace();
            showError("Error getting directions: " + e.getMessage());
            isShowingDirections = false;
            stopLocationTracking();
        }
    }

    public class LocationCallback {
        public void onLocationReceived(double latitude, double longitude) {
            System.out.println("Location received: " + latitude + ", " + longitude);
            javafx.application.Platform.runLater(() -> {
                userLatitude = latitude;
                userLongitude = longitude;
                
                // Get new route based on current location
                getRouteFromAzure(userLatitude, userLongitude, markerLatitude, markerLongitude);
                
                // Update map view
                updateMapBounds();
                renderMap();
            });
        }

        public void onLocationError(String error) {
            System.err.println("Location error: " + error);
            javafx.application.Platform.runLater(() -> {
                showError("Could not get your location. Please ensure location services are enabled.\nError: " + error);
                isShowingDirections = false;
                stopLocationTracking();
            });
        }
    }

    private void updateMapBounds() {
        // Calculate the bounds to include both points with padding
        double latMin = Math.min(markerLatitude, userLatitude);
        double latMax = Math.max(markerLatitude, userLatitude);
        double lonMin = Math.min(markerLongitude, userLongitude);
        double lonMax = Math.max(markerLongitude, userLongitude);
        
        // Add padding (30% of the range)
        double latPadding = Math.max((latMax - latMin) * 0.3, 0.002);
        double lonPadding = Math.max((lonMax - lonMin) * 0.3, 0.002);
        
        mapBounds = new double[]{
            latMax + latPadding,  // North
            lonMin - lonPadding,  // West
            latMin - latPadding,  // South
            lonMax + lonPadding   // East
        };
        
        // Update center point
        centerLatitude = (latMax + latMin) / 2;
        centerLongitude = (lonMax + lonMin) / 2;
        
        // Calculate appropriate zoom level
        double latSpan = mapBounds[0] - mapBounds[2];
        double lonSpan = mapBounds[3] - mapBounds[1];
        
        // Calculate zoom level based on the smaller span to ensure both points are visible
        double zoomLat = Math.log(170.1022 / latSpan) / Math.log(2);
        double zoomLon = Math.log(360 / lonSpan) / Math.log(2);
        zoomLevel = (int) Math.min(zoomLat, zoomLon);
        
        // Ensure zoom level is within bounds and subtract 1 to give extra space
        zoomLevel = Math.max(Math.min(zoomLevel - 1, MAX_ZOOM), MIN_ZOOM);
    }

    private void drawMarker(GraphicsContext gc, double x, double y, javafx.scene.paint.Color color) {
        double size = 10;
        gc.setFill(color);
        gc.fillOval(x - size/2, y - size/2, size, size);
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(x - size/2, y - size/2, size, size);
    }

    private double[] convertToPixelCoordinates(double lat, double lon) {
        // Calculate world coordinates
        double worldSize = Math.pow(2, zoomLevel) * TILE_SIZE;
        
        // Convert longitude to x position
        double x = (lon + 180) / 360 * worldSize;
        
        // Convert latitude to y position
        double latRad = Math.toRadians(lat);
        double mercN = Math.log(Math.tan((Math.PI/4) + (latRad/2)));
        double y = (worldSize/2) - (worldSize * mercN / (2 * Math.PI));
        
        // Adjust for current view
        double centerWorldX = ((centerLongitude + 180) / 360) * worldSize;
        double centerLatRad = Math.toRadians(centerLatitude);
        double centerMercN = Math.log(Math.tan((Math.PI/4) + (centerLatRad/2)));
        double centerWorldY = (worldSize/2) - (worldSize * centerMercN / (2 * Math.PI));
        
        // Convert to screen coordinates
        double screenX = (x - centerWorldX) + mapCanvas.getWidth() / 2;
        double screenY = (y - centerWorldY) + mapCanvas.getHeight() / 2;
        
        return new double[]{screenX, screenY};
    }

    private void drawDirectionsAndMarkers() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        
        // Draw the route if we have points
        if (!routePoints.isEmpty()) {
            gc.setStroke(javafx.scene.paint.Color.web("#f5945c"));
            gc.setLineWidth(3);
            gc.setGlobalAlpha(0.7);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineJoin(StrokeLineJoin.ROUND);

            boolean first = true;
            for (double[] point : routePoints) {
                double[] pixel = convertToPixelCoordinates(point[0], point[1]);
                if (first) {
                    gc.beginPath();
                    gc.moveTo(pixel[0], pixel[1]);
                    first = false;
                } else {
                    gc.lineTo(pixel[0], pixel[1]);
                }
            }
            gc.stroke();
        }

        // Draw current location marker
        double[] fromPixel = convertToPixelCoordinates(userLatitude, userLongitude);
        drawMarker(gc, fromPixel[0], fromPixel[1], javafx.scene.paint.Color.BLUE);

        // Draw destination marker
        double[] toPixel = convertToPixelCoordinates(markerLatitude, markerLongitude);
        drawLocationMarker(gc, toPixel[0], toPixel[1]);
    }

    private void drawLocationMarker(GraphicsContext gc, double pixelX, double pixelY) {
        // Draw marker shadow
        gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.2));
        gc.fillOval(pixelX - 12, pixelY - 8, 24, 16);
        
        // Draw marker pin base (larger circle)
        gc.setFill(javafx.scene.paint.Color.rgb(245, 148, 92, 1));
        gc.fillOval(pixelX - 10, pixelY - 25, 20, 20);
        
        // Draw marker pin point (triangle)
        double[] xPoints = {pixelX - 10, pixelX + 10, pixelX};
        double[] yPoints = {pixelY - 15, pixelY - 15, pixelY + 5};
        gc.setFill(javafx.scene.paint.Color.rgb(245, 148, 92, 1));
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Draw white border around the marker
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(pixelX - 10, pixelY - 25, 20, 20);
        gc.strokePolygon(xPoints, yPoints, 3);

        // Draw location name
        if (locationName != null && !locationName.isEmpty()) {
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
            double textWidth = gc.getFont().getSize() * locationName.length() * 0.6;
            gc.fillRoundRect(pixelX - textWidth/2 - 10, pixelY - 50, textWidth + 20, 30, 10, 10);
            
            gc.setFill(javafx.scene.paint.Color.rgb(245, 148, 92, 1));
            gc.fillText(locationName, pixelX - textWidth/2, pixelY - 30);
        }
    }

    private void renderMap() {
        if (mapCanvas == null) return;
        
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        // Set background color
        gc.setFill(javafx.scene.paint.Color.rgb(240, 234, 228));
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        // Calculate number of tiles needed
        int numTilesX = (int) Math.ceil(mapCanvas.getWidth() / TILE_SIZE) + 1;
        int numTilesY = (int) Math.ceil(mapCanvas.getHeight() / TILE_SIZE) + 1;

        // Calculate center tile coordinates
        double worldSize = Math.pow(2, zoomLevel);
        int centerTileX = (int) ((centerLongitude + 180) / 360 * worldSize);
        int centerTileY = (int) ((1 - Math.log(Math.tan(Math.toRadians(centerLatitude)) + 1 / Math.cos(Math.toRadians(centerLatitude))) / Math.PI) / 2 * worldSize);

        // Load tiles (markers and directions will be drawn after tiles are loaded)
        loadTiles(gc, numTilesX, numTilesY, centerTileX, centerTileY, worldSize);
    }

    private void loadTiles(GraphicsContext gc, int numTilesX, int numTilesY,
                           int centerTileX, int centerTileY, double worldSize) {
        // Create a set to track loaded tiles
        java.util.Set<String> loadedTiles = new java.util.HashSet<>();
        java.util.concurrent.atomic.AtomicInteger pendingTiles = new java.util.concurrent.atomic.AtomicInteger(0);
        
        for (int x = -1; x < numTilesX; x++) {
            for (int y = -1; y < numTilesY; y++) {
                final int currentX = x;
                final int currentY = y;
                final int tileX = (centerTileX - numTilesX / 2 + x + (int) worldSize) % (int) worldSize;
                final int tileY = (centerTileY - numTilesY / 2 + y + (int) worldSize) % (int) worldSize;

                // Validate tile coordinates
                int maxTileIndex = (int) Math.pow(2, zoomLevel) - 1;
                if (tileX < 0 || tileX > maxTileIndex || tileY < 0 || tileY > maxTileIndex) {
                    continue;
                }

                // Create unique tile identifier
                String tileKey = tileX + ":" + tileY + ":" + zoomLevel;
                if (loadedTiles.contains(tileKey)) {
                    continue;
                }
                loadedTiles.add(tileKey);
                
                pendingTiles.incrementAndGet();
                loadSingleTile(gc, currentX, currentY, tileX, tileY, numTilesX, numTilesY, () -> {
                    if (pendingTiles.decrementAndGet() == 0) {
                        // All tiles loaded, now draw the markers and directions
                        javafx.application.Platform.runLater(() -> {
                            if (isShowingDirections) {
                                drawDirectionsAndMarkers();
                            } else {
                                double[] markerPixel = convertToPixelCoordinates(markerLatitude, markerLongitude);
                                drawLocationMarker(gc, markerPixel[0], markerPixel[1]);
                            }
                        });
                    }
                });
            }
        }
        
        // If no tiles were loaded, draw immediately
        if (pendingTiles.get() == 0) {
            if (isShowingDirections) {
                drawDirectionsAndMarkers();
            } else {
                double[] markerPixel = convertToPixelCoordinates(markerLatitude, markerLongitude);
                drawLocationMarker(gc, markerPixel[0], markerPixel[1]);
            }
        }
    }

    private void loadSingleTile(GraphicsContext gc, int currentX, int currentY,
                                int tileX, int tileY, int numTilesX, int numTilesY,
                                Runnable onTileLoaded) {
        CompletableFuture.supplyAsync(() -> {
            try {
                TileIndex tileIndex = new TileIndex()
                        .setZ(zoomLevel)
                        .setX(tileX)
                        .setY(tileY);

                MapTileOptions tileOptions = new MapTileOptions()
                        .setTilesetId(TilesetId.MICROSOFT_BASE_ROAD)
                        .setTileIndex(tileIndex);

                BinaryData tileData = renderClient.getMapTile(tileOptions);
                return tileData.toBytes();
            } catch (Exception e) {
                return null;
            }
        }, executorService).thenAccept(tileData -> {
            if (tileData == null) {
                onTileLoaded.run();
                return;
            }

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(tileData);
                Image tileImage = new Image(bis, TILE_SIZE, TILE_SIZE, true, true);

                if (tileImage.isError()) {
                    onTileLoaded.run();
                    return;
                }

                // Calculate tile position
                double pixelX = (currentX - numTilesX / 2.0) * TILE_SIZE + mapCanvas.getWidth() / 2.0;
                double pixelY = (currentY - numTilesY / 2.0) * TILE_SIZE + mapCanvas.getHeight() / 2.0;

                javafx.application.Platform.runLater(() -> {
                    gc.drawImage(tileImage, pixelX, pixelY);
                    onTileLoaded.run();
                });
            } catch (Exception e) {
                onTileLoaded.run();
            }
        });
    }

    private void drawMarker(GraphicsContext gc) {
        // Draw location marker
        double pixelX = mapCanvas.getWidth() / 2;
        double pixelY = mapCanvas.getHeight() / 2;

        // Draw marker shadow
        gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.2));
        gc.fillOval(pixelX - 12, pixelY - 8, 24, 16);
        
        // Draw marker pin base (larger circle)
        gc.setFill(javafx.scene.paint.Color.rgb(245, 148, 92, 1));
        gc.fillOval(pixelX - 10, pixelY - 25, 20, 20);
        
        // Draw marker pin point (triangle)
        double[] xPoints = {pixelX - 10, pixelX + 10, pixelX};
        double[] yPoints = {pixelY - 15, pixelY - 15, pixelY + 5};
        gc.setFill(javafx.scene.paint.Color.rgb(245, 148, 92, 1));
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Draw white border around the marker
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(pixelX - 10, pixelY - 25, 20, 20);
        gc.strokePolygon(xPoints, yPoints, 3);

        // Draw location name
        if (locationName != null && !locationName.isEmpty()) {
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
            double textWidth = gc.getFont().getSize() * locationName.length() * 0.6;
            gc.fillRoundRect(pixelX - textWidth/2 - 10, pixelY - 50, textWidth + 20, 30, 10, 10);
            
            gc.setFill(javafx.scene.paint.Color.rgb(245, 148, 92, 1));
            gc.fillText(locationName, pixelX - textWidth/2, pixelY - 30);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initializeLocationServices() {
        if (hiddenWebView == null) {
            System.err.println("WebView is not initialized");
            return;
        }

        System.out.println("Initializing location services...");
        hiddenWebView.getEngine().setJavaScriptEnabled(true);

        // Create HTML content with geolocation script
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <script>
                console.log('Location script loaded');
                function getLocation() {
                    console.log('getLocation called');
                    try {
                        if (!navigator.geolocation) {
                            console.error('Geolocation not supported');
                            window.locationCallback.onLocationError("Geolocation is not supported");
                            return;
                        }
                        
                        console.log('Requesting position...');
                        navigator.geolocation.getCurrentPosition(
                            function(position) {
                                console.log('Position received:', position.coords.latitude, position.coords.longitude);
                                window.locationCallback.onLocationReceived(
                                    position.coords.latitude,
                                    position.coords.longitude
                                );
                            },
                            function(error) {
                                console.error('Position error:', error.code, error.message);
                                window.locationCallback.onLocationError(error.message);
                            },
                            {
                                enableHighAccuracy: true,
                                timeout: 10000,
                                maximumAge: 0
                            }
                        );
                    } catch (e) {
                        console.error('Error in getLocation:', e);
                        window.locationCallback.onLocationError(e.toString());
                    }
                }
                </script>
            </head>
            <body>
                <div>Location Services Ready</div>
            </body>
            </html>
            """;

        System.out.println("Loading HTML content...");
        hiddenWebView.getEngine().loadContent(htmlContent);

        hiddenWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebView state changed: " + newState);
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    System.out.println("Setting up location callback...");
                    JSObject window = (JSObject) hiddenWebView.getEngine().executeScript("window");
                    window.setMember("locationCallback", new LocationCallback());
                    
                    // Test the setup
                    hiddenWebView.getEngine().executeScript(
                        "console.log('Location services initialized');"
                    );
                    System.out.println("Location services setup complete");
                } catch (Exception e) {
                    System.err.println("Error in location services setup: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }

    public void cleanup() {
        stopLocationTracking();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}