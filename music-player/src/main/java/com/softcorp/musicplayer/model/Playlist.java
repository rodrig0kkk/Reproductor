package com.softcorp.musicplayer.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.softcorp.musicplayer.controller.Utiles;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.media.MediaPlayer;

import com.google.gson.GsonBuilder;

public class Playlist {
    List<Lista> playlists;
    private Gson gson;
    private static final String PLAYLISTS_FILE = "playlists.json";
    private Scanner scanner = new Scanner(System.in);

    public Playlist() {
        this.gson = new Gson();
        this.playlists = cargarPlaylists();
    }

    // Clase interna para una lista de reproducción
    public static class Lista {
        public String nombre;
        public List<String> nombreDeCancion;

        public Lista(String nombre) {
            this.nombre = nombre;
            this.nombreDeCancion = new ArrayList<>();
        }

        public void agregarCancion(String nombreArchivo) {
            this.nombreDeCancion.add(nombreArchivo);
        }

        @Override
        public String toString() {
            return "Lista de reproducción: " + nombre + ", Canciones: "
                    + (nombreDeCancion.isEmpty() ? "Lista de canciones vacía" : nombreDeCancion);
        }
    }

    // Getter para la lista de playlists
    public List<Lista> getPlaylists() {
        return playlists;
    }

    // Cargar las playlists desde el archivo JSON
    private List<Lista> cargarPlaylists() {
        File archivo = new File(PLAYLISTS_FILE);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(archivo)) {
            Type playlistListType = new TypeToken<List<Lista>>() {
            }.getType();
            List<Lista> loadedPlaylists = gson.fromJson(reader, playlistListType);
            return (loadedPlaylists != null) ? loadedPlaylists : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error al cargar las playlists: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Guardar las playlists en el archivo JSON
    public void guardarPlaylists() {
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(PLAYLISTS_FILE)) {
            prettyGson.toJson(playlists, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar las playlists: " + e.getMessage());
        }
    }

    public void crearListaDeReproduccion(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("Error: El nombre de la lista de reproducción no puede estar vacío.");
            return;
        }
        if (playlists.stream().anyMatch(lista -> lista.nombre.equals(nombre))) {
            System.out.println("Ya existe una lista de reproducción con ese nombre.");
            return;
        }
        playlists.add(new Lista(nombre));
        guardarPlaylists();
        System.out.println("Lista de reproducción creada: " + nombre);
    }

    public void agregarCancion(List<File> cancionesDisponibles) {
        if (playlists.isEmpty()) {
            System.out.println("No hay listas de reproducción disponibles.");
            return;
        }

        System.out.println("Selecciona la lista a la que deseas agregar canciones:");
        for (int i = 0; i < playlists.size(); i++) {
            System.out.println((i + 1) + ". " + playlists.get(i).nombre);
        }

        System.out.print("Introduce el número de la lista: ");
        if (scanner.hasNextInt()) {
            int indiceListaSeleccionada = scanner.nextInt() - 1;
            scanner.nextLine();
            if (indiceListaSeleccionada >= 0 && indiceListaSeleccionada < playlists.size()) {
                Lista listaSeleccionada = playlists.get(indiceListaSeleccionada);
                System.out.println("\nCanciones disponibles:");
                for (int i = 0; i < cancionesDisponibles.size(); i++) {
                    System.out.println((i + 1) + ". " + cancionesDisponibles.get(i).getName());
                }
                System.out.print("Selecciona el número de la canción para agregar: ");
                if (scanner.hasNextInt()) {
                    int indiceCancionAAgregar = scanner.nextInt() - 1;
                    scanner.nextLine(); // consumir la nueva línea
                    if (indiceCancionAAgregar >= 0 && indiceCancionAAgregar < cancionesDisponibles.size()) {
                        String nombreArchivoAAgregar = cancionesDisponibles.get(indiceCancionAAgregar).getName();
                        if (!listaSeleccionada.nombreDeCancion.contains(nombreArchivoAAgregar)) {
                            listaSeleccionada.agregarCancion(nombreArchivoAAgregar);
                            guardarPlaylists();
                            System.out.println("Canción agregada correctamente a '" + listaSeleccionada.nombre + "'.");
                        } else {
                            System.out.println("La canción '" + nombreArchivoAAgregar + "' ya existe en esta lista.");
                        }
                    } else {
                        System.out.println("Índice de canción no válido.");
                    }
                } else {
                    System.out.println("Entrada inválida. Introduce un número de canción.");
                    scanner.nextLine();
                }
            } else {
                System.out.println("Índice de lista inválido.");
            }
        } else {
            System.out.println("Entrada inválida. Introduce un número de lista.");
            scanner.nextLine();
        }
    }

    public void agregarCancionGUI(Lista lista, List<File> cancionesAAgregar) {
        if (lista != null && cancionesAAgregar != null && !cancionesAAgregar.isEmpty()) {
            for (File cancion : cancionesAAgregar) {
                if (!lista.nombreDeCancion.contains(cancion.getName())) {
                    lista.agregarCancion(cancion.getName());
                    System.out.println("Añadida '" + cancion.getName() + "' a '" + lista.nombre + "'.");
                } else {
                    System.out.println("La canción '" + cancion.getName() + "' ya existe en '" + lista.nombre + "'.");
                }
            }
            guardarPlaylists();
        } else {
            System.out.println("No se seleccionó una lista o no hay canciones para agregar.");
        }
    }

    public void eliminarCancion() {
        if (playlists.isEmpty()) {
            System.out.println("No hay listas de reproducción disponibles.");
            return;
        }
        System.out.println("Selecciona la lista de reproducción de la que deseas eliminar una canción:");
        for (int i = 0; i < playlists.size(); i++) {
            System.out.println((i + 1) + ". " + playlists.get(i).nombre);
        }
        System.out.print("Introduce el número de la lista: ");
        if (scanner.hasNextInt()) {
            int indiceListaSeleccionada = scanner.nextInt() - 1;
            scanner.nextLine();
            if (indiceListaSeleccionada >= 0 && indiceListaSeleccionada < playlists.size()) {
                Lista listaSeleccionada = playlists.get(indiceListaSeleccionada);
                if (listaSeleccionada.nombreDeCancion.isEmpty()) {
                    System.out.println("La lista '" + listaSeleccionada.nombre + "' no tiene canciones para eliminar.");
                    return;
                }
                System.out.println("\nCanciones en '" + listaSeleccionada.nombre + "':");
                for (int i = 0; i < listaSeleccionada.nombreDeCancion.size(); i++) {
                    System.out.println((i + 1) + ". " + listaSeleccionada.nombreDeCancion.get(i));
                }
                System.out.print("Introduce el número de la canción que deseas eliminar: ");
                if (scanner.hasNextInt()) {
                    int indiceCancionAEliminar = scanner.nextInt() - 1;
                    scanner.nextLine();
                    if (indiceCancionAEliminar >= 0 && indiceCancionAEliminar < listaSeleccionada.nombreDeCancion.size()) {
                        String cancionAEliminar = listaSeleccionada.nombreDeCancion.get(indiceCancionAEliminar);
                        System.out.println("¿Estás seguro que deseas eliminar la canción '" + cancionAEliminar + "' de '" + listaSeleccionada.nombre + "'? (si/no)");
                        String respuesta = scanner.nextLine().trim().toLowerCase();
                        String cancionEliminada = "";
                        if (respuesta.equals("si")) {
                            cancionEliminada = listaSeleccionada.nombreDeCancion.remove(indiceCancionAEliminar);
                            guardarPlaylists();
                            System.out.println("Canción '" + cancionEliminada + "' eliminada de '" + listaSeleccionada.nombre + "'.");
                        } else {
                            System.out.println("Eliminación de la canción cancelada.");
                        }
                    } else {
                        System.out.println("Índice de canción no válido.");
                    }
                } else {
                    System.out.println("Entrada inválida. Introduce el número de la canción.");
                    scanner.nextLine();
                }
            } else {
                System.out.println("Índice de lista no válido.");
            }
        } else {
            System.out.println("Entrada inválida. Introduce el número de la lista.");
            scanner.nextLine();
        }
    }

    public void mostrarPlaylists() {
        if (playlists.isEmpty()) {
            System.out.println(" No hay listas de reproducción para mostrar.");
            return;
        }

        System.out.println(" -- LISTAS DE REPRODUCCIÓN -- ");
        System.out.println("Total: " + playlists.size() + " playlist");

        for (int i = 0; i < playlists.size(); i++) {
            Lista lista = playlists.get(i);
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
    }
    public boolean isEmpty() {
        return playlists.size() == 0;
    }
    public int size() {
        return playlists.size();
    }


    public Lista get(int index) {
        if (index >= 0 && index < playlists.size()) {
            return playlists.get(index);
        }
        return null;
    }


    public void editarNombreLista() {
        if (playlists.isEmpty()) {
            System.out.println("No hay listas de reproducción para editar.");
            return;
        }
        System.out.println("Selecciona la lista que deseas renombrar:");
        for (int i = 0; i < playlists.size(); i++) {
            System.out.println((i + 1) + ". " + playlists.get(i).nombre);
        }
        System.out.print("Introduce el número de la lista: ");
        if (scanner.hasNextInt()) {
            int indice = scanner.nextInt();
            scanner.nextLine();
            if (indice > 0 && indice <= playlists.size()) {
                System.out.print("Introduce el nuevo nombre para la lista: ");
                String nuevoNombre = scanner.nextLine();
                if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                    System.out.println("Error: El nombre de la lista no puede estar vacío.");
                } else {
                    playlists.get(indice - 1).nombre = nuevoNombre;
                    guardarPlaylists();
                    System.out.println("Nombre de la lista actualizado a: " + nuevoNombre);
                }
            } else {
                System.out.println("Índice de lista inválido.");
            }
        } else {
            System.out.println("Entrada inválida. Introduce un número.");
            scanner.nextLine();
        }
    }

    public void eliminarListaDeReproduccion(int indiceLista) {
        if (indiceLista > 0 && indiceLista <= playlists.size()) {
            Lista listaAEliminar = playlists.get(indiceLista - 1);
            Scanner scanner = new Scanner(System.in);
            System.out.println("¿Estás seguro que deseas eliminar la lista de reproducción: " + listaAEliminar.nombre + "? (si/no)");
            String respuesta = scanner.nextLine().trim().toLowerCase();

            if (respuesta.equals("si")) {
                Lista listaEliminada = playlists.remove(indiceLista - 1);
                guardarPlaylists();
                System.out.println("Lista de reproducción eliminada: " + listaEliminada.nombre + " (índice: " + indiceLista + ")");
            } else {
                System.out.println("Eliminación de la lista de reproducción cancelada.");
            }
        } else {
            System.out.println("Índice de lista de reproducción inválido.");
        }
    }
}