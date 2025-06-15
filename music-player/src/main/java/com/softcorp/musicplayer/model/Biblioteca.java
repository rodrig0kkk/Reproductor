package com.softcorp.musicplayer.model;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;

import java.util.logging.*;
import java.io.PrintStream;
import java.io.Reader;
import java.io.OutputStream;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.softcorp.musicplayer.model.Playlist.Lista;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;

import java.util.HashMap;

public class Biblioteca {
    // private List<File> listaCanciones;
    private Map<String, List<File>> cancionesPorGenero;
    private Map<String, List<File>> cancionesPorArtista;

    private final List<File> listaCanciones;
    private static final List<String> FORMATOS_VALIDOS = Arrays.asList(".mp3", ".wav", ".aac");

    private static final String MUSIC_DIRECTORY = System.getProperty("user.home") + "/Music/";
    private File musicDir = new File(MUSIC_DIRECTORY);

    public Playlist playlist;

    public Biblioteca() {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        cancionesPorArtista = new HashMap<>();
        cancionesPorGenero = new HashMap<>();
        listaCanciones = new ArrayList<>();
        actualizarListaCanciones();
        this.playlist = new Playlist();
    }

    static {

        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);

        LogManager.getLogManager().reset();
        Logger.getGlobal().setLevel(Level.OFF);
    }

    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        System.setProperty("org.jaudiotagger.logging.level", "OFF");
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // Ignorar logs de error
            }
        }));
    }

    public void actualizarListaCanciones() {
        File[] archivos = musicDir.listFiles((dir, name) -> esFormatoValido(name));

        listaCanciones.clear(); // Limpiar lista antes de actualizar
        if (archivos != null) {
            listaCanciones.addAll(Arrays.asList(archivos)); // Agregar nuevos archivos
        }
    }

    public List<File> getListaCanciones() {
        return listaCanciones;
    }

    private boolean esFormatoValido(String fileName) {
        return FORMATOS_VALIDOS.stream().anyMatch(fileName.toLowerCase()::endsWith);
    }

    public void addListaCanciones(String directoryPath) {
        File newDir = new File(directoryPath);

        if (newDir.exists() && newDir.isDirectory()) {
            // Obtener los archivos de música en el nuevo directorio
            File[] newFiles = newDir.listFiles((dir, name) -> esFormatoValido(name));

            if (newFiles != null && newFiles.length > 0) {
                for (File archivo : newFiles) {
                    boolean yaExiste = listaCanciones.stream()
                            .anyMatch(c -> c.getAbsolutePath().equals(archivo.getAbsolutePath()));
                    if (!yaExiste) {
                        listaCanciones.add(archivo);
                    } else {
                        System.out.println("La canción ya está en la lista.");
                    }
                }
                System.out.println(newFiles.length + " canciones agregadas.");
            } else {
                System.out.println("No existen archivos .mp3, .wav, .aac en el directorio.");
            }
        } else {
            System.out.println("La carpeta no existe o no es válida.");
        }
    }

    public void addCanciones(List<String> filePaths) {
        for (String filePath : filePaths) {
            File newFile = new File(filePath);

            if (newFile.exists() && newFile.isFile() && esFormatoValido(newFile.getName())) {

                boolean yaExiste = listaCanciones.stream()
                        .map(File::getAbsolutePath)
                        .anyMatch(path -> path.equalsIgnoreCase(newFile.getAbsolutePath()));

                if (!yaExiste) {
                    listaCanciones.add(newFile);
                    System.out.println("Añadida: " + newFile.getName());
                } else {
                    System.out.println("La canción ya está en la lista.");
                }
            } else {
                System.out.println("Archivo no válido: " + filePath);
            }
        }
    }

    public void agregarCancionDeCarpeta(Stage stage) {
        // File chooser to select files (files or directories)
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            System.out.println("Carpeta seleccionada: " +
                    selectedDirectory.getAbsolutePath());
        }
        addListaCanciones(selectedDirectory.getAbsolutePath());
    }

    public void agregarCanciones(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona archivos de audio");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de audio", "*.mp3", "*.wav", "*.aac"));

        List<File> archivos = fileChooser.showOpenMultipleDialog(stage); // Selección múltiple

        if (archivos != null && !archivos.isEmpty()) {
            List<String> rutas = new ArrayList<>();
            for (File archivo : archivos) {
                System.out.println("Archivo seleccionado: " + archivo.getAbsolutePath());
                rutas.add(archivo.getAbsolutePath());
            }
            addCanciones(rutas); // Ahora agregamos varias canciones
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }

    public void guardarListaCancionesJson(String filePath) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(listaCanciones.stream().map(File::getAbsolutePath).toList(), writer);
            System.out.println("Lista guardada en JSON correctamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar la lista en JSON: " + e.getMessage());
        }
    }

    public void cargarListaCancionesJson(String filePath) {
        Gson gson = new Gson();
        File archivo = new File(filePath);

        if (!archivo.exists()) {
            System.out.println("El archivo JSON no existe.");
            return;
        }

        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            List<String> rutas = gson.fromJson(reader, listType);

            listaCanciones.clear();
            for (String ruta : rutas) {
                File cancion = new File(ruta);
                if (cancion.exists() && cancion.isFile()) {
                    listaCanciones.add(cancion);
                }
            }
            System.out.println("Lista de canciones cargada desde JSON.");
        } catch (IOException e) {
            System.out.println("Error al cargar la lista desde JSON: " + e.getMessage());
        }
    }

    public void eliminarCancion(int indice) {
        if (indice >= 0 && indice < listaCanciones.size()) {
            File eliminada = listaCanciones.remove(indice);
            System.out.println("Canción eliminada: " + eliminada.getName());
        } else {
            System.out.println("Índice inválido.");
        }
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

    public void listadodeCanciones() {
        System.out.println("Listado de canciones:");
        List<File> listaInmutable = new ArrayList<>(getListaCanciones());

        for (int i = 0; i < listaInmutable.size(); i++) {
            for (int j = 0; j < listaInmutable.size() - 1 - i; j++) {
                File current = listaInmutable.get(j);
                File next = listaInmutable.get(j + 1);

                if (current.getName().compareTo(next.getName()) > 0) {
                    // Si el orden está mal, intercambiamos los archivos
                    listaInmutable.set(j, next);
                    listaInmutable.set(j + 1, current);
                }
            }
        }
        for (int i = 0; i < listaInmutable.size(); i++) {
            System.out.println(listaInmutable.get(i).getName());
        }
    }

    public void mostrarListaActual() {
        int contador = 1;
        System.out.println("Lista por adición:");
        for (File listaCancione : listaCanciones) {
            System.out.println(contador + ". " + listaCancione.getName());
            contador++;
        }
    }

    public void mostrarPorTitulo() {
        List<File> copiaOrdenada = new ArrayList<>(listaCanciones);

        copiaOrdenada.sort((f1, f2) -> {
            Tag t1 = obtenerMetadataCancion(f1);
            Tag t2 = obtenerMetadataCancion(f2);
            String s1 = (t1 != null) ? t1.getFirst(FieldKey.TITLE) : "";
            String s2 = (t2 != null) ? t2.getFirst(FieldKey.TITLE) : "";
            return s1.compareToIgnoreCase(s2);
        });

        setListaTemporal(copiaOrdenada);

        int contador = 1;
        System.out.println("Canciones ordenadas por título:");
        for (File cancion : copiaOrdenada) {
            System.out.println(contador + ". " + cancion.getName());
            contador++;
        }
    }

    public void mostrarCancionesPorArtista() {
        int contador = 1;
        for (Map.Entry<String, List<File>> entry : cancionesPorArtista.entrySet()) {
            String artista = entry.getKey();
            List<File> canciones = entry.getValue();
            System.out.println("Artista: " + artista);
            for (File cancione : canciones) {
                System.out.println(contador + ". " + cancione.getName());
                contador++;
            }
        }
    }

    public void mostrarPorArtista() {
        cancionesPorArtista.clear();
        List<File> temporal = new ArrayList<>();

        for (File cancion : listaCanciones) {
            Tag tag = obtenerMetadataCancion(cancion);
            if (tag != null) {
                String artista = tag.getFirst(FieldKey.ARTIST);
                if (artista != null && !artista.isEmpty()) {
                    cancionesPorArtista.computeIfAbsent(artista, k -> new ArrayList<>()).add(cancion);
                    temporal.add(cancion);
                }
            }
        }

        setListaTemporal(temporal);
        mostrarCancionesPorArtista();
    }
    public void mostrarCancionesPorGenero() {
        int contador = 1;
        for (Map.Entry<String, List<File>> entry : cancionesPorGenero.entrySet()) {
            String genero = entry.getKey();
            List<File> canciones = entry.getValue();
            System.out.println("Género: " + genero);
            for (File cancione : canciones) {
                System.out.println(contador + ". " + cancione.getName());
                contador++;
            }
        }
    }

    public void mostrarPorGenero() {
        cancionesPorGenero.clear();
        List<File> temporal = new ArrayList<>();

        for (File cancion : listaCanciones) {
            Tag tag = obtenerMetadataCancion(cancion);
            if (tag != null) {
                String genero = tag.getFirst(FieldKey.GENRE);
                if (genero != null && !genero.isEmpty()) {
                    cancionesPorGenero.computeIfAbsent(genero, k -> new ArrayList<>()).add(cancion);
                    temporal.add(cancion);
                }
            }
        }

        setListaTemporal(temporal);
        mostrarCancionesPorGenero();
    }

    public void mostrarPlaylistsConIndices() {
        if (playlist.getPlaylists().isEmpty()) {
            System.out.println("No hay listas de reproducción creadas.");
        } else {
            for (int i = 0; i < playlist.getPlaylists().size(); i++) {
                System.out.println((i + 1) + ". " + playlist.getPlaylists().get(i).nombre);
            }
        }
    }

    public void crearPlaylist(String nombre) {
        playlist.crearListaDeReproduccion(nombre);
    }

    public void agregarCancionAPlaylist() {
        playlist.agregarCancion(listaCanciones);
    }

    public void eliminarCancionDePlaylist() {
        playlist.eliminarCancion();
    }

    public void editarNombrePlaylist() {
        playlist.editarNombreLista();
    }

    public void eliminarPlaylist(int indice) {
        playlist.eliminarListaDeReproduccion(indice);
        System.out.println("Playlist eliminada.");
    }

    public void guardarPlaylists() {
        this.playlist.guardarPlaylists();
        System.out.println("Playlists guardadas en archivo.");
    }

    public String obtenerNombrePlaylistPorIndice(int indice) {
        if (indice >= 0 && indice < playlist.getPlaylists().size()) {
            return playlist.getPlaylists().get(indice).nombre;
        }
        return null;
    }

    public void mostrarPlaylistsDesdeBiblioteca() {
        this.playlist.mostrarPlaylists();
    }

    public Map<String, List<File>> getCancionesPorArtista() {
        return cancionesPorArtista;
    }

    public Map<String, List<File>> getCancionesPorGenero() {
        return cancionesPorGenero;
    }
    private List<File> listaTemporal = new ArrayList<>();

    public void setListaTemporal(List<File> lista) {
        listaTemporal = lista;
    }

    public List<File> getListaTemporal() {
        return listaTemporal;
    }
    public void limpiarListaTemporal() {
        listaTemporal.clear();
    }


    public void mostrarPlaylists() {
        limpiarListaTemporal();

        System.out.println(" -- LISTAS DE REPRODUCCIÓN -- ");
        System.out.println("Total: " + playlist.size() + " playlist");

        for (int i = 0; i < playlist.size(); i++) {
            Lista lista = playlist.get(i);
            System.out.println("══════════════════════════════════════");
            System.out.println(" Playlist #" + (i + 1) + ": " + lista.nombre);
            int totalCanciones = lista.nombreDeCancion.size();
            System.out.println(" Total de canciones: " + totalCanciones);

            if (totalCanciones == 0) {
                System.out.println("   (Esta lista está vacía)");
            } else {
                System.out.println("  Canciones:");
                for (int j = 0; j < totalCanciones; j++) {
                    String cancion = lista.nombreDeCancion.get(j);
                    System.out.printf("      %2d. %s%n", j + 1, cancion);
                }
            }
            System.out.println("══════════════════════════════════════");
        }
        Scanner leer = new Scanner(System.in);
        System.out.println("Elija la lista que desea reproducir ");
        int seleccion = leer.nextInt();
        leer.nextLine();

        if (seleccion > 0 && seleccion <= playlist.size()) {
            Lista listaSeleccionada = playlist.get(seleccion - 1);
            System.out.println("Lista seleccionada: " + listaSeleccionada.nombre);
            listaTemporal.clear();
            for (String cancion : listaSeleccionada.nombreDeCancion) {
                listaTemporal.add(new File(cancion));
            }

        } else {
            System.out.println("Opción inválida.");
        }
    }

}