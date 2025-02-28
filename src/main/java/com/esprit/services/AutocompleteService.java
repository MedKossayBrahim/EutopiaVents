package com.esprit.services;

import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AutocompleteService {
    private final Map<String, List<String>> contextualSuggestions;
    private final HttpClient client;
    private static final String DATAMUSE_API_URL = "https://api.datamuse.com/sug?s=";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "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";
    private List<String> recentContext;

    public AutocompleteService() {
        this.contextualSuggestions = new HashMap<>();
        this.client = HttpClient.newHttpClient();
        this.recentContext = new ArrayList<>();
        initializeSuggestions();
    }

    private void initializeSuggestions() {
        // Keep some basic contextual suggestions for immediate response
        addSuggestions("", Arrays.asList("I", "The", "This", "Your", "That", "How", "Why", "What"));
        addSuggestions("I", Arrays.asList("think", "believe", "agree", "disagree", "like", "love"));
        // ... add more basic contextual suggestions ...
    }

    private void addSuggestions(String word, List<String> suggestions) {
        contextualSuggestions.put(word.toLowerCase(), suggestions);
    }

    public List<String> getSuggestions(String currentWord) {
        System.out.println("\n=== Getting suggestions for: '" + currentWord + "' ===");
        
        List<String> combinedSuggestions = new ArrayList<>();
        
        // Get suggestions from both APIs concurrently
        CompletableFuture<List<String>> openAiFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<String> suggestions = getOpenAISuggestions(currentWord);
                System.out.println("OpenAI suggestions: " + suggestions);
                return suggestions;
            } catch (Exception e) {
                System.out.println("OpenAI API error: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<String>> datamuseFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<String> suggestions = getDatamuseSuggestions(currentWord);
                System.out.println("Datamuse suggestions: " + suggestions);
                return suggestions;
            } catch (Exception e) {
                System.out.println("Datamuse API error: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        // Wait for both APIs to respond (or timeout)
        try {
            List<String> openAiSuggestions = openAiFuture.get();
            List<String> datamuseSuggestions = datamuseFuture.get();
            
            // Combine suggestions with priority
            // Take first 5 from OpenAI (more contextual)
            combinedSuggestions.addAll(openAiSuggestions.stream().limit(5).collect(Collectors.toList()));
            
            // Add unique suggestions from Datamuse
            datamuseSuggestions.stream()
                .filter(s -> !combinedSuggestions.contains(s))
                .limit(5)
                .forEach(combinedSuggestions::add);
            
            System.out.println("Combined API suggestions: " + combinedSuggestions);
        } catch (Exception e) {
            System.out.println("Error combining API results: " + e.getMessage());
        }

        // Add contextual suggestions if we don't have enough
        if (combinedSuggestions.size() < 10) {
            List<String> contextual = contextualSuggestions.getOrDefault(
                currentWord.toLowerCase(), 
                getDefaultSuggestions(currentWord)
            );
            
            contextual.stream()
                .filter(s -> !combinedSuggestions.contains(s))
                .limit(10 - combinedSuggestions.size())
                .forEach(combinedSuggestions::add);
        }

        // Update recent context
        updateRecentContext(currentWord);

        // Filter and return final suggestions
        List<String> finalSuggestions = combinedSuggestions.stream()
                .distinct()
                .filter(s -> s.toLowerCase().startsWith(currentWord.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());
                
        System.out.println("Final suggestions: " + finalSuggestions);
        return finalSuggestions;
    }

    private List<String> getOpenAISuggestions(String currentWord) throws Exception {
        String context = String.join(" ", recentContext);
        
        JSONObject requestBody = new JSONObject()
            .put("model", "gpt-3.5-turbo")
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a helpful assistant providing word suggestions. " +
                                  "Respond with only 5 natural next words, separated by commas."))
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", String.format(
                        "Given the context '%s' and current word '%s', suggest 5 natural next words. " +
                        "Respond with only the words, separated by commas.",
                        context, currentWord))))
            .put("temperature", 0.7)
            .put("max_tokens", 50);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OPENAI_API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + OPENAI_API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject jsonResponse = new JSONObject(response.body());
        
        String completionText = jsonResponse.getJSONArray("choices")
                                         .getJSONObject(0)
                                         .getJSONObject("message")
                                         .getString("content")
                                         .trim();
                                         
        return Arrays.asList(completionText.split(",\\s*"));
    }

    private void updateRecentContext(String word) {
        recentContext.add(word);
        if (recentContext.size() > 5) {
            recentContext.remove(0);
        }
    }

    private List<String> getDatamuseSuggestions(String currentWord) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(DATAMUSE_API_URL + currentWord))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray jsonArray = new JSONArray(response.body());
        
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject suggestion = jsonArray.getJSONObject(i);
            suggestions.add(suggestion.getString("word"));
        }
        
        return suggestions;
    }

    private List<String> getDefaultSuggestions(String prefix) {
        return Arrays.asList(
            "the", "this", "that", "with", "would", "could",
            "have", "help", "how", "here", "hope",
            "in", "is", "it", "important", "interesting",
            "make", "more", "much", "many",
            "need", "new", "now",
            "think", "that", "there",
            "want", "will", "with",
            "you", "your"
        );
    }
} 