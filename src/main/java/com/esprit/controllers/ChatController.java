package com.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.esprit.models.*;

public class ChatController {
    @FXML private VBox chatArea;
    @FXML private TextField userInput;
    @FXML private ScrollPane scrollPane;
    private ChatBot chatBot;
    
    @FXML
    public void initialize() {
        chatBot = new ChatBot();
        Platform.runLater(() -> {
            addBotMessage("Hi! I can help you find events. Try asking me about:\n" +
                         "- Music events\n" +
                         "- Sports events\n" +
                         "- Art exhibitions\n" +
                         "- Events this weekend\n" +
                         "- Free events");
        });
    }
    
    @FXML
    private void handleSend() {
        String message = userInput.getText().trim();
        if (!message.isEmpty()) {
            addUserMessage(message);
            String response = chatBot.processUserInput(message);
            addBotMessage(response);
            userInput.clear();
        }
    }
    
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 5, 5, 50));
        
        Text text = new Text(message);
        VBox textContainer = new VBox(text);
        textContainer.setStyle("-fx-background-color: #FFE5EC; " +
                             "-fx-background-radius: 15 15 0 15; " +
                             "-fx-padding: 10; " +
                             "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        text.setStyle("-fx-fill: #333333;");
        text.setWrappingWidth(200);
        
        messageBox.getChildren().add(textContainer);
        chatArea.getChildren().add(messageBox);
        
        scrollToBottom();
    }
    
    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 5));
        
        Text text = new Text(message);
        VBox textContainer = new VBox(text);
        textContainer.setStyle("-fx-background-color: white; " +
                             "-fx-background-radius: 15 15 15 0; " +
                             "-fx-padding: 10; " +
                             "-fx-border-color: #E8E8E8; " +
                             "-fx-border-radius: 15 15 15 0; " +
                             "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        text.setStyle("-fx-fill: #333333;");
        text.setWrappingWidth(200);
        
        messageBox.getChildren().add(textContainer);
        chatArea.getChildren().add(messageBox);
        
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        if (scrollPane != null) {
            Platform.runLater(() -> {
                chatArea.layout();
                scrollPane.setVvalue(1.0);
            });
        }
    }
} 