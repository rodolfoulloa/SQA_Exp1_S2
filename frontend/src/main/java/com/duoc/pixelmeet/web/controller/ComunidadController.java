package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.ApiDtos.Foto;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import com.duoc.pixelmeet.web.security.SesionUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * [Privadas] Acciones de comunidad sobre un evento: fotos y videos, comentarios, valoracion y compartir.
 * Todos los formularios llevan token CSRF y usan Post/Redirect/Get; la API valida y decide los permisos.
 */
@Controller
@RequestMapping("/eventos/{id}")
public class ComunidadController {

    private static final Set<String> IMAGENES = Set.of("image/jpeg", "image/png");

    private final PixelMeetApiClient api;
    private final String urlPublica;

    public ComunidadController(PixelMeetApiClient api, @Value("${app.public-url:}") String urlPublica) {
        this.api = api;
        this.urlPublica = urlPublica;
    }

    @PostMapping("/fotos")
    public String subirFoto(@PathVariable long id, @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                            RedirectAttributes redirect) {
        if (archivo == null || archivo.isEmpty()) {
            return error(id, redirect, "Selecciona una imagen JPG o PNG", "galeria");
        }
        try {
            api.subirFoto(id, archivo.getBytes(), archivo.getOriginalFilename(), SesionUsuario.token());
            return ok(id, redirect, "Foto agregada al evento", "galeria");
        } catch (IOException ex) {
            return error(id, redirect, "No se pudo leer la imagen", "galeria");
        } catch (ApiException ex) {
            return errorApi(id, redirect, ex, "galeria");
        }
    }

    @PostMapping("/videos")
    public String agregarVideo(@PathVariable long id, @RequestParam(defaultValue = "") String url,
                               RedirectAttributes redirect) {
        try {
            api.agregarVideo(id, url, SesionUsuario.token());
            return ok(id, redirect, "Video agregado al evento", "galeria");
        } catch (ApiException ex) {
            return errorApi(id, redirect, ex, "galeria");
        }
    }

    @PostMapping("/media/{mediaId}/eliminar")
    public String eliminarMedia(@PathVariable long id, @PathVariable long mediaId, RedirectAttributes redirect) {
        try {
            api.eliminarMedia(id, mediaId, SesionUsuario.token());
            return ok(id, redirect, "Contenido eliminado", "galeria");
        } catch (ApiException ex) {
            return errorApi(id, redirect, ex, "galeria");
        }
    }

    /** Entrega la foto pedida a la API con el JWT de la sesion. Solo tipos de imagen conocidos. */
    @GetMapping("/media/{mediaId}")
    public ResponseEntity<byte[]> foto(@PathVariable long id, @PathVariable long mediaId) {
        Foto foto = api.foto(id, mediaId, SesionUsuario.token());
        if (foto.contentType() == null || !IMAGENES.contains(foto.contentType())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(foto.datos());
    }

    @PostMapping("/comentarios")
    public String comentar(@PathVariable long id, @RequestParam(defaultValue = "") String texto,
                           RedirectAttributes redirect) {
        try {
            api.comentar(id, texto, SesionUsuario.token());
            return ok(id, redirect, "Comentario publicado", "comentarios");
        } catch (ApiException ex) {
            redirect.addFlashAttribute("textoComentario", texto);
            return errorApi(id, redirect, ex, "comentarios");
        }
    }

    @PostMapping("/comentarios/{comentarioId}/eliminar")
    public String eliminarComentario(@PathVariable long id, @PathVariable long comentarioId,
                                     RedirectAttributes redirect) {
        try {
            api.eliminarComentario(id, comentarioId, SesionUsuario.token());
            return ok(id, redirect, "Comentario eliminado", "comentarios");
        } catch (ApiException ex) {
            return errorApi(id, redirect, ex, "comentarios");
        }
    }

    @PostMapping("/valoracion")
    public String valorar(@PathVariable long id, @RequestParam(required = false) Integer estrellas,
                          RedirectAttributes redirect) {
        try {
            api.valorar(id, estrellas, SesionUsuario.token());
            return ok(id, redirect, "¡Gracias por tu valoración!", "valoracion");
        } catch (ApiException ex) {
            return errorApi(id, redirect, ex, "valoracion");
        }
    }

    /**
     * Compartir en el sitio (muro del inicio) o en una red social. Para redes, se registra en la API y se
     * redirige al sitio de la red con un enlace armado desde una lista blanca (sin redireccion abierta).
     */
    @PostMapping("/compartir")
    public String compartir(@PathVariable long id, @RequestParam(defaultValue = "SITIO") String canal,
                            @RequestParam(required = false) String mensaje, RedirectAttributes redirect) {
        boolean sitio = "SITIO".equals(canal);
        if (!sitio && !RedesSociales.CANALES.containsKey(canal)) {
            return error(id, redirect, "Canal para compartir no permitido", "compartir");
        }
        try {
            api.compartir(id, canal, sitio ? mensaje : null, SesionUsuario.token());
        } catch (ApiException ex) {
            return errorApi(id, redirect, ex, "compartir");
        }
        if (sitio) {
            return ok(id, redirect, "Evento compartido en el muro de PixelMeet", "compartir");
        }
        String nombre = api.detalle(id, SesionUsuario.token()).nombre();
        return "redirect:" + RedesSociales.enlace(canal, urlEvento(id), "Mira este evento en PixelMeet: " + nombre);
    }

    private String urlEvento(long id) {
        String base = urlPublica == null || urlPublica.isBlank()
                ? ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
                : urlPublica.replaceAll("/+$", "");
        return base + "/eventos/" + id;
    }

    private static String ok(long id, RedirectAttributes redirect, String mensaje, String ancla) {
        redirect.addFlashAttribute("mensaje", mensaje);
        return "redirect:/eventos/" + id + "#" + ancla;
    }

    private static String error(long id, RedirectAttributes redirect, String mensaje, String ancla) {
        redirect.addFlashAttribute("error", mensaje);
        return "redirect:/eventos/" + id + "#" + ancla;
    }

    /** Errores de validacion o permisos se muestran en la pagina del evento; los demas van al manejador global. */
    private static String errorApi(long id, RedirectAttributes redirect, ApiException ex, String ancla) {
        switch (ex.getTipo()) {
            case VALIDACION -> {
                Map<String, String> campos = ex.getCampos();
                String detalle = campos == null || campos.isEmpty() ? ex.getMessage()
                        : String.join(" · ", campos.values());
                return error(id, redirect, detalle, ancla);
            }
            case PROHIBIDO -> {
                return error(id, redirect, "No tienes permisos para esta acción", ancla);
            }
            default -> throw ex;
        }
    }
}
