package com.softcorp.musicplayer.model;

import javafx.application.Platform;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BuscarCancion {
    private final LinkedList<String> historialBusquedas = new LinkedList<>(); // Guarda últimas 10 búsquedas
    private static final int MAX_HISTORIAL = 10;
    private static final String HISTORIAL_FILE = "historial.json";
    private final Biblioteca biblioteca;
    private final Cancion reproductor;
    private final Gson gson = new Gson();

    public BuscarCancion(Biblioteca biblioteca, Cancion reproductor){
        this.biblioteca = biblioteca;
        this.reproductor = reproductor;
        cargarHistorial();
    }

    public void agregarHistorial(String query) {
        query = query.trim().toLowerCase();
        if (query.isEmpty()) return;
        // Si ya existe en el historial, la movemos al inicio
        historialBusquedas.remove(query);
        historialBusquedas.addFirst(query);

        // Mantener solo las últimas 10 búsquedas
        if (historialBusquedas.size() > MAX_HISTORIAL) {
            historialBusquedas.removeLast();
        }

        guardarHistorial();
    }

    public LinkedList<String> getHistorialBusquedas() {
        return new LinkedList<>(historialBusquedas);
    }

    private void guardarHistorial() {
        try (FileWriter writer = new FileWriter(HISTORIAL_FILE)) {
            gson.toJson(historialBusquedas, writer);
        } catch (IOException e) {
            System.err.println("Error guardando historial: " + e.getMessage());
        }
    }

    private void cargarHistorial() {
        File file = new File(HISTORIAL_FILE);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            List<String> historial = gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());
            if (historial != null) {
                historialBusquedas.addAll(historial);
            }
        } catch (IOException e) {
            System.err.println("Error cargando historial: " + e.getMessage());
        }
    }

    public List<File> buscarMetadatos(String query){
        List<File> resultados = new ArrayList<>();
        for (File file : biblioteca.getListaCanciones()) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();
                if (tag != null) {
                    String titulo = Optional.ofNullable(tag.getFirst(FieldKey.TITLE)).orElse("").toLowerCase();
                    String artista = Optional.ofNullable(tag.getFirst(FieldKey.ARTIST)).orElse("").toLowerCase();
                    String album = Optional.ofNullable(tag.getFirst(FieldKey.ALBUM)).orElse("").toLowerCase();
                    String genero = Optional.ofNullable(tag.getFirst(FieldKey.GENRE)).orElse("").toLowerCase();

                    if (titulo.contains(query) || artista.contains(query) || album.contains(query)
                            || genero.contains(query)) {
                        resultados.add(file);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error leyendo metadatos de '" + file.getName() + "': " + e.getMessage());
            }
        }
        return resultados;
    }

    public void mostrarHistorial() {
        if (historialBusquedas.isEmpty()) {
            System.out.println("No hay búsquedas recientes.");
            return;
        }

        System.out.println("Historial de búsquedas recientes:");
        for (String busqueda : historialBusquedas) {
            System.out.println("- " + busqueda);
        }
    }

    public void mostrarResultados(List<File> resultados, Scanner scanner){
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron coincidencias.");
        } else {
            System.out.println("Canciones encontradas:");
            for (int i = 0; i < resultados.size(); i++) {
                System.out.println((i + 1) + ". " + resultados.get(i).getName());
            }

            System.out.print("¿Desea reproducir alguna canción? (Ingrese el número o 'N' para salir): ");
            String respuesta = scanner.nextLine().trim().toLowerCase();

            if (!respuesta.equals("n")) {
                try {
                    int opcion = Integer.parseInt(respuesta) - 1;
                    if (opcion >= 0 && opcion < resultados.size()) {
                        int index = biblioteca.getListaCanciones().indexOf(resultados.get(opcion));
                        Platform.runLater(() -> reproductor.playSong(index));
                    } else {
                        System.out.println("Número inválido.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada no válida.");
                }
            }
        }
    }
}
