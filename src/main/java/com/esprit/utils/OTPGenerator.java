package com.esprit.utils;

import java.security.SecureRandom;

public class OTPGenerator {
    public static String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // Change to "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" for alphanumeric

        for (int i = 0; i < 5; i++) {
            otp.append(chars.charAt(random.nextInt(chars.length())));
        }
        return otp.toString();
    }


}
