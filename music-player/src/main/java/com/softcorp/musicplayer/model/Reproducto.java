package com.softcorp.musicplayer.model;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Reproducto {
  private List<File> listaActual = new ArrayList<>();
     private int cancionActual = -1;

    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;
    private List<File> musicFiles;
    private boolean onRepeat;
  public void setListaActual(List<File> lista) {
    this.listaActual = lista;
}

public void setCancionActual(int index) {
    if (listaActual != null && index >= 0 && index < listaActual.size()) {
        Media media = new Media(listaActual.get(index).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        this.mediaPlayer = mediaPlayer;
    } else {
        System.out.println("Índice fuera de rango o lista no cargada.");
    }
 }

public MediaPlayer getMediaPlayer() {
    return this.mediaPlayer;
 } 
}
