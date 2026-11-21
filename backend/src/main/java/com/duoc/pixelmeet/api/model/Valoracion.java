package com.duoc.pixelmeet.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/** Valoracion de 1 a 5 estrellas. La restriccion unica asegura un solo voto por usuario y evento. */
@Entity
@Table(name = "valoraciones",
        uniqueConstraints = @UniqueConstraint(name = "uk_valoracion_evento_usuario",
                columnNames = {"evento_id", "usuario_id"}))
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private int estrellas;

    @Column(nullable = false)
    private LocalDateTime actualizadoEn;

    protected Valoracion() {
    }

    public Valoracion(Evento evento, Usuario usuario, int estrellas, LocalDateTime ahora) {
        this.evento = evento;
        this.usuario = usuario;
        this.estrellas = estrellas;
        this.actualizadoEn = ahora;
    }

    public void actualizar(int estrellas, LocalDateTime ahora) {
        this.estrellas = estrellas;
        this.actualizadoEn = ahora;
    }

    public Long getId() { return id; }
    public int getEstrellas() { return estrellas; }
}
