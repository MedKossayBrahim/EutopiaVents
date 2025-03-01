package com.esprit.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        try {
            String processedText = text;
            for (Pattern pattern : PROFANITY_PATTERNS) {
                processedText = pattern.matcher(processedText)
                        .replaceAll(match -> "*".repeat(match.group().length()));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + processedText.replace(" ", "%20")))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String result = response.body();
            return result.substring(result.indexOf(":\"") + 2, result.length() - 2);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            String processedText = text;
            for (Pattern pattern : PROFANITY_PATTERNS) {
                processedText = pattern.matcher(processedText)
                        .replaceAll(match -> "*".repeat(match.group().length()));
            }
            return processedText;
        }
    }

    public static boolean containsProfanity(String text) {
        for (Pattern pattern : PROFANITY_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }

        String filtered = filter(text);
        return !filtered.equals(text);
    }
} 