package com.esprit.controllers;

import com.esprit.utils.DataReceiver;
import com.esprit.utils.EmailSender;
import com.esprit.utils.OTPGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EnterOPTView implements DataReceiver<String> {
    String generatedOTP = OTPGenerator.generateOTP();
    @FXML
    private TextField input1;
    @FXML
    private TextField input2;
    @FXML
    private TextField input3;
    @FXML
    private TextField input4;
    @FXML
    private TextField input5;
    private String input;

    @Override
    public void setData(String data) {
        System.out.println(data);

        EmailSender.sendEmail(data, generatedOTP);
        setupTextField(input1, input2);
        setupTextField(input2, input3);
        setupTextField(input3, input4);
        setupTextField(input4, input5);
        setupTextField(input5, null);

    }


    private void setupTextField(TextField current, TextField next) {
        current.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 2) {
                distributeOTP(newText); // If full OTP is pasted, split it
            } else if (newText.length() == 1 && next != null) {
                next.requestFocus(); // Move to next field
            } else if (newText.length() > 1) {
                current.setText(newText.substring(0, 1)); // Keep only 1 character
            }
        });
    }

    private void distributeOTP(String otp) {
        String[] digits = otp.split("");
        if (digits.length > 0) input1.setText(digits[0]);
        if (digits.length > 1) input2.setText(digits[1]);
        if (digits.length > 2) input3.setText(digits[2]);
        if (digits.length > 3) input4.setText(digits[3]);
        if (digits.length > 4) input5.setText(digits[4]);
        input5.requestFocus(); // Move to the last field
    }

    @FXML
    public void check(ActionEvent actionEvent) {
        String userOTP = input1.getText() + input2.getText() + input3.getText() + input4.getText() + input5.getText();

        if (generatedOTP.equals(userOTP)) {
            System.out.println(" OTP Verified Successfully!");
        } else {
            System.out.println(" Invalid OTP. Please try again.");
        }
    }
}

