package com.jdkk.musicplayer.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.net.URI;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jaudiotagger.tag.*;

public class AlbumCoverView {
    private ImageView albumCoverView;
    private Label songTitleLabel;
    private VBox container;

    public AlbumCoverView() {
        // ImageView para la portada
        albumCoverView = new ImageView();
        albumCoverView.setFitWidth(340);
        albumCoverView.setFitHeight(340);
        albumCoverView.getStyleClass().add("album-cover"); // Apply CSS class

        // Label para el nombre de la canción
        songTitleLabel = new Label("Sin canción");
        songTitleLabel.getStyleClass().add("song-title"); // Apply CSS class
        songTitleLabel.setMaxWidth(500);
        songTitleLabel.setWrapText(true);

        // VBox contenedor
        container = new VBox(10); // espacio entre portada y texto
        container.setPrefWidth(200);
        container.getChildren().addAll(albumCoverView, songTitleLabel);
        container.getStyleClass().add("album-container"); // Apply CSS class

        // Imagen inicial por defecto
        updateCover(null);
    }

    public VBox getView() {
        return container;
    }

    public void updateCover(MediaPlayer mediaPlayer) {
        Image albumCover = getAlbumCover(mediaPlayer);
        albumCoverView.setImage(albumCover);

        // Actualizar el nombre de la canción
        String title = getSongTitle(mediaPlayer);
        songTitleLabel.setText(title);
    }

    private Image getAlbumCover(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null || mediaPlayer.getMedia() == null) {
            return createDefaultAlbumCover();
        }

        try {
            String filePath = mediaPlayer.getMedia().getSource();
            java.io.File audioFile = new java.io.File(new URI(filePath).getPath());
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                byte[] imageData = artwork.getBinaryData();
                return new Image(new ByteArrayInputStream(imageData));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return createDefaultAlbumCover();
    }

    private String getSongTitle(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null || mediaPlayer.getMedia() == null) {
            return "Sin canción";
        }

        try {
            String filePath = mediaPlayer.getMedia().getSource();
            java.io.File audioFile = new java.io.File(new URI(filePath).getPath());
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            String title = tag.getFirst(FieldKey.TITLE);
            return (title != null && !title.isEmpty()) ? title : "Sin título";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al leer título";
        }
    }

    private Image createDefaultAlbumCover() {
        final int size = 340;
        WritableImage defaultImage = new WritableImage(size, size);
        PixelWriter writer = defaultImage.getPixelWriter();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                writer.setColor(x, y, Color.rgb(50, 50, 50));
            }
        }

        // Fondo rectangular gris claro
        Color lightGray = Color.rgb(70, 70, 70);
        int rectWidth = 272;
        int rectHeight = 306;
        int rectX = (size - rectWidth) / 2;
        int rectY = (size - rectHeight) / 2;
        for (int x = rectX; x < rectX + rectWidth; x++) {
            for (int y = rectY; y < rectY + rectHeight; y++) {
                writer.setColor(x, y, lightGray);
            }
        }

        // Vinilo
        Color black = Color.BLACK;
        int vinylRadius = 85;
        int centerX = size / 2;
        int centerY = size / 2;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                double dx = (x - centerX) / (double) vinylRadius;
                double dy = (y - centerY) / (double) vinylRadius;
                if (dx * dx + dy * dy <= 1) {
                    writer.setColor(x, y, black);
                }
            }
        }

        // Etiqueta
        Color white = Color.WHITE;
        int labelRadius = 17;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                double dx = (x - centerX) / (double) labelRadius;
                double dy = (y - centerY) / (double) labelRadius;
                if (dx * dx + dy * dy <= 1) {
                    writer.setColor(x, y, white);
                }
            }
        }

        return defaultImage;
    }
}