package com.jdkk.musicplayer;

import com.jdkk.musicplayer.controller.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Reproductor iniciando");
            MenuPrincipal.launch(MenuPrincipal.class, args);
        } catch (Exception e) {
            e.printStackTrace();  // <-- this will reveal if JavaFX crashes
        }
    }
}
