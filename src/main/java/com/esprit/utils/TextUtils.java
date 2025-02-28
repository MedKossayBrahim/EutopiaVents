package com.esprit.utils;

public class TextUtils {
    public static String stripEmojis(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove emojis and other special characters
        // This regex pattern matches most emoji characters
        return input.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "")
                   .replaceAll("\\s+", " ")  // Replace multiple spaces with single space
                   .trim();
    }
} 