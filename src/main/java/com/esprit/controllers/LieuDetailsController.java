package com.esprit.controllers;

import com.esprit.models.Lieu;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.maps.render.MapsRenderClient;
import com.azure.maps.render.MapsRenderClientBuilder;
import com.azure.maps.render.models.MapTileOptions;
import com.azure.maps.render.models.TileIndex;
import com.azure.maps.render.models.TilesetId;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    private final String SUBSCRIPTION_KEY = "BwF2cQrllsSHb42pApInB5LrLbWXzK6abgXyKtPxmT83BLxrbCZSJQQJ99BBACYeBjF8Ca2JAAAgAZMP22qF"; // Replace with your actual key
    private MapsRenderClient renderClient;
    private ExecutorService executorService;

    // Map state
    private double centerLatitude = 47.6062; // Seattle
    private double centerLongitude = -122.3321;
    private int zoomLevel = 12;
    private double dragStartX;
    private double dragStartY;


    // Constants
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 18;
    private Lieu lieu;

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
        loadData();
    }

    private void loadData() {
        nomLabel.setText(lieu.getNom());
        adresseLabel.setText(lieu.getAdresse());
        villeLabel.setText(lieu.getVille());
        codePostalLabel.setText(lieu.getCodePostal());
        capaciteLabel.setText(String.valueOf(lieu.getCapacite()));
        prixLabel.setText(String.format("%.2f DT", lieu.getPrix()));
        categorieLabel.setText(lieu.getCategorie().getNom());
        initialize();

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

    @FXML
    private void handleClose() {
        ((Stage) nomLabel.getScene().getWindow()).close();
    }


    //essaie
    public void initialize() {
        initializeMapComponents();

    }

    private void initializeMapComponents() {
        // Initialize executor service
        executorService = Executors.newFixedThreadPool(4);

        // Initialize Azure Maps client
        renderClient = new MapsRenderClientBuilder()
                .credential(new AzureKeyCredential(SUBSCRIPTION_KEY))
                .buildClient();

        // Setup canvas and event handlers
        setupCanvas();
        setupEventHandlers();

        // Initial render
        renderMap();
    }

    private void setupCanvas() {
        mapCanvas.setWidth(250); // Set appropriate width
        mapCanvas.setHeight(250); // Set appropriate height
        System.out.println("Canvas size: " + mapCanvas.getWidth() + "x" + mapCanvas.getHeight());
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

    private void renderMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight()); // Clear the canvas

        // Calculate number of tiles needed
        int numTilesX = (int) Math.ceil(mapCanvas.getWidth() / TILE_SIZE) + 1;
        int numTilesY = (int) Math.ceil(mapCanvas.getHeight() / TILE_SIZE) + 1;

        // Calculate center tile coordinates
        double worldSize = Math.pow(2, zoomLevel);
        int centerTileX = (int) ((centerLongitude + 180) / 360 * worldSize);
        int centerTileY = (int) ((1 - Math.log(Math.tan(Math.toRadians(centerLatitude)) + 1 / Math.cos(Math.toRadians(centerLatitude))) / Math.PI) / 2 * worldSize);

        System.out.println("Rendering map at zoom " + zoomLevel + ", center: " + centerLatitude + ", " + centerLongitude);
        System.out.println("Center tile: " + centerTileX + ", " + centerTileY);

        // Load tiles
        loadTiles(gc, numTilesX, numTilesY, centerTileX, centerTileY, worldSize);
    }

    private void loadTiles(GraphicsContext gc, int numTilesX, int numTilesY,
                           int centerTileX, int centerTileY, double worldSize) {
        for (int x = -1; x < numTilesX; x++) {
            for (int y = -1; y < numTilesY; y++) {
                final int currentX = x;
                final int currentY = y;
                final int tileX = (centerTileX - numTilesX / 2 + x + (int) worldSize) % (int) worldSize;
                final int tileY = (centerTileY - numTilesY / 2 + y + (int) worldSize) % (int) worldSize;

                // Validate tile coordinates
                int maxTileIndex = (int) Math.pow(2, zoomLevel) - 1;
                if (tileX < 0 || tileX > maxTileIndex || tileY < 0 || tileY > maxTileIndex) {
                    System.out.println("Skipping invalid tile: " + tileX + ", " + tileY + " at zoom " + zoomLevel);
                    continue;
                }

                System.out.println("Loading tile: x=" + tileX + ", y=" + tileY + ", zoom=" + zoomLevel);
                loadSingleTile(gc, currentX, currentY, tileX, tileY, numTilesX, numTilesY);
            }
        }
    }

    private void loadSingleTile(GraphicsContext gc, int currentX, int currentY,
                                int tileX, int tileY, int numTilesX, int numTilesY) {
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
                byte[] tileBytes = tileData.toBytes();

                if (tileBytes.length < 8) {
                    throw new Exception("Invalid image data received - data too short");
                }

                return tileBytes;
            } catch (Exception e) {
                System.err.println("Error fetching tile: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }, executorService).thenAccept(tileData -> {
            if (tileData == null) return;

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(tileData);
                Image tileImage = new Image(bis, TILE_SIZE, TILE_SIZE, true, true);

                if (tileImage.isError()) {
                    System.err.println("Image error: " + tileImage.getException().getMessage());
                    return;
                }

                // Corrected tile positioning formula
                double pixelX = (currentX - numTilesX / 2) * TILE_SIZE + mapCanvas.getWidth() / 2;
                double pixelY = (currentY - numTilesY / 2) * TILE_SIZE + mapCanvas.getHeight() / 2;

                System.out.println("Drawing tile at: " + pixelX + ", " + pixelY);
                javafx.application.Platform.runLater(() -> {
                    gc.drawImage(tileImage, pixelX, pixelY);
                });
            } catch (Exception e) {
                System.err.println("Error loading tile image: " + e.getMessage());
            }
        });
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}