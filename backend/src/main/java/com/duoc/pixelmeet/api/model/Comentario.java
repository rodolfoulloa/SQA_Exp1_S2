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

import java.time.LocalDateTime;

/** Comentario de un usuario sobre un evento. El texto se guarda tal cual y se escapa al mostrarlo. */
@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    protected Comentario() {
    }

    public Comentario(Evento evento, Usuario autor, String texto, LocalDateTime creadoEn) {
        this.evento = evento;
        this.autor = autor;
        this.texto = texto;
        this.creadoEn = creadoEn;
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public Usuario getAutor() { return autor; }
    public String getTexto() { return texto; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
