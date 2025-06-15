package com.softcorp.musicplayer.view;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ButtonPlayer {
    private Image buttonImage;
    private ImageView buttonView;
    private Button buttonplayer;

    public ButtonPlayer (int width, int height, String path){
        buttonImage = new Image(getClass().getResource(path).toExternalForm());
        buttonView = new ImageView(buttonImage);
        buttonView.setFitWidth(width);
        buttonView.setFitHeight(height);
        buttonplayer = new javafx.scene.control.Button("", buttonView);
        buttonplayer.setStyle("""
                        -fx-background-color: transparent;
                        -fx-border-color: transparent;
                        -fx-padding: 4;
                        -fx-cursor: hand;
                        """);
        buttonplayer.setOnMouseEntered(e -> buttonplayer.setStyle("""
                        -fx-background-color: #333333;
                        -fx-border-color: transparent;
                        -fx-padding: 4;
                        -fx-cursor: hand;
                        """));
        buttonplayer.setOnMouseExited(e -> buttonplayer.setStyle("""
                        -fx-background-color: transparent;
                        -fx-border-color: transparent;
                        -fx-padding: 4;
                        -fx-cursor: hand;
                        """));
    }

    public Button getButtonPlayer(){
        return buttonplayer;
    }
    public void setImageView(String imagePath) {
        Image newImage = new Image(getClass().getResource(imagePath).toExternalForm());
        buttonView.setImage(newImage); // update the existing ImageView
    }


}
