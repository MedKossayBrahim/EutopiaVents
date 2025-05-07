package com.esprit.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.List;

public class OpenAIUtil {
    
    public static int analyzeMaterielReviews(List<String> reviews) {
        String allReviews = String.join("\n", reviews);
        return analyzeSentiment(allReviews);
    }

    public static int analyzeSentiment(String text) {
        try {
            System.out.println("🔍 Démarrage analyse pour: " + text);
            
            // Analyse par mots-clés uniquement
            int rating = calculateKeywordScore(text);
            
            System.out.println("✅ Analyse réussie - Score final: " + rating);
            return rating;

        } catch (Exception e) {
            System.err.println("💥 Erreur critique: " + e.getMessage());
            return 3; // Note neutre par défaut en cas d'erreur
        }
    }

    private static int calculateKeywordScore(String text) {
        text = text.toLowerCase().trim();
        double score = 3.0;

        // Gestion des cas particuliers
        if (Pattern.compile("\\bpas mal\\b").matcher(text).find()) {
            return 4;
        }
        if (Pattern.compile("\\bpas mauvais\\b").matcher(text).find()) {
            return 4;
        }
        if (Pattern.compile("\\bpas terrible\\b").matcher(text).find()) {
            return 2;
        }

        if (containsCriticalNegative(text)) {
            return 1;
        }

        double positiveScore = getWeightedKeywordScore(text, getPositiveWords());
        double negativeScore = getWeightedKeywordScore(text, getNegativeWords());
        double neutralScore = getWeightedKeywordScore(text, getNeutralWords());

        System.out.println("Scores calculés - Positif: " + positiveScore + 
                         ", Négatif: " + negativeScore + 
                         ", Neutre: " + neutralScore);

        // Si le score neutre est significatif, limiter le score maximum
        if (neutralScore > 0) {
            // Un commentaire neutre ne peut pas dépasser 3 étoiles
            double maxScore = 3;
            
            // Si c'est un commentaire purement neutre
            if (positiveScore == 0 && negativeScore == 0) {
                return (int) maxScore;
            }
            
            // Sinon, atténuer les autres scores
            positiveScore *= 0.7;
            negativeScore *= 0.7;
        }

        if (negativeScore > 0) {
            negativeScore *= 1.5;
        }

        if (positiveScore + negativeScore + neutralScore == 0) {
            return (int) score;
        }

        double ratio = positiveScore / (positiveScore + negativeScore + neutralScore);

        // Ajustement des seuils
        if (ratio >= 0.95) score = 5.0;
        else if (ratio >= 0.85) score = 4.0;
        else if (ratio >= 0.6) score = 3.0;
        else if (ratio >= 0.3) score = 2.0;
        else score = 1.0;

        // Si le commentaire est neutre, limiter à 3 étoiles maximum
        if (neutralScore > 0 && score > 3) {
            score = 3;
        }

        // Vérification spéciale pour les mots simples
        if (containsOnlySimplePositiveWords(text) && score > 3) {
            score = 3;
        }

        return (int) Math.round(score);
    }

    private static double getWeightedKeywordScore(String text, Map<String, Double> keywords) {
        double score = 0;
        for (Map.Entry<String, Double> entry : keywords.entrySet()) {
            if (Pattern.compile("\\b" + Pattern.quote(entry.getKey()) + "\\b")
                      .matcher(text).find()) {
                score += entry.getValue();
                System.out.println("Mot trouvé: " + entry.getKey() + " (poids: " + entry.getValue() + ")");
            }
        }
        return score;
    }

    private static Map<String, Double> getPositiveWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("excellent", 1.5);
        words.put("parfait", 1.5);
        words.put("super", 1.2);
        words.put("génial", 1.2);
        words.put("satisfait", 1.0);
        words.put("recommande", 1.2);
        words.put("efficace", 0.8);
        words.put("fiable", 0.8);
        words.put("bien", 0.5);
        words.put("pratique", 0.5);
        words.put("bon", 0.5);
        words.put("agréable", 0.5);
        words.put("conforme", 0.5);
        words.put("qualité", 0.5);
        words.put("top", 0.8);
        words.put("parfaitement", 1.0);
        words.put("très bien", 0.8);
        words.put("exactement", 0.3);
        words.put("idéal", 0.8);
        words.put("fonctionne", 0.3);
        words.put("apprécié", 0.8);
        words.put("recommandé", 1.0);
        words.put("satisfaisant", 0.8);
        words.put("performant", 0.8);
        words.put("adapté", 0.5);
        words.put("very good", 1.2);
        words.put("great", 1.2);
        words.put("amazing", 1.5);
        words.put("perfect", 1.5);
        words.put("awesome", 1.5);
        words.put("fantastic", 1.5);
        words.put("wonderful", 1.2);
        words.put("superb", 1.5);
        words.put("outstanding", 1.5);
        words.put("impressive", 1.2);
        words.put("satisfied", 1.0);
        words.put("recommend", 1.2);
        words.put("good", 0.3);
        words.put("nice", 0.3);
        words.put("well", 0.3);
        words.put("fine", 0.3);
        words.put("satisfactory", 0.6);
        words.put("pleased", 0.8);
        words.put("happy", 0.8);
        words.put("delighted", 1.0);
        words.put("thrilled", 1.2);
        return words;
    }

    private static Map<String, Double> getNegativeWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("mauvais", 1.5);
        words.put("décevant", 1.2);
        words.put("panne", 1.5);
        words.put("cassé", 1.5);
        words.put("insatisfait", 1.2);
        words.put("médiocre", 1.2);
        words.put("inadmissible", 2.0);
        words.put("problème", 0.8);
        words.put("défaut", 0.8);
        words.put("mal", 0.8);
        words.put("difficile", 0.5);
        words.put("compliqué", 0.5);
        words.put("déçu", 1.2);
        words.put("défectueux", 1.5);
        words.put("ne fonctionne pas", 1.5);
        words.put("ne marche pas", 1.5);
        words.put("inutilisable", 1.5);
        words.put("insatisfaisant", 1.2);
        words.put("défaillant", 1.5);
        words.put("inadapté", 1.0);
        words.put("déplorable", 1.5);
        return words;
    }

    private static Map<String, Double> getNeutralWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("correct", 0.5);
        words.put("normal", 0.5);
        words.put("standard", 0.5);
        words.put("basique", 0.5);
        words.put("simple", 0.4);
        words.put("fonctionnel", 0.4);
        words.put("comme prévu", 0.5);
        words.put("attendu", 0.4);
        words.put("habituel", 0.4);
        words.put("ordinaire", 0.4);
        words.put("moyen", 0.5);
        words.put("classique", 0.4);
        words.put("conventionnel", 0.4);
        words.put("traditionnel", 0.4);
        return words;
    }

    private static boolean containsCriticalNegative(String text) {
        String[] criticalWords = {
            "inadmissible", "catastrophique", "horrible", "inutile",
            "dangereux", "arnaque", "scandaleux", "déplorable",
            "dangereux", "risque", "défectueux", "ne fonctionne pas du tout",
            "ne marche pas du tout", "inutilisable", "déplorable",
            "scandaleux", "inacceptable"
        };

        for (String word : criticalWords) {
            if (text.contains(word)) {
                System.out.println("Mot critique trouvé: " + word);
                return true;
            }
        }
        return false;
    }

    private static boolean containsOnlySimplePositiveWords(String text) {
        String[] simpleWords = {"good", "nice", "well", "fine", "ok", "okay", "bien", "bon"};
        String[] significantWords = {
            "excellent", "parfait", "super", "génial", "satisfait", "recommande",
            "efficace", "fiable", "pratique", "agréable", "conforme", "qualité",
            "top", "parfaitement", "très bien", "idéal", "apprécié", "recommandé",
            "satisfaisant", "performant", "adapté", "very good", "great", "amazing",
            "perfect", "awesome", "fantastic", "wonderful", "superb", "outstanding",
            "impressive", "satisfied", "recommend", "pleased", "happy", "delighted", "thrilled"
        };
        
        // Vérifier si le texte contient au moins un mot significatif
        for (String word : significantWords) {
            if (Pattern.compile("\\b" + Pattern.quote(word) + "\\b")
                      .matcher(text).find()) {
                return false;
            }
        }
        
        // Vérifier si le texte contient au moins un mot simple
        boolean containsSimpleWord = false;
        for (String word : simpleWords) {
            if (Pattern.compile("\\b" + Pattern.quote(word) + "\\b")
                      .matcher(text).find()) {
                containsSimpleWord = true;
                break;
            }
        }
        
        // Si le texte ne contient que des mots simples et est court
        return containsSimpleWord && text.trim().length() < 20;
    }
}