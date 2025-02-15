package com.esprit.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProfanityFilter {
    private static final String API_URL = "https://www.purgomalum.com/service/json?text=";
    private static final HttpClient client = HttpClient.newHttpClient();
    
    // Base profanity words
    private static final List<String> CUSTOM_PROFANITY = Arrays.asList(
        "nik", "nikomek", "3asba", "zebi", "wabna", "miboun", 
        "ta7an", "9o7b", "zabbour"
    );
    
    // Common letter substitutions
    private static final Map<String, String> SUBSTITUTIONS = new HashMap<>() {{
        put("a", "[a@4àáâãäå]");
        put("e", "[e3éèêë]");
        put("i", "[i1!íìîï]");
        put("o", "[o0óòôõö]");
        put("u", "[uúùûü]");
        put("y", "[y¥]");
        put("s", "[s$5]");
        put("l", "[l1!]");
        put("3", "[3e]");
        put("7", "[7h]");
        put("9", "[9q]");
    }};

    private static String createRegexPattern(String word) {
        // Convert word to regex pattern that matches letter substitutions
        String pattern = word.toLowerCase();
        for (Map.Entry<String, String> entry : SUBSTITUTIONS.entrySet()) {
            pattern = pattern.replace(entry.getKey(), entry.getValue());
        }
        // Add word boundaries and limit the number of allowed symbols between letters
        return "\\b" + pattern.chars()
                     .mapToObj(ch -> String.valueOf((char)ch))
                     .collect(Collectors.joining("[\\W_]{0,2}")) + "\\b"; // Max 2 symbols between letters
    }

    private static final List<Pattern> PROFANITY_PATTERNS = CUSTOM_PROFANITY.stream()
        .map(word -> Pattern.compile(createRegexPattern(word), Pattern.CASE_INSENSITIVE))
        .collect(Collectors.toList());

    public static String filter(String text) {
        try {
            // First check for regex patterns
            String processedText = text;
            for (Pattern pattern : PROFANITY_PATTERNS) {
                processedText = pattern.matcher(processedText)
                                     .replaceAll(match -> "*".repeat(match.group().length()));
            }
            
            // Then use API as backup
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + processedText.replace(" ", "%20")))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            String result = response.body();
            return result.substring(result.indexOf(":\"") + 2, result.length() - 2);
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // If API fails, at least we still have the regex filtering
            String processedText = text;
            for (Pattern pattern : PROFANITY_PATTERNS) {
                processedText = pattern.matcher(processedText)
                                     .replaceAll(match -> "*".repeat(match.group().length()));
            }
            return processedText;
        }
    }

    public static boolean containsProfanity(String text) {
        // Check regex patterns first
        for (Pattern pattern : PROFANITY_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        
        // Then check API result
        String filtered = filter(text);
        return !filtered.equals(text);
    }
} 