package com.duoc.pixelmeet.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Registro de un evento compartido en el muro del sitio (con mensaje opcional) o en una red social. */
@Entity
@Table(name = "compartidos")
public class Compartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalCompartir canal;

    @Column(length = 280)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    protected Compartido() {
    }

    public Compartido(Evento evento, Usuario usuario, CanalCompartir canal, String mensaje, LocalDateTime ahora) {
        this.evento = evento;
        this.usuario = usuario;
        this.canal = canal;
        this.mensaje = mensaje;
        this.creadoEn = ahora;
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public Usuario getUsuario() { return usuario; }
    public CanalCompartir getCanal() { return canal; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
