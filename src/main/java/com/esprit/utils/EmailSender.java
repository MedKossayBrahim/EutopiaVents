package com.esprit.utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    public static void sendEmail(String recipient, String otp) {
        final String senderEmail = "kossaybrahim47@gmail.com";  // Replace with your email
        final String senderPassword = "gfvz hxbg cvzx tskl";      // Replace with your email password or App Password

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp + "\nThis code is valid for 10 minutes.");

            Transport.send(message);
            System.out.println("OTP sent successfully to " + recipient);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String otp = OTPGenerator.generateOTP();
        sendEmail( "MohamedKossay.BRAHIM@esprit.tn", otp);  // Replace with actual recipient email
    }
}
