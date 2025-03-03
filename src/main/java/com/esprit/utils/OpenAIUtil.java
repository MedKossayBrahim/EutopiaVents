package com.esprit.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class OpenAIUtil {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static int analyzeMaterielReviews(List<String> reviews) {
        String allReviews = String.join("\n", reviews);

        // Calculer d'abord le score basé sur les mots-clés
        int keywordScore = calculateKeywordScore(allReviews);

        // Ensuite obtenir le score de l'API
        int apiScore = getAPIScore(allReviews);

        // Combiner les deux scores avec une pondération
        // 60% pour l'API, 40% pour l'analyse des mots-clés
        double finalScore = (apiScore * 0.5) + (keywordScore * 0.5);

        // Arrondir au nombre entier le plus proche
        return Math.min(5, Math.max(1, (int) Math.round(finalScore)));
    }

    private static int calculateKeywordScore(String text) {
        text = text.toLowerCase().trim();
        double score = 3.0;

        // Vérifier d'abord les mots critiques pour une réduction immédiate du score
        if (containsCriticalNegative(text)) {
            score = 1.0;  // Score de base plus bas pour les mots critiques
        }

        double positiveScore = getWeightedPositiveScore(text);
        double negativeScore = getWeightedNegativeScore(text);

        // Si des mots négatifs sont trouvés, augmenter leur impact
        if (negativeScore > 0) {
            negativeScore *= 1.5; // Augmenter l'impact des mots négatifs de 50%
        }

        // Si aucun mot trouvé, retourner le score de base
        if (positiveScore + negativeScore == 0) {
            return (int) score;
        }

        // Calculer le ratio avec plus de poids pour les mots négatifs
        double ratio = positiveScore / (positiveScore + negativeScore);

        // Seuils plus stricts
        if (ratio >= 0.9) score = 5.0;
        else if (ratio >= 0.75) score = 4.0;
        else if (ratio >= 0.5) score = 3.0;
        else if (ratio >= 0.25) score = 2.0;
        else score = 1.0;

        // Réductions supplémentaires basées sur des mots spécifiques
        if (text.contains("faible") || text.contains("mal") || text.contains("mauvais")) {
            score = Math.max(1.0, score - 2.0); // Réduction plus importante
        }

        return (int) Math.round(score);
    }

    private static double getWeightedPositiveScore(String text) {
        double score = 0;

        // Mots très positifs (poids 2.0)
        String[] veryPositive = {
                "excellent", "parfait", "exceptionnel", "extraordinaire", "incroyable",
                "fantastique", "remarquable", "meilleur"
        };

        // Mots positifs (poids 1.0)
        String[] positive = {
                "excellent", "parfait", "super", "génial", "très bien", "satisfait",
                "recommande", "qualité", "efficace", "pratique", "facile", "agréable",
                "fiable", "formidable", "merveilleux", "rapide", "top", "convivial",
                "fonctionnel", "utile", "fluide", "intuitif", "performant", "incroyable",
                "exceptionnel", "remarquable", "brillant", "impressionnant", "nickel",
                "content", "satisfaisant", "pratique", "bien", "bon", "super",
                "bien", "bon", "satisfait", "content", "pratique", "efficace",
                "utile", "agréable", "recommande", "qualité"
        };

        // Mots légèrement positifs (poids 0.5)
        String[] slightlyPositive = {
                "correct", "ok", "acceptable", "passable", "suffisant"
        };

        for (String word : veryPositive) {
            if (text.contains(word)) score += 2.0;
        }

        for (String word : positive) {
            if (text.contains(word)) score += 1.0;
        }

        for (String word : slightlyPositive) {
            if (text.contains(word)) score += 0.5;
        }

        return score;
    }

    private static double getWeightedNegativeScore(String text) {
        double score = 0;

        // Mots très négatifs (poids 3.0) - augmenté de 2.0 à 3.0
        String[] veryNegative = {
                "mauvais", "décevant", "problème", "défaut", "panne", "difficile",
                "compliqué", "cassé", "inutile", "cher", "déçu", "insatisfait",
                "horrible", "médiocre", "inadmissible", "lamentable", "catastrophique",
                "nul", "impossible", "inefficace", "lent", "inacceptable", "bof",
                "bug", "planté", "dysfonctionnement", "peu fiable", "instable",
                "défectueux", "trop cher", "frustrant", "dommage", "problématique",
                "pas bon", "mauvaise", "pas bien", "pas terrible"
        };

        // Mots négatifs (poids 2.0) - augmenté de 1.0 à 2.0
        String[] negative = {
                "problème", "défaut", "panne", "difficile", "compliqué",
                "cassé", "inutile", "déçu", "insatisfait", "peu fiable"
        };

        // Mots légèrement négatifs (poids 1.0) - augmenté de 0.5 à 1.0
        String[] slightlyNegative = {
                "moyen", "bof", "pas terrible", "peu", "lent", "médiocre"
        };

        for (String word : veryNegative) {
            if (text.contains(word)) score += 3.0;
        }

        for (String word : negative) {
            if (text.contains(word)) score += 2.0;
        }

        for (String word : slightlyNegative) {
            if (text.contains(word)) score += 1.0;
        }

        // Vérifier les combinaisons de mots négatifs
        if (text.contains("pas") || text.contains("peu")) {
            String[] positiveWords = {"bon", "bien", "efficace", "utile", "pratique"};
            for (String word : positiveWords) {
                if (text.contains(word)) score += 2.0; // Augmenter le score négatif pour "pas bon", "peu efficace", etc.
            }
        }

        return score;
    }

    private static boolean containsCriticalNegative(String text) {
        String[] criticalWords = {
                "mauvais", "décevant", "problème", "défaut", "panne", "difficile",
                "compliqué", "cassé", "inutile", "cher", "déçu", "insatisfait",
                "horrible", "médiocre", "inadmissible", "lamentable", "catastrophique",
                "nul", "impossible", "inefficace", "lent", "inacceptable", "bof",
                "bug", "planté", "dysfonctionnement", "peu fiable", "instable",
                "défectueux", "trop cher", "frustrant", "dommage", "problématique",
                "pas bon", "mauvaise", "pas bien", "pas terrible"
        };

        for (String word : criticalWords) {
            if (text.contains(word)) return true;
        }
        return false;
    }

    private static int getAPIScore(String review) {
        String requestBody = String.format("""
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "system",
                        "content": "Vous êtes un expert en analyse de sentiment spécialisé dans l'évaluation des avis sur le matériel informatique et électronique. Évaluez l'avis selon ces critères stricts:\\n\\n5: Excellente expérience, satisfaction totale, aucun problème\\n4: Bonne expérience avec des points mineurs\\n3: Expérience moyenne ou mitigée\\n2: Problèmes significatifs mais quelques points positifs\\n1: Expérience très négative, problèmes majeurs\\n\\nConsidérez:\\n- Fonctionnalité\\n- Fiabilité\\n- Rapport qualité/prix\\n- Problèmes techniques\\n- Satisfaction globale"
                    },
                    {
                        "role": "user",
                        "content": "Évaluez cet avis et donnez une note précise de 1 à 5. Répondez uniquement avec le chiffre.\\n\\nAvis: %s"
                    }
                ],
                "temperature": 0.1,
                "max_tokens": 10
            }
            """, review);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseRating(response.body());
        } catch (Exception e) {
            e.printStackTrace();
            return 3;
        }
    }

    private static int parseRating(String response) {
        try {
            String cleanResponse = response.replaceAll("[^1-5]", "");
            if (!cleanResponse.isEmpty()) {
                return Integer.parseInt(cleanResponse.substring(0, 1));
            }
        } catch (Exception ignored) {}
        return 3;
    }

}