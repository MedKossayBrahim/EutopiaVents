package com.esprit.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ProfanityFilter {
    private static final String API_URL = "https://www.purgomalum.com/service/json?text=";
    private static final HttpClient client = HttpClient.newHttpClient();

    // Encoded profanity list with additional variations
    private static final String ENCODED_LIST = "bmlrLG5pa29tZWssMzFzYmEsemViaSx3YWJuYSxtaWJvdW4sdGE3YW4sOW83Yix6YWJib3VyLG5heWVrLHRib24sMzFzYmVrLDMxc2JldCx6ZWJuYSx6ZWJvayxaZWJ5LDlhaGJhLDlhN2JhLDlhN2JldCw5YWhiZXQsbWFueW91ayxtbmF5ZWssbWFueWFrLG1hbnlvdWthLHdlbGQgbDlhaGJhLHdsZWQgbDloYWIsd2VsZCBsazdlYix3YWxkIGw5YWhiYSx3bGQgbDlhaGJhLHdlbGQgZWwga2FoYmEsa2FoYmEsa2FoYmV0LGthaGJhLGthaGViLDNhcnMsMzFyZXMsMzFyc2EsMzFyYXMsdGFoYW4sdGE3YW5hLHRhaGFuLHRhaGFuYSxobWFyYSxobWFyLGJoaW0sYmFncmEsN2F5YXdhbixoYXlhd2FuLDdtYXIsN21hcmEsa29zc2F5LDNhc2JhLGFzYmEsYWFzYmEsM2FhemJhLDNhemJhLDNhemViYSwzYXNiZWssMzFzYmVrLGFzemJhLGFzemViYSwzYXNiYXRlayxhc2JhdGVrLDNhemJhdGVr";

    // Common letter substitutions (encoded) - Updated to handle more variations
    private static final String ENCODED_SUBS = "YSxbYUA04OHDoMOhw6LDo8Onw6VdLGUsW2Uz82nDqcOow6rDq10saSxbaTEhw63DrMOuw69dLG8sW28ww7PDssOuw7XDtl0sdSxbdcO6w7nDu8O8XSx5LFt5wqVdLHMsW3MkNXp6XSxsLFtsMS1dLDMsW2UzYV0sNyxbN2hdLDksWzlxXSxrLFtrOF0sdyxbd9iOXSxoLFtoN10seixbejNd";

    private static final List<String> CUSTOM_PROFANITY = decodeList(ENCODED_LIST);
    private static final Map<String, String> SUBSTITUTIONS = decodeSubstitutions(ENCODED_SUBS);

    private static List<String> decodeList(String encoded) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encoded);
            String decoded = new String(decodedBytes);
            return Arrays.asList(decoded.split(","));
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static Map<String, String> decodeSubstitutions(String encoded) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encoded);
            String decoded = new String(decodedBytes);
            Map<String, String> subs = new HashMap<>();
            String[] pairs = decoded.split(",");
            for (int i = 0; i < pairs.length; i += 2) {
                subs.put(pairs[i], pairs[i + 1]);
            }
            return subs;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private static String createRegexPattern(String word) {
        String pattern = word.toLowerCase();
        for (Map.Entry<String, String> entry : SUBSTITUTIONS.entrySet()) {
            pattern = pattern.replace(entry.getKey(), entry.getValue());
        }
        return "\\b" + pattern.chars()
                .mapToObj(ch -> String.valueOf((char)ch))
                .collect(Collectors.joining("[\\W_]{0,2}")) + "\\b";
    }

    private static final List<Pattern> PROFANITY_PATTERNS = CUSTOM_PROFANITY.stream()
            .map(word -> Pattern.compile(createRegexPattern(word), Pattern.CASE_INSENSITIVE))
            .collect(Collectors.toList());

    public static String filter(String text) {
        return filter(text, false);
    }
    
    public static String filter(String text, boolean isAIGenerated) {
        // Skip profanity check for AI-generated content
        if (isAIGenerated) {
            return text;
        }
        
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String urlString = API_URL + encodedText;
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String response = reader.lines().collect(Collectors.joining());
                
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                String filteredText = jsonResponse.get("result").getAsString();
                
                if (!text.equals(filteredText)) {
                    StringBuilder details = new StringBuilder();
                    details.append("Inappropriate content found:\n");
                    
                    String[] originalWords = text.split("\\s+");
                    String[] filteredWords = filteredText.split("\\s+");
                    
                    for (int i = 0; i < Math.min(originalWords.length, filteredWords.length); i++) {
                        if (!originalWords[i].equals(filteredWords[i])) {
                            String errorMsg = String.format("Inappropriate word found at position %d: '%s'", 
                                i + 1, originalWords[i]);
                            throw new IllegalArgumentException(errorMsg);
                        }
                    }
                    
                    // If we get here and texts are different, throw generic error
                    throw new IllegalArgumentException("Inappropriate content detected");
                }
                
                return filteredText;
            }
        } catch (IllegalArgumentException e) {
            // Rethrow IllegalArgumentException to be handled by caller
            throw e;
        } catch (Exception e) {
            System.err.println("Error in profanity filter: " + e.getMessage());
            throw new IllegalArgumentException("Error checking content for inappropriate language");
        }
    }
    
    public static boolean containsProfanity(String text) {
        return containsProfanity(text, false);
    }
    
    public static boolean containsProfanity(String text, boolean isAIGenerated) {
        if (isAIGenerated) {
            return false;
        }
        
        try {
            filter(text, false); // This will throw an exception if profanity is found
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        } catch (Exception e) {
            System.err.println("Error checking profanity: " + e.getMessage());
            return true; // Err on the side of caution
        }
    }
    
    public static String filterModifiedAIContent(String originalAIText, String modifiedText) {
        if (originalAIText.equals(modifiedText)) {
            return modifiedText;
        }
        
        try {
            String filtered = filter(modifiedText, false);
            if (!modifiedText.equals(filtered)) {
                throw new IllegalArgumentException("Inappropriate content found in modified AI text");
            }
            return modifiedText;
        } catch (IllegalArgumentException e) {
            throw e; // Rethrow to be handled by caller
        } catch (Exception e) {
            System.err.println("Error checking modified AI content: " + e.getMessage());
            throw new IllegalArgumentException("Error validating modified content");
        }
    }
} 