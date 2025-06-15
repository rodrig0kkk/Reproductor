package com.jdkk.musicplayer.view;

public class Prints {

    public static void printMenuPrincipal() {
        System.out.println("\nReproductor de Musica");
        System.out.println("1. Reproducir");
        System.out.println("2. Biblioteca");
        System.out.println("3. Buscar canción");
        System.out.println("4. Listas de reproducción");
        System.out.println("5. Salir");
        System.out.print("Seleccione una opción: ");
    }

    public static void printMenuReproducir() {
        System.out.println("Menú de Reproducción:");
        System.out.println("1. Reproducir/Pausar");
        System.out.println("2. Siguiente");
        System.out.println("3. Anterior");
        System.out.println("4. Reproducir Aleatorio");
        System.out.println("5. Repetir");
        System.out.println("6. Ver Cola");
        System.out.println("7. Salir");
        System.out.print("Seleccione una opción: ");
    }

    public static void printMenuBiblioteca() {
        System.out.println("\nMenu Biblioteca");
        System.out.println("1. Ver biblioteca");
        System.out.println("2. Añadir Canciones");
        System.out.println("3. Eliminar canción");
        System.out.println("4. Lista canciones");
        System.out.println("5. Volver al menu principal");
        System.out.print("Seleccione una opción: ");
    }

    public static void printVerBiblioteca() {
        System.out.println("\nSelecciona el tipo de lista a ver:");
        System.out.println("1. Ver por adición");
        System.out.println("2. Ver por título");
        System.out.println("3. Ver por artista");
        System.out.println("4. Ver por género");
        System.out.println("5. Salir");
        System.out.print("Seleccione una opción: ");
    }

    public static void printListasDeReproduccion() {
        System.out.println("\nMenú Listas de Reproducción");
        System.out.println("1. Ver listas");
        System.out.println("2. Crear nueva lista");
        System.out.println("3. Editar lista");
        System.out.println("4. Eliminar lista");
        System.out.println("5. Volver al menú principal");
        System.out.print("Seleccione una opción: ");
    }

    public static void printVerListasDeReproduccion() {
        System.out.println("\nSelecciona el tipo de lista a ver:");
        System.out.println("1. Ver lista apor adición");
        System.out.println("2. Ver lista por título");
        System.out.println("3. Ver lista por artista");
        System.out.println("4. Ver lista por género");
        System.out.print("Seleccione una opción: ");
    }

    public static void printSeleccionadorDeArchivos() {
        System.out.println("Añadir Canciones");
        System.out.println("1.Escoger Cancion");
        System.out.println("2.Escoger Carpeta");
    }

}
