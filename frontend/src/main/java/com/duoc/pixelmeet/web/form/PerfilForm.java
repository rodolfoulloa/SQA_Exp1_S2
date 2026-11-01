package com.duoc.pixelmeet.web.form;

import com.duoc.pixelmeet.web.client.ApiDtos.Perfil;
import com.duoc.pixelmeet.web.client.ApiDtos.PerfilRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos editables del perfil gamer.
 */
public class PerfilForm {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 20, message = "Debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Solo letras, números y guion bajo")
    private String nombreUsuario;

    @NotBlank(message = "Selecciona un avatar")
    private String avatar;

    @Size(max = 12, message = "Selecciona como máximo 12 juegos")
    private List<String> juegos = new ArrayList<>();

    private boolean notifCorreo;
    private boolean notifPush;
    private boolean notifNuevosEventos;

    public static PerfilForm desde(Perfil perfil) {
        PerfilForm form = new PerfilForm();
        form.setNombreUsuario(perfil.nombreUsuario());
        form.setAvatar(perfil.avatar());
        form.setJuegos(new ArrayList<>(perfil.juegos()));
        form.setNotifCorreo(perfil.notifCorreo());
        form.setNotifPush(perfil.notifPush());
        form.setNotifNuevosEventos(perfil.notifNuevosEventos());
        return form;
    }

    public PerfilRequest aRequest() {
        return new PerfilRequest(nombreUsuario, avatar, juegos == null ? java.util.List.of() : juegos,
                notifCorreo, notifPush, notifNuevosEventos);
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public List<String> getJuegos() { return juegos; }
    public void setJuegos(List<String> juegos) { this.juegos = juegos; }

    public boolean isNotifCorreo() { return notifCorreo; }
    public void setNotifCorreo(boolean notifCorreo) { this.notifCorreo = notifCorreo; }

    public boolean isNotifPush() { return notifPush; }
    public void setNotifPush(boolean notifPush) { this.notifPush = notifPush; }

    public boolean isNotifNuevosEventos() { return notifNuevosEventos; }
    public void setNotifNuevosEventos(boolean notifNuevosEventos) { this.notifNuevosEventos = notifNuevosEventos; }
}
