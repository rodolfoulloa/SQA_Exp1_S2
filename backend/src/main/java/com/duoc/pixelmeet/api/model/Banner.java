package com.duoc.pixelmeet.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Aviso comercial que se muestra en la pagina de inicio. */
@Entity
@Table(name = "banners")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 200)
    private String mensaje;

    @Column(nullable = false, length = 40)
    private String llamado;

    /** Nombre del archivo de imagen (servido por el frontend). */
    @Column(nullable = false, length = 80)
    private String imagen;

    private boolean activo = true;

    private int orden;

    protected Banner() {
    }

    public Banner(String marca, String mensaje, String llamado, String imagen, int orden) {
        this.marca = marca;
        this.mensaje = mensaje;
        this.llamado = llamado;
        this.imagen = imagen;
        this.orden = orden;
    }

    public Long getId() { return id; }
    public String getMarca() { return marca; }
    public String getMensaje() { return mensaje; }
    public String getLlamado() { return llamado; }
    public String getImagen() { return imagen; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public int getOrden() { return orden; }
}
