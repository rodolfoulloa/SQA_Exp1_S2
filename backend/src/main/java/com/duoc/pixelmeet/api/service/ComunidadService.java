package com.duoc.pixelmeet.api.service;

import com.duoc.pixelmeet.api.dto.ComunidadDtos.ComentarioDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.CompartidoDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.CompartirRequest;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.CompartirResultadoDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.EventoComunidadDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.MediaDto;
import com.duoc.pixelmeet.api.dto.ComunidadDtos.ValoracionDto;
import com.duoc.pixelmeet.api.exception.RecursoNoEncontradoException;
import com.duoc.pixelmeet.api.exception.ValidacionException;
import com.duoc.pixelmeet.api.model.CanalCompartir;
import com.duoc.pixelmeet.api.model.Comentario;
import com.duoc.pixelmeet.api.model.Compartido;
import com.duoc.pixelmeet.api.model.Evento;
import com.duoc.pixelmeet.api.model.MediaEvento;
import com.duoc.pixelmeet.api.model.Rol;
import com.duoc.pixelmeet.api.model.TipoMedia;
import com.duoc.pixelmeet.api.model.Usuario;
import com.duoc.pixelmeet.api.model.Valoracion;
import com.duoc.pixelmeet.api.repository.ComentarioRepository;
import com.duoc.pixelmeet.api.repository.CompartidoRepository;
import com.duoc.pixelmeet.api.repository.EventoRepository;
import com.duoc.pixelmeet.api.repository.MediaEventoRepository;
import com.duoc.pixelmeet.api.repository.UsuarioRepository;
import com.duoc.pixelmeet.api.repository.ValoracionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Funcionalidades privadas de comunidad: fotos y videos, comentarios, valoraciones y compartir.
 * El usuario siempre se toma del token; los permisos sobre cada recurso se revisan aqui (no solo por ruta),
 * para evitar acceso a objetos ajenos (IDOR).
 */
@Service
public class ComunidadService {

    static final int MAX_FOTOS = 10;
    static final int MAX_VIDEOS = 5;

    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final MediaEventoRepository media;
    private final ComentarioRepository comentarios;
    private final ValoracionRepository valoraciones;
    private final CompartidoRepository compartidos;
    private final AlmacenFotos almacen;
    private final Clock clock;

    public ComunidadService(EventoRepository eventos, UsuarioRepository usuarios, MediaEventoRepository media,
                            ComentarioRepository comentarios, ValoracionRepository valoraciones,
                            CompartidoRepository compartidos, AlmacenFotos almacen, Clock clock) {
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.media = media;
        this.comentarios = comentarios;
        this.valoraciones = valoraciones;
        this.compartidos = compartidos;
        this.almacen = almacen;
        this.clock = clock;
    }

    // ---------------- Lectura ----------------

    @Transactional(readOnly = true)
    public EventoComunidadDto comunidad(Long eventoId, String username) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        List<MediaEvento> todo = media.findByEventoIdOrderByCreadoEnAsc(eventoId);
        List<MediaDto> fotos = todo.stream().filter(m -> m.getTipo() == TipoMedia.FOTO).map(ComunidadService::dto).toList();
        List<MediaDto> videos = todo.stream().filter(m -> m.getTipo() == TipoMedia.VIDEO).map(ComunidadService::dto).toList();
        List<ComentarioDto> lista = comentarios.findTop50ByEventoIdOrderByCreadoEnDesc(eventoId).stream()
                .map(c -> dto(c, puedeEliminar(c, usuario))).toList();
        return new EventoComunidadDto(eventoId, puedeEditar(evento, usuario), fotos, videos, lista,
                valoracion(eventoId, usuario), compartidos.countByEventoId(eventoId));
    }

    /** Bytes y tipo de una foto. equals/hashCode/toString consideran el contenido del arreglo (java:S6218). */
    public record ContenidoFoto(byte[] datos, String contentType) {

        @Override
        public boolean equals(Object o) {
            return o instanceof ContenidoFoto otra && java.util.Arrays.equals(datos, otra.datos)
                    && Objects.equals(contentType, otra.contentType);
        }

        @Override
        public int hashCode() {
            return 31 * java.util.Arrays.hashCode(datos) + Objects.hashCode(contentType);
        }

        @Override
        public String toString() {
            return "ContenidoFoto[" + (datos == null ? 0 : datos.length) + " bytes, " + contentType + "]";
        }
    }

    @Transactional(readOnly = true)
    public ContenidoFoto foto(Long eventoId, Long mediaId) {
        MediaEvento m = media.findByIdAndEventoId(mediaId, eventoId)
                .filter(x -> x.getTipo() == TipoMedia.FOTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Imagen no encontrada"));
        return new ContenidoFoto(almacen.leer(m.getArchivo()), m.getContentType());
    }

    /** Muro "Compartidos por la comunidad" de la pagina de inicio. */
    @Transactional(readOnly = true)
    public List<CompartidoDto> compartidosRecientes() {
        return compartidos.findTop6ByCanalOrderByCreadoEnDesc(CanalCompartir.SITIO).stream()
                .map(c -> new CompartidoDto(c.getEvento().getId(), c.getEvento().getNombre(),
                        c.getUsuario().getNombreUsuario(), c.getUsuario().getAvatar(), c.getMensaje(),
                        c.getCreadoEn()))
                .toList();
    }

    // ---------------- Fotos y videos (solo quien publico el evento o un ADMIN) ----------------

    @Transactional
    public MediaDto agregarFoto(Long eventoId, String username, MultipartFile archivo) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        exigirEdicion(evento, usuario);
        if (media.countByEventoIdAndTipo(eventoId, TipoMedia.FOTO) >= MAX_FOTOS) {
            throw new ValidacionException("archivo", "El evento ya tiene el máximo de " + MAX_FOTOS + " fotos");
        }
        AlmacenFotos.FotoGuardada guardada = almacen.guardar(archivo);
        // Si la transaccion falla despues de escribir el archivo, se borra para no dejar huerfanos.
        alRevertir(() -> almacen.eliminar(guardada.archivo()));
        MediaEvento m = media.save(MediaEvento.foto(evento, usuario, guardada.archivo(), guardada.contentType(),
                LocalDateTime.now(clock)));
        return dto(m);
    }

    @Transactional
    public MediaDto agregarVideo(Long eventoId, String username, String url) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        exigirEdicion(evento, usuario);
        VideoEnlace.Video video = VideoEnlace.reconocer(url).orElseThrow(() -> new ValidacionException("url",
                "Usa un enlace https de YouTube o Vimeo"));
        if (media.countByEventoIdAndTipo(eventoId, TipoMedia.VIDEO) >= MAX_VIDEOS) {
            throw new ValidacionException("url", "El evento ya tiene el máximo de " + MAX_VIDEOS + " videos");
        }
        MediaEvento m = media.save(MediaEvento.video(evento, usuario, video.plataforma(), video.id(),
                LocalDateTime.now(clock)));
        return dto(m);
    }

    @Transactional
    public void eliminarMedia(Long eventoId, Long mediaId, String username) {
        Evento evento = evento(eventoId);
        exigirEdicion(evento, usuario(username));
        MediaEvento m = media.findByIdAndEventoId(mediaId, eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado"));
        media.delete(m);
        if (m.getTipo() == TipoMedia.FOTO) {
            String archivo = m.getArchivo();
            alConfirmar(() -> almacen.eliminar(archivo));
        }
    }

    // ---------------- Comentarios y valoraciones ----------------

    @Transactional
    public ComentarioDto comentar(Long eventoId, String username, String texto) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        String limpio = texto.strip();
        if (limpio.isEmpty()) {
            throw new ValidacionException("texto", "El comentario no puede estar vacío");
        }
        Comentario c = comentarios.save(new Comentario(evento, usuario, limpio, LocalDateTime.now(clock)));
        return dto(c, true);
    }

    @Transactional
    public void eliminarComentario(Long eventoId, Long comentarioId, String username) {
        Usuario usuario = usuario(username);
        Comentario c = comentarios.findByIdAndEventoId(comentarioId, eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comentario no encontrado"));
        if (!puedeEliminar(c, usuario)) {
            throw new AccessDeniedException("Solo el autor o un administrador pueden eliminar el comentario");
        }
        comentarios.delete(c);
    }

    @Transactional
    public ValoracionDto valorar(Long eventoId, String username, int estrellas) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        LocalDateTime ahora = LocalDateTime.now(clock);
        valoraciones.findByEventoIdAndUsuarioId(eventoId, usuario.getId())
                .ifPresentOrElse(v -> v.actualizar(estrellas, ahora),
                        () -> valoraciones.save(new Valoracion(evento, usuario, estrellas, ahora)));
        valoraciones.flush();
        return valoracion(eventoId, usuario);
    }

    // ---------------- Compartir ----------------

    @Transactional
    public CompartirResultadoDto compartir(Long eventoId, String username, CompartirRequest req) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        String mensaje = null;
        if (req.canal() == CanalCompartir.SITIO && req.mensaje() != null && !req.mensaje().isBlank()) {
            mensaje = req.mensaje().strip();
        }
        compartidos.save(new Compartido(evento, usuario, req.canal(), mensaje, LocalDateTime.now(clock)));
        return new CompartirResultadoDto(compartidos.countByEventoId(eventoId));
    }

    // ---------------- Auxiliares ----------------

    private Evento evento(Long id) {
        return eventos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
    }

    private Usuario usuario(String username) {
        return usuarios.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private static boolean esAdmin(Usuario u) {
        return u.getRoles().stream().anyMatch(r -> Rol.ADMIN.equals(r.getNombre()));
    }

    static boolean puedeEditar(Evento evento, Usuario usuario) {
        return esAdmin(usuario) || (evento.getPublicadoPor() != null
                && Objects.equals(evento.getPublicadoPor().getId(), usuario.getId()));
    }

    private static void exigirEdicion(Evento evento, Usuario usuario) {
        if (!puedeEditar(evento, usuario)) {
            throw new AccessDeniedException("Solo quien publicó el evento puede agregar o quitar fotos y videos");
        }
    }

    private static boolean puedeEliminar(Comentario c, Usuario usuario) {
        return esAdmin(usuario) || Objects.equals(c.getAutor().getId(), usuario.getId());
    }

    private ValoracionDto valoracion(Long eventoId, Usuario usuario) {
        long total = valoraciones.countByEventoId(eventoId);
        double promedio = total == 0 ? 0 : Math.round(valoraciones.promedio(eventoId) * 10) / 10.0;
        Integer mia = valoraciones.findByEventoIdAndUsuarioId(eventoId, usuario.getId())
                .map(Valoracion::getEstrellas).orElse(null);
        return new ValoracionDto(promedio, total, mia);
    }

    private static MediaDto dto(MediaEvento m) {
        return new MediaDto(m.getId(), m.getTipo().name(), m.getPlataforma(), m.getVideoId(),
                m.getSubidoPor().getNombreUsuario(), m.getCreadoEn());
    }

    private static ComentarioDto dto(Comentario c, boolean puedeEliminar) {
        return new ComentarioDto(c.getId(), c.getAutor().getNombreUsuario(), c.getAutor().getAvatar(),
                c.getTexto(), c.getCreadoEn(), puedeEliminar);
    }

    private static void alRevertir(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        accion.run();
                    }
                }
            });
        }
    }

    private static void alConfirmar(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }
}
