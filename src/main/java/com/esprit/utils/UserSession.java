package com.esprit.utils;

import com.esprit.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class UserSession {
    private static final String FILE_NAME = "user_session.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveUser(User user) {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(user, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User loadUser() {
        try (Reader reader = new FileReader(FILE_NAME)) {
            return gson.fromJson(reader, User.class);
        } catch (IOException e) {
            return null; // No user session found
        }
    }

    public static void clearUser() {
        new File(FILE_NAME).delete();
    }
}