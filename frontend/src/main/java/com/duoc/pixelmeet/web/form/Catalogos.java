package com.duoc.pixelmeet.web.form;

import java.util.List;

/** Opciones que muestra el formulario de perfil (la API vuelve a validarlas con su lista blanca). */
public final class Catalogos {

    public static final List<String> JUEGOS = List.of(
            "League of Legends", "Valorant", "Counter-Strike 2", "Fortnite", "Minecraft",
            "Rocket League", "Street Fighter 6", "Tekken 8", "EA Sports FC", "Genshin Impact",
            "Pokémon", "Super Smash Bros.");

    public static final List<String> AVATARES = List.of("avatar-1", "avatar-2", "avatar-3", "avatar-4");

    private Catalogos() {
    }
}
