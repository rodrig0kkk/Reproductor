package com.softcorp.musicplayer.model;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaDeReproduccion {

    private Biblioteca biblioteca;
    private String nombre;
    private List<File> canciones;


    public ListaDeReproduccion(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    public Tag obtenerMetadataCancion(File archivo) {
        if (archivo.exists() && archivo.isFile()) {
            try {
                AudioFile audioFile = AudioFileIO.read(archivo);
                return (audioFile != null) ? audioFile.getTag() : null;
            } catch (CannotReadException e) {
                System.err.println("Error: No se puede leer el archivo de audio.");
            } catch (IOException e) {
                System.err.println("Error de entrada/salida.");
            } catch (TagException e) {
                System.err.println("Error en las etiquetas de metadatos.");
            } catch (ReadOnlyFileException e) {
                System.err.println("Error: El archivo es de solo lectura.");
            } catch (InvalidAudioFrameException e) {
                System.err.println("Error: El archivo tiene un frame de audio inválido.");
            }
        }
        return null; // Retorna null si hay un error o si el archivo no es válido.
    }
    public ListaDeReproduccion(String nombre) {
        this.nombre = nombre;
        this.canciones = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<File> getCanciones() {
        return canciones;
    }

    public void agregarCancion(File cancion) {
        canciones.add(cancion);
    }

    public void eliminarCancion(File cancion) {
        canciones.remove(cancion);
    }
}
