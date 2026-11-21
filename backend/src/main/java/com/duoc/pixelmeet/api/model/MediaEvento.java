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

/**
 * Foto subida o video enlazado de un evento. De las fotos se guarda solo el nombre aleatorio del
 * archivo; de los videos, la plataforma y el identificador, nunca la URL entregada por el usuario.
 */
@Entity
@Table(name = "evento_media")
public class MediaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMedia tipo;

    /** Nombre del archivo en disco (UUID + extension). Solo fotos. */
    @Column(length = 60)
    private String archivo;

    @Column(length = 20)
    private String contentType;

    /** YOUTUBE o VIMEO. Solo videos. */
    @Column(length = 10)
    private String plataforma;

    @Column(length = 20)
    private String videoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subido_por_id")
    private Usuario subidoPor;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    protected MediaEvento() {
    }

    public static MediaEvento foto(Evento evento, Usuario usuario, String archivo, String contentType,
                                   LocalDateTime ahora) {
        MediaEvento m = new MediaEvento();
        m.evento = evento;
        m.tipo = TipoMedia.FOTO;
        m.archivo = archivo;
        m.contentType = contentType;
        m.subidoPor = usuario;
        m.creadoEn = ahora;
        return m;
    }

    public static MediaEvento video(Evento evento, Usuario usuario, String plataforma, String videoId,
                                    LocalDateTime ahora) {
        MediaEvento m = new MediaEvento();
        m.evento = evento;
        m.tipo = TipoMedia.VIDEO;
        m.plataforma = plataforma;
        m.videoId = videoId;
        m.subidoPor = usuario;
        m.creadoEn = ahora;
        return m;
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public TipoMedia getTipo() { return tipo; }
    public String getArchivo() { return archivo; }
    public String getContentType() { return contentType; }
    public String getPlataforma() { return plataforma; }
    public String getVideoId() { return videoId; }
    public Usuario getSubidoPor() { return subidoPor; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
