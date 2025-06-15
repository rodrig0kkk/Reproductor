package com.softcorp.musicplayer;

import com.softcorp.musicplayer.controller.MenuPrincipal;

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
