package com.duoc.pixelmeet.api.controller;

import com.duoc.pixelmeet.api.dto.ComunidadDtos.ComentarioDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.ComentarioRequest;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.CompartirRequest;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.CompartirResultadoDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.EventoComunidadDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.MediaDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.ValoracionDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.ValoracionRequest;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.VideoRequest;
import com.duoc.pixelmeet.api.service.ComunidadService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/**
 * [Privadas] Fotos y videos, comentarios, valoraciones y compartir eventos.
 * Todas requieren JWT; las que modifican datos requieren ademas token CSRF.
 */
@RestController
@RequestMapping("/api/eventos/{id}")
public class ComunidadController {

    private final ComunidadService comunidad;

    public ComunidadController(ComunidadService comunidad) {
        this.comunidad = comunidad;
    }

    @GetMapping("/comunidad")
    public EventoComunidadDto ver(@PathVariable Long id, Authentication auth) {
        return comunidad.comunidad(id, auth.getName());
    }

    @PostMapping(path = "/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaDto subirFoto(@PathVariable Long id, @RequestPart("archivo") MultipartFile archivo,
                              Authentication auth) {
        return comunidad.agregarFoto(id, auth.getName(), archivo);
    }

    @PostMapping("/videos")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaDto agregarVideo(@PathVariable Long id, @Valid @RequestBody VideoRequest req,
                                 Authentication auth) {
        return comunidad.agregarVideo(id, auth.getName(), req.url());
    }

    @DeleteMapping("/media/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMedia(@PathVariable Long id, @PathVariable Long mediaId, Authentication auth) {
        comunidad.eliminarMedia(id, mediaId, auth.getName());
    }

    /** Bytes de la foto. Content-Type fijado por el servidor y cache solo privada (el contenido es privado). */
    @GetMapping("/media/{mediaId}/contenido")
    public ResponseEntity<byte[]> foto(@PathVariable Long id, @PathVariable Long mediaId) {
        ComunidadService.ContenidoFoto foto = comunidad.foto(id, mediaId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename("foto-" + mediaId + (foto.contentType().endsWith("png") ? ".png" : ".jpg"))
                        .build().toString())
                .body(foto.datos());
    }

    @PostMapping("/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioDto comentar(@PathVariable Long id, @Valid @RequestBody ComentarioRequest req,
                                  Authentication auth) {
        return comunidad.comentar(id, auth.getName(), req.texto());
    }

    @DeleteMapping("/comentarios/{comentarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarComentario(@PathVariable Long id, @PathVariable Long comentarioId, Authentication auth) {
        comunidad.eliminarComentario(id, comentarioId, auth.getName());
    }

    @PutMapping("/valoracion")
    public ValoracionDto valorar(@PathVariable Long id, @Valid @RequestBody ValoracionRequest req,
                                 Authentication auth) {
        return comunidad.valorar(id, auth.getName(), req.estrellas());
    }

    @PostMapping("/compartir")
    public CompartirResultadoDto compartir(@PathVariable Long id, @Valid @RequestBody CompartirRequest req,
                                           Authentication auth) {
        return comunidad.compartir(id, auth.getName(), req);
    }
}
