package com.duoc.pixelmeet.api.service;

import com.duoc.pixelmeet.api.dto.BannerDto;
import com.duoc.pixelmeet.api.dto.BusquedaEventos;
import com.duoc.pixelmeet.api.dto.EventoDetalleDto;
import com.duoc.pixelmeet.api.dto.EventoRequest;
import com.duoc.pixelmeet.api.dto.EventoResumenDto;
import com.duoc.pixelmeet.api.dto.FiltrosDto;
import com.duoc.pixelmeet.api.dto.HomeDto;
import com.duoc.pixelmeet.api.exception.RecursoNoEncontradoException;
import com.duoc.pixelmeet.api.exception.ValidacionException;
import com.duoc.pixelmeet.api.model.Evento;
import com.duoc.pixelmeet.api.model.Modalidad;
import com.duoc.pixelmeet.api.repository.BannerRepository;
import com.duoc.pixelmeet.api.repository.EventoRepository;
import com.duoc.pixelmeet.api.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class EventoService {

    private static final int MAX_TEXTO = 60;

    private final EventoRepository eventos;
    private final BannerRepository banners;
    private final UsuarioRepository usuarios;
    private final Clock clock;

    public EventoService(EventoRepository eventos, BannerRepository banners, UsuarioRepository usuarios,
                         Clock clock) {
        this.eventos = eventos;
        this.banners = banners;
        this.usuarios = usuarios;
        this.clock = clock;
    }

    /** Datos de la pagina de inicio: recientes, populares y avisos comerciales. */
    public HomeDto home(int cantidad) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        PageRequest top = PageRequest.of(0, cantidad);
        return new HomeDto(
                eventos.findByFechaHoraAfterOrderByPublicadoEnDesc(ahora, top).stream()
                        .map(EventoResumenDto::desde).toList(),
                eventos.findByFechaHoraAfterOrderByInteresadosDesc(ahora, top).stream()
                        .map(EventoResumenDto::desde).toList(),
                banners.findByActivoTrueOrderByOrdenAsc().stream().map(BannerDto::desde).toList(),
                List.of());
    }

    /**
     * Busca eventos con filtros opcionales. Se usa la API Criteria de JPA, por lo que todos los
     * valores viajan como parametros de la consulta (sin concatenar SQL).
     */
    public List<EventoResumenDto> buscar(BusquedaEventos f) {
        if (f.desde() != null && f.hasta() != null && f.hasta().isBefore(f.desde())) {
            throw new IllegalArgumentException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        Specification<Evento> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            String nombre = limpiar(f.nombre());
            if (nombre != null) {
                p.add(cb.like(cb.lower(root.get("nombre")), "%" + escaparLike(nombre) + "%", '\\'));
            }
            agregarIgual(p, cb, root.get("videojuego"), f.videojuego());
            agregarIgual(p, cb, root.get("tipo"), f.tipo());
            agregarIgual(p, cb, root.get("pais"), f.pais());
            agregarIgual(p, cb, root.get("ciudad"), f.ciudad());
            if (f.modalidad() != null) {
                p.add(cb.equal(root.get("modalidad"), f.modalidad()));
            }
            if (f.desde() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), f.desde().atStartOfDay()));
            }
            if (f.hasta() != null) {
                p.add(cb.lessThan(root.get("fechaHora"), f.hasta().plusDays(1).atStartOfDay()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return eventos.findAll(spec, Sort.by("fechaHora").ascending()).stream()
                .map(EventoResumenDto::desde).toList();
    }

    public EventoDetalleDto detalle(Long id) {
        return eventos.findById(id)
                .map(EventoDetalleDto::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
    }

    /**
     * Publica un evento a nombre del usuario autenticado. El organizador se toma del token (no de la
     * peticion) y se validan las reglas que dependen de la modalidad y de la fecha actual.
     */
    @Transactional
    public EventoDetalleDto publicar(String username, EventoRequest req) {
        var usuario = usuarios.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (!req.fechaHora().isAfter(ahora)) {
            throw new ValidacionException("fechaHora", "La fecha del evento debe ser futura");
        }
        String ubicacion = recortar(req.ubicacion());
        String enlace = recortar(req.enlaceAcceso());
        if (req.modalidad() == Modalidad.PRESENCIAL && ubicacion == null) {
            throw new ValidacionException("ubicacion", "La ubicación es obligatoria en eventos presenciales");
        }
        if (req.modalidad() == Modalidad.ONLINE && enlace == null) {
            throw new ValidacionException("enlaceAcceso", "El enlace es obligatorio en eventos online");
        }
        Evento evento = new Evento(req.nombre().trim(), req.videojuego().trim(), req.tipo().trim(),
                req.modalidad(), req.pais().trim(), req.ciudad().trim(), req.fechaHora().withSecond(0).withNano(0),
                req.resumen().trim(), req.descripcion().trim(), usuario.getNombre(), req.requisitos().trim(),
                req.inscripcion().trim(), req.precio().trim())
                .ubicacion(ubicacion)
                .enlaceAcceso(enlace)
                .cupos(req.cupos())
                .publicadoEn(ahora)
                .publicadoPor(usuario);
        return EventoDetalleDto.desde(eventos.save(evento));
    }

    private static String recortar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public FiltrosDto filtros() {
        return new FiltrosDto(eventos.videojuegos(), eventos.tipos(), eventos.paises(), eventos.ciudades(),
                Arrays.stream(Modalidad.values()).map(Enum::name).toList());
    }

    private static void agregarIgual(List<Predicate> p, jakarta.persistence.criteria.CriteriaBuilder cb,
                                     jakarta.persistence.criteria.Path<String> campo, String valor) {
        String v = limpiar(valor);
        if (v != null) {
            p.add(cb.equal(cb.lower(campo), v));
        }
    }

    /** Normaliza el texto: recorta, limita el largo y pasa a minusculas. */
    static String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String v = valor.trim();
        if (v.length() > MAX_TEXTO) {
            v = v.substring(0, MAX_TEXTO);
        }
        return v.toLowerCase(Locale.ROOT);
    }

    /** Escapa los comodines de LIKE para que el usuario no pueda inyectarlos. */
    static String escaparLike(String valor) {
        return valor.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
