package com.duoc.pixelmeet.api.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Evento gamer publicado en PixelMeet. */
@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String videojuego;

    /** Tipo o categoria: Torneo, LAN Party, Convencion, Meetup, etc. */
    @Column(nullable = false, length = 40)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Modalidad modalidad;

    @Column(nullable = false, length = 60)
    private String pais;

    /** Ciudad del evento; en los eventos online es la ciudad del organizador. */
    @Column(nullable = false, length = 60)
    private String ciudad;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 200)
    private String resumen;

    @Column(nullable = false, length = 3000)
    private String descripcion;

    /** Direccion fisica (eventos presenciales). */
    @Column(length = 200)
    private String ubicacion;

    /** Enlace de transmision o sala (eventos online). */
    @Column(length = 300)
    private String enlaceAcceso;

    @Column(nullable = false, length = 120)
    private String organizador;

    @Column(nullable = false, length = 1000)
    private String requisitos;

    @Column(nullable = false, length = 1000)
    private String inscripcion;

    @Column(nullable = false, length = 40)
    private String precio;

    private Integer cupos;

    /** Personas interesadas; se usa para ordenar los eventos populares. */
    private int interesados;

    private boolean destacado;

    @Column(nullable = false)
    private LocalDateTime publicadoEn;

    /** Nombres de archivo de las fotografias (servidas por el frontend). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evento_fotos", joinColumns = @JoinColumn(name = "evento_id"))
    @OrderColumn(name = "orden")
    @Column(name = "foto", length = 120)
    private List<String> fotos = new ArrayList<>();

    protected Evento() {
    }

    public Evento(String nombre, String videojuego, String tipo, Modalidad modalidad, String pais, String ciudad,
                  LocalDateTime fechaHora, String resumen, String descripcion, String organizador,
                  String requisitos, String inscripcion, String precio) {
        this.nombre = nombre;
        this.videojuego = videojuego;
        this.tipo = tipo;
        this.modalidad = modalidad;
        this.pais = pais;
        this.ciudad = ciudad;
        this.fechaHora = fechaHora;
        this.resumen = resumen;
        this.descripcion = descripcion;
        this.organizador = organizador;
        this.requisitos = requisitos;
        this.inscripcion = inscripcion;
        this.precio = precio;
        this.publicadoEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getVideojuego() { return videojuego; }
    public String getTipo() { return tipo; }
    public Modalidad getModalidad() { return modalidad; }
    public String getPais() { return pais; }
    public String getCiudad() { return ciudad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getResumen() { return resumen; }
    public String getDescripcion() { return descripcion; }
    public String getUbicacion() { return ubicacion; }
    public String getEnlaceAcceso() { return enlaceAcceso; }
    public String getOrganizador() { return organizador; }
    public String getRequisitos() { return requisitos; }
    public String getInscripcion() { return inscripcion; }
    public String getPrecio() { return precio; }
    public Integer getCupos() { return cupos; }
    public int getInteresados() { return interesados; }
    public boolean isDestacado() { return destacado; }
    public LocalDateTime getPublicadoEn() { return publicadoEn; }
    public List<String> getFotos() { return fotos; }

    // Setters "fluidos" usados por el cargador de datos iniciales.
    public Evento ubicacion(String ubicacion) { this.ubicacion = ubicacion; return this; }
    public Evento enlaceAcceso(String enlaceAcceso) { this.enlaceAcceso = enlaceAcceso; return this; }
    public Evento cupos(Integer cupos) { this.cupos = cupos; return this; }
    public Evento interesados(int interesados) { this.interesados = interesados; return this; }
    public Evento destacado(boolean destacado) { this.destacado = destacado; return this; }
    public Evento publicadoEn(LocalDateTime publicadoEn) { this.publicadoEn = publicadoEn; return this; }
    public Evento fotos(String... fotos) { this.fotos = new ArrayList<>(List.of(fotos)); return this; }
}
