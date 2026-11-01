package com.duoc.pixelmeet.web.client;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Filtros del formulario de busqueda de eventos. */
public class BusquedaForm {

    private String nombre;
    private String videojuego;
    private String tipo;
    private String pais;
    private String ciudad;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;
    private String modalidad;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = recortar(nombre, 60); }
    public String getVideojuego() { return videojuego; }
    public void setVideojuego(String videojuego) { this.videojuego = recortar(videojuego, 60); }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = recortar(tipo, 40); }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = recortar(pais, 60); }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = recortar(ciudad, 60); }
    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
    public String getModalidad() { return modalidad; }

    /** Solo se aceptan los valores conocidos; cualquier otro se ignora. */
    public void setModalidad(String modalidad) {
        this.modalidad = "PRESENCIAL".equals(modalidad) || "ONLINE".equals(modalidad) ? modalidad : null;
    }

    private static String recortar(String valor, int max) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String v = valor.trim();
        return v.length() > max ? v.substring(0, max) : v;
    }
}
