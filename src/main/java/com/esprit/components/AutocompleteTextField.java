package com.esprit.components;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import com.esprit.services.AutocompleteService;
import java.util.List;

public class AutocompleteTextField extends VBox {
    private final TextField textField;
    private final FlowPane suggestionsPane;
    private final AutocompleteService autocompleteService;

    private static final String BUBBLE_STYLE_NORMAL = "-fx-background-color: #f0eae4; " +
            "-fx-padding: 5 10; " +
            "-fx-background-radius: 15; " +
            "-fx-cursor: hand; " +
            "-fx-font-family: 'Segoe UI'; " +
            "-fx-font-size: 12px; " +
            "-fx-border-color: #ddd; " +
            "-fx-border-radius: 15;";

    private static final String BUBBLE_STYLE_HOVER = "-fx-background-color: #e0dad4; " +
            "-fx-padding: 5 10; " +
            "-fx-background-radius: 15; " +
            "-fx-cursor: hand; " +
            "-fx-font-family: 'Segoe UI'; " +
            "-fx-font-size: 12px; " +
            "-fx-border-color: #bbb; " +
            "-fx-border-radius: 15;";

    public AutocompleteTextField() {
        super(5);
        this.autocompleteService = new AutocompleteService();
        
        textField = new TextField();
        textField.setPromptText("Write a comment...");
        
        suggestionsPane = new FlowPane(5, 5);
        suggestionsPane.setPrefHeight(35);
        suggestionsPane.setMinHeight(35);
        suggestionsPane.setMaxWidth(USE_PREF_SIZE);
        
        getChildren().addAll(textField, suggestionsPane);
        
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                clearSuggestions();
            }
        });
    }
    
    public void initializeSuggestions() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("\n--- Text Changed ---");
            System.out.println("Old value: '" + oldValue + "'");
            System.out.println("New value: '" + newValue + "'");
            
            if (newValue == null || newValue.isEmpty()) {
                System.out.println("Text is empty or null, clearing suggestions");
                clearSuggestions();
                return;
            }
            
            String[] words = newValue.split("\\s");
            System.out.println("Split words: " + String.join(", ", words));
            
            if (words.length > 0) {
                String lastWord = words[words.length - 1];
                System.out.println("Last word: '" + lastWord + "'");
                
                if (!lastWord.isEmpty()) {
                    System.out.println("Getting suggestions for: '" + lastWord + "'");
                    List<String> suggestions = autocompleteService.getSuggestions(lastWord);
                    System.out.println("Received suggestions: " + suggestions);
                    updateSuggestions(suggestions, lastWord);
                } else {
                    System.out.println("Last word is empty, clearing suggestions");
                    clearSuggestions();
                }
            }
        });

        suggestionsPane.setOnMouseClicked(event -> {
            System.out.println("Suggestions pane clicked");
            event.consume();
        });
    }
    
    private void updateSuggestions(List<String> suggestions, String currentWord) {
        System.out.println("\n--- Updating Suggestions ---");
        System.out.println("Current word: '" + currentWord + "'");
        System.out.println("Suggestions: " + suggestions);
        
        suggestionsPane.getChildren().clear();
        
        if (suggestions.isEmpty()) {
            System.out.println("No suggestions to show");
            return;
        }
        
        for (String suggestion : suggestions) {
            Label bubble = new Label(suggestion);
            bubble.setStyle(BUBBLE_STYLE_NORMAL);
            
            // Make the label consume mouse events
            bubble.setMouseTransparent(false);
            bubble.setPickOnBounds(true);
            
            // Debug mouse events
            bubble.setOnMousePressed(e -> {
                System.out.println("Mouse PRESSED on: " + suggestion);
                e.consume();
            });
            
            bubble.setOnMouseReleased(e -> {
                System.out.println("Mouse RELEASED on: " + suggestion);
                e.consume();
            });
            
            bubble.setOnMouseEntered(e -> {
                System.out.println("Mouse entered: " + suggestion);
                bubble.setStyle(BUBBLE_STYLE_HOVER);
                e.consume();
            });
            
            bubble.setOnMouseExited(e -> {
                System.out.println("Mouse exited: " + suggestion);
                bubble.setStyle(BUBBLE_STYLE_NORMAL);
                e.consume();
            });
            
            bubble.setOnMouseClicked(e -> {
                System.out.println("Mouse CLICKED on: " + suggestion);
                String text = textField.getText();
                System.out.println("Current text: '" + text + "'");
                System.out.println("Current word to replace: '" + currentWord + "'");
                
                // Replace the current word with the suggestion
                int lastIndex = text.lastIndexOf(currentWord);
                String newText;
                if (lastIndex != -1) {
                    newText = text.substring(0, lastIndex) + suggestion + " ";
                } else {
                    newText = text + suggestion + " ";
                }
                
                System.out.println("Setting new text: '" + newText + "'");
                Platform.runLater(() -> {
                    textField.setText(newText);
                    textField.positionCaret(newText.length());
                    textField.requestFocus();
                });
                
                clearSuggestions();
                e.consume();
            });
            
            suggestionsPane.getChildren().add(bubble);
            System.out.println("Added suggestion bubble for: " + suggestion);
        }
        
        // Make sure the suggestions pane itself doesn't block events
        suggestionsPane.setMouseTransparent(false);
        suggestionsPane.setPickOnBounds(false);
    }
    
    private void clearSuggestions() {
        System.out.println("Clearing suggestions");
        suggestionsPane.getChildren().clear();
    }

    public void applyTextFieldStyle(String style) {
        textField.setStyle(style);
    }

    public String getText() {
        return textField.getText();
    }
    
    public void setText(String text) {
        textField.setText(text);
    }
    
    public void setPromptText(String text) {
        textField.setPromptText(text);
    }

    public void clear() {
        textField.clear();
        clearSuggestions();
    }
    
    public void setOnAction(EventHandler<ActionEvent> value) {
        textField.setOnAction(value);
    }

    public EventHandler<ActionEvent> getOnAction() {
        return textField.getOnAction();
    }

    public javafx.beans.property.StringProperty textProperty() {
        return textField.textProperty();
    }
} 