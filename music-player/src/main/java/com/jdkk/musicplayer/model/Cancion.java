package com.jdkk.musicplayer.model;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.IOException;


import java.util.*;

import com.jdkk.musicplayer.view.AlbumCoverView;
import com.jdkk.musicplayer.view.ButtonPlayer;

import java.io.File;

public class Cancion {
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;
    private List<File> musicFiles;
    private boolean onRepeat;
    private boolean onShuffle;
    private List<ButtonPlayer> buttonsPlayers;
    private List<File> listaActual;
    private Slider timelineSlider;
    private BooleanProperty userIsDragging;
    private Label timestampLabel;
    private ProgressBar progressBar;
    private double volume;
    private AlbumCoverView albumCover; // Added to update cover and title

    public Cancion(List<File> musicFiles, List<ButtonPlayer> buttons, ProgressBar progressBar, AlbumCoverView albumCover) {
        this.onRepeat = false;
        this.onShuffle = false;
        this.musicFiles = musicFiles;
        this.listaActual = new ArrayList<>(musicFiles);
        Media media = new Media(musicFiles.get(0).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        this.buttonsPlayers = buttons;
        this.progressBar = progressBar;
        this.userIsDragging = new SimpleBooleanProperty(false);
        this.volume = 1;
        this.albumCover = albumCover; // Initialize AlbumCoverView
    }

    public void setTimelineSlider(Slider slider) {
        this.timelineSlider = slider;
    }

    public void setVolumeCancion(double volume) {
        mediaPlayer.setVolume(volume);
        this.volume = volume;
        if (volume == 0){
            buttonsPlayers.get(5).setImageView("/icons/volume-off.png");
        }else if (volume  < 0.51){
            buttonsPlayers.get(5).setImageView("/icons/volume-low.png");
        } else {
            buttonsPlayers.get(5).setImageView("/icons/volume.png");

        }
    }

    public void setTimestampLabel(Label label) {
        this.timestampLabel = label;
    }

    public void setUserIsDragging(boolean userIsDragging) {
        this.userIsDragging.set(userIsDragging);
    }

    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void setIndex(int index) {
        this.currentIndex = index;
    }

    public void setMediaPlayer(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        if (listaActual.isEmpty() || index < 0 || index >= listaActual.size()) {
            System.err.println("Error: listaActual está vacía o índice fuera de rango en setMediaPlayer.");
            return;
        }

        Media media = new Media(listaActual.get(index).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(this.volume);

        // Add the timeline update listener here
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null && !userIsDragging.get()) {
                Duration current = newTime;
                Duration total = mediaPlayer.getTotalDuration();
                if (total != null && total.toSeconds() > 0) {
                    double progress = (current.toSeconds() / total.toSeconds()) * 100;
                    Platform.runLater(() -> {
                        timelineSlider.setValue(progress);
                        progressBar.setProgress(progress / 100);
                        timestampLabel.setText(formatDuration(current) + " / " + formatDuration(total));
                    });
                }
            }
        });
    }

    public void playSong(int index) {
        if (listaActual.isEmpty()) {
            System.err.println("No hay canciones en la lista para reproducir.");
            return;
        }

        // Ajustar índice si está fuera de rango
        currentIndex = (index + listaActual.size()) % listaActual.size();

        setMediaPlayer(currentIndex);
        mediaPlayer.play();

        System.out.println("Reproduciendo: " + listaActual.get(currentIndex).getName());
        buttonsPlayers.getFirst().setImageView("/icons/pause.png");

        // Update album cover and title
        Platform.runLater(() -> albumCover.updateCover(mediaPlayer));

        mediaPlayer.setOnEndOfMedia(() -> {
            if (onRepeat) {
                playSong(currentIndex); // Replay same song if onRepeat is true
            } else {
                currentIndex = (currentIndex + 1) % listaActual.size();
                playSong(currentIndex);
            }
        });
    }

    public void stopSong() {
        buttonsPlayers.getFirst().setImageView("/icons/play.png");
        mediaPlayer.stop();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int buscarCancion(List<File> musicFiles, String query) {
        for (int i = 0; i < musicFiles.size(); i++) {
            String fileName = musicFiles.get(i).getName().toLowerCase();
            if (fileName.contains(query.toLowerCase())) {
                return i; // Devuelve el índice de la canción encontrada
            }
        }
        return -1; // Retorna -1 si no se encuentra la canción
    }

    public boolean playPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                buttonsPlayers.getFirst().setImageView("/icons/play.png");
                System.out.println("Pausado.");
                return false;
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
                System.out.println("Reanudado.");
                buttonsPlayers.getFirst().setImageView("/icons/pause.png");
                return true;
            } else {
                playSong(0);
                buttonsPlayers.getFirst().setImageView("/icons/pause.png");
                return true;
            }
        } else {
            playSong(0);
            buttonsPlayers.getFirst().setImageView("/icons/pause.png");
            return true;
        }
    }

    public void playNext() {
        stopSong();
        if (!listaActual.isEmpty()) {
            currentIndex = (currentIndex + 1) % listaActual.size();
            playSong(currentIndex);
        } else {
            System.out.println("No hay canciones en la lista actual para avanzar.");
        }
    }

    public void playPrevious() {
        stopSong();
        if (!listaActual.isEmpty()) {
            currentIndex = (currentIndex - 1 + listaActual.size()) % listaActual.size();
            playSong(currentIndex);
        } else {
            System.out.println("No hay canciones en la lista actual para retroceder.");
        }
    }

    private int findMatchingFileIndex(String targetPath) {
        try {
            String canonicalTarget = new File(targetPath).getCanonicalPath();

            for (int i = 0; i < musicFiles.size(); i++) {
                File file = musicFiles.get(i);
                if (file.getCanonicalPath().equals(canonicalTarget)) {
                    return i; // Index of match
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1; // No match found
    }

    public void playRandom() {
        onShuffle = !onShuffle;
        if (onShuffle) {
            Collections.shuffle(listaActual);
            currentIndex = listaActual.size();
            buttonsPlayers.get(4).setImageView("/icons/arrows-shuffle-on.png");
//            if (currentIndex + 1 > listaActual.size() - 1) {
//                currentIndex = 0;
//            } else {
//                currentIndex++;
//            }
            currentIndex = -1;
        }else{
            String path = listaActual.get(currentIndex).getAbsolutePath();
            System.out.println("path"+ path);
            int indexLastSong = findMatchingFileIndex(path);
            System.out.println("lastindex" + indexLastSong);
            currentIndex = indexLastSong;
            listaActual = new ArrayList<>(musicFiles);
            buttonsPlayers.get(4).setImageView("/icons/arrows-shuffle.png");
        }
        System.out.println("CUrrent index"+ currentIndex);

    }

    public void playOnRepeat() {
        onRepeat = !onRepeat;
        if (onRepeat) {
            buttonsPlayers.get(3).setImageView("/icons/repeat-on.png");
        } else {
            buttonsPlayers.get(3).setImageView("/icons/repeat.png");
        }
        System.out.println("On-repeat activado");
    }

    public String obtenerInfoSiguienteCancion() {
        if (musicFiles.isEmpty()) {
            return "No hay canciones en la cola.";
        }
        int nextIndex = (currentIndex + 1) % musicFiles.size();
        return "Próxima canción: " + musicFiles.get(nextIndex).getName();
    }

    public String obtenerInfoCancionActual() {
        if (musicFiles.isEmpty()) {
            return "No hay canciones en reproducción.";
        }
        return "Canción actual: " + musicFiles.get(currentIndex).getName();
    }

    public List<File> getListaCanciones() {
        return musicFiles;
    }

    public void setCancionActual(int index) {
        if (index >= 0 && index < musicFiles.size()) {
            currentIndex = index;
            playSong(currentIndex);
        } else {
            System.out.println("Índice fuera de rango");
        }
    }

    public void setListaActual(List<File> listaActual) {
        this.listaActual = listaActual;
    }

    public List<File> getListaActual() {
        return listaActual;
    }
}