package com.jdkk.musicplayer.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Utiles {
    public static void clearConsole() {
        for (int i = 0; i < 20; i++) {
            System.out.println();
        }
    }

    // Metodo auxiliar para mostrar alertas
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Get the DialogPane to apply styles
        DialogPane dialogPane = alert.getDialogPane();

        // Apply the CSS stylesheet
        try {
            dialogPane.getStylesheets().add(Utiles.class.getResource("/css/estilo.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Error: Could not load estilo.css. Check resource path.");
        }

        // Add the alert-dialog style class
        dialogPane.getStyleClass().add("alert-dialog");

        // Map alert types to image paths
        String imagePath;
        switch (type) {
            case WARNING:
                imagePath = "/images/warning.png";
                break;
            case CONFIRMATION:
                imagePath = "/images/confirmation.png";
                break;
            case ERROR:
                imagePath = "/images/error.png";
                break;
            default:
                imagePath = "/images/default.png";
                break;
        }

        // Load and style the alert icon
        try {
            Image icon = new Image(Utiles.class.getResourceAsStream(imagePath));
            ImageView iconView = new ImageView(icon);
            iconView.setFitHeight(80);
            iconView.setFitWidth(80);
            iconView.getStyleClass().add("alert-icon");
            alert.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + imagePath);
        }

        alert.showAndWait();
    }

}
