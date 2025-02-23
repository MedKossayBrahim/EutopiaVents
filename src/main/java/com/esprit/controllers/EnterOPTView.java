package com.esprit.controllers;

import com.esprit.tests.Eutopia;
import com.esprit.utils.DataReceiver;
import com.esprit.utils.EmailSender;
import com.esprit.utils.OTPGenerator;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class EnterOPTView implements DataReceiver<String> {
    public Group back;
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
    String mail;

    @Override
    public void setData(String data) {
        mail= data;
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
            if (!newText.isEmpty()) {
                newText = newText.toUpperCase(); // Convert to uppercase
                current.setText(newText);
            }
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
        String[] digits = otp.toUpperCase().split(""); // Convert to uppercase before splitting
        if (digits.length > 0) input1.setText(digits[0]);
        if (digits.length > 1) input2.setText(digits[1]);
        if (digits.length > 2) input3.setText(digits[2]);
        if (digits.length > 3) input4.setText(digits[3]);
        if (digits.length > 4) input5.setText(digits[4]);
        input5.requestFocus(); // Move to the last field
    }

    @FXML
    public void check() throws IOException {
        String userOTP = (input1.getText() + input2.getText() + input3.getText() + input4.getText() + input5.getText()).toUpperCase();

        if (generatedOTP.equals(userOTP)) {
            Eutopia.getSceneManager().switchScene("/newPass-view.fxml", mail);
            System.out.println(" OTP Verified Successfully!");
        } else {
            System.out.println(" Invalid OTP. Please try again.");
        }
    }

    public void back(MouseEvent mouseEvent) throws IOException {
        Eutopia.getSceneManager().switchScene("/otp-view.fxml", null);
    }
}
