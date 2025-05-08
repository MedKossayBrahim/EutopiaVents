package com.esprit.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;

public class MqttService {

    private static final String BROKER = "tcp://broker.hivemq.com:1883";
    private static final String CLIENT_ID = "SmsPublisherClient";
    private static final String TOPIC = "sms/send";

    private MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttService() throws MqttException {
        connect();
    }

    private void connect() throws MqttException {
        if (client == null || !client.isConnected()) {
            client = new MqttClient(BROKER, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);
            System.out.println("Connected to MQTT Broker");
        }
    }

    public void publishSms(String to, String message) throws Exception {
        if (!client.isConnected()) {
            connect();
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("to", to);
        payload.put("message", message);

        String json = objectMapper.writeValueAsString(payload);
        MqttMessage mqttMessage = new MqttMessage(json.getBytes());
        mqttMessage.setQos(1);

        client.publish(TOPIC, mqttMessage);
        System.out.println("Published: " + json);
    }


    public void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
            System.out.println("Disconnected from MQTT broker");
        }
    }
}