package com.jdkk.musicplayer.model;

import javafx.application.Platform;
import java.util.List;
import java.util.Scanner;
import java.io.File;

public class ColaDeReproduccion {

    private final Cancion reproductor;
    private final Scanner scanner;
    private final Biblioteca biblioteca;

    public ColaDeReproduccion(Cancion reproductor, Scanner scanner, Biblioteca biblioteca) {
        this.reproductor = reproductor;
        this.scanner = scanner;
        this.biblioteca = biblioteca;
    }

    public void mostrarCola() {
        List<File> listaCanciones = reproductor.getListaCanciones();
        do {
            System.out.println("Cola de Reproducción:");
            if (listaCanciones.isEmpty()) {
                System.out.println("La cola está vacía.");
            } else {
                for (int i = 0; i < listaCanciones.size(); i++) {
                    System.out.println((i + 1) + ". " + listaCanciones.get(i).getName());
                }
            }

            System.out.println("\nOpciones:");
            System.out.println("1. Reproducir canción");
            System.out.println("2. Agregar canción a la cola");
            System.out.println("3. Quitar canción de la cola");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = scanner.nextInt();
                scanner.nextLine(); // Limpiar buffer

                switch (opcion) {
                    case 1:
                        if (listaCanciones.isEmpty()) {
                            System.out.println("No hay canciones en la cola.");
                            break;
                        }
                        System.out.print("Ingrese el número de la canción que desea reproducir: ");
                        int seleccion = scanner.nextInt();
                        scanner.nextLine();
                        if (seleccion > 0 && seleccion <= listaCanciones.size()) {
                            Platform.runLater(() -> {
                                reproductor.stopSong();
                                reproductor.playSong(seleccion - 1);
                            });
                        } else {
                            System.out.println("Número inválido.");
                        }
                        break;

                    case 2:
                        System.out.println("Lista de canciones disponibles:");
                        List<File> bibliotecaCanciones = biblioteca.getListaCanciones();
                        for (int i = 0; i < bibliotecaCanciones.size(); i++) {
                            System.out.println((i + 1) + ". " + bibliotecaCanciones.get(i).getName());
                        }
                        System.out.print("Ingrese el número de la canción a agregar (0 para cancelar): ");
                        int addIndex = scanner.nextInt();
                        scanner.nextLine();
                        if (addIndex > 0 && addIndex <= bibliotecaCanciones.size()) {
                            File songToAdd = bibliotecaCanciones.get(addIndex - 1);
                            if (listaCanciones.contains(songToAdd)) {
                                System.out.println("La canción '" + songToAdd.getName() + "' ya está en la cola.");
                            } else {
                                listaCanciones.add(songToAdd);
                                System.out.println("Canción '" + songToAdd.getName() + "' añadida a la cola.");
                            }
                        } else if (addIndex != 0) {
                            System.out.println("Número inválido.");
                        }
                        break;

                    case 3:
                        if (listaCanciones.isEmpty()) {
                            System.out.println("No hay canciones en la cola para quitar.");
                            break;
                        }
                        System.out.print("Ingrese el número de la canción a quitar (0 para cancelar): ");
                        int removeIndex = scanner.nextInt();
                        scanner.nextLine();
                        if (removeIndex > 0 && removeIndex <= listaCanciones.size()) {
                            File removedSong = listaCanciones.remove(removeIndex - 1);
                            System.out.println("Canción '" + removedSong.getName() + "' eliminada de la cola.");
                        } else if (removeIndex != 0) {
                            System.out.println("Número inválido.");
                        }
                        break;

                    case 0:
                        return;

                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer
            }
        } while (true);
    }
}