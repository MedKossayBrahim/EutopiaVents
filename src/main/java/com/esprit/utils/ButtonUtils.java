package com.esprit.utils;

import com.esprit.models.Role;
import com.esprit.models.User;
import javafx.scene.Node;
import javafx.scene.control.Button;
import java.util.Map;
import java.util.HashMap;

public class ButtonUtils {

    public static void setupButtonVisibility(User currentUser, Map<String, Node> buttons) {
        if (currentUser == null) {
            // Si aucun utilisateur n'est connecté, cacher tous les boutons
            hideAllButtons(buttons);
            return;
        }

        switch (currentUser.getRole()) {
            case Admin:
                // Admin peut voir tous les boutons
                showAllButtons(buttons);
                break;

            case Organisateur:
                // Organisateur peut voir tous les boutons sauf certains
                showAllButtons(buttons);
                hideSpecificButtons(buttons, "btnAjouterCateg", "btnGererEvenements");
                break;

            case Participant:
                // Participant ne peut voir que certains boutons
                hideAllButtons(buttons);
                showSpecificButtons(buttons, "btnPanier", "searchField", "categoryFilter");
                break;

            default:
                hideAllButtons(buttons);
                break;
        }
    }

    private static void hideAllButtons(Map<String, Node> buttons) {
        buttons.values().forEach(button -> {
            button.setVisible(false);
            button.setManaged(false);
        });
    }

    private static void showAllButtons(Map<String, Node> buttons) {
        buttons.values().forEach(button -> {
            button.setVisible(true);
            button.setManaged(true);
        });
    }

    private static void hideSpecificButtons(Map<String, Node> buttons, String... buttonIds) {
        for (String buttonId : buttonIds) {
            Node button = buttons.get(buttonId);
            if (button != null) {
                button.setVisible(false);
                button.setManaged(false);
            }
        }
    }

    private static void showSpecificButtons(Map<String, Node> buttons, String... buttonIds) {
        for (String buttonId : buttonIds) {
            Node button = buttons.get(buttonId);
            if (button != null) {
                button.setVisible(true);
                button.setManaged(true);
            }
        }
    }
}