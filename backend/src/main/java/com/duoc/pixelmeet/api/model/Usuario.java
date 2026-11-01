package com.duoc.pixelmeet.api.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cuenta de usuario. La contrasena se guarda solo como hash BCrypt.
 * Incluye los datos del perfil gamer (alias, avatar, juegos y notificaciones).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de inicio de sesion (para los usuarios registrados es su correo). */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Hash BCrypt, nunca la contrasena en texto plano. */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String nombreUsuario;

    @Column(nullable = false, length = 20)
    private String avatar = "avatar-1";

    private boolean notifCorreo = true;
    private boolean notifPush;
    private boolean notifNuevosEventos = true;

    private boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_juegos", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "juego", length = 40)
    private List<String> juegos = new ArrayList<>();

    protected Usuario() {
    }

    public Usuario(String username, String email, String passwordHash, String nombre, String nombreUsuario) {
        this.username = username;
        this.email = email;
        this.password = passwordHash;
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNombre() { return nombre; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public boolean isNotifCorreo() { return notifCorreo; }
    public void setNotifCorreo(boolean notifCorreo) { this.notifCorreo = notifCorreo; }
    public boolean isNotifPush() { return notifPush; }
    public void setNotifPush(boolean notifPush) { this.notifPush = notifPush; }
    public boolean isNotifNuevosEventos() { return notifNuevosEventos; }
    public void setNotifNuevosEventos(boolean notifNuevosEventos) { this.notifNuevosEventos = notifNuevosEventos; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public Set<Rol> getRoles() { return roles; }
    public List<String> getJuegos() { return juegos; }

    public void setJuegos(List<String> juegos) {
        this.juegos.clear();
        this.juegos.addAll(juegos);
    }

    public void agregarRol(Rol rol) {
        roles.add(rol);
    }
}
