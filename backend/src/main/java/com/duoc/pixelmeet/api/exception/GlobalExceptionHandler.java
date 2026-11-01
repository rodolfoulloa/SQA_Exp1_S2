package com.duoc.pixelmeet.api.exception;

import com.duoc.pixelmeet.api.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones a respuestas JSON homogeneas.
 * Los errores inesperados se registran en el log y al cliente solo se le entrega un mensaje generico.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> campos.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        ErrorDto body = new ErrorDto(Instant.now(), 400, "Bad Request", "Datos inválidos",
                req.getRequestURI(), campos);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorDto> solicitudInvalida(Exception ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida", req);
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErrorDto> credenciales(Exception ex, HttpServletRequest req) {
        // Mismo mensaje para usuario inexistente o clave incorrecta (evita enumerar usuarios).
        return respuesta(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permisos para este recurso", req);
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    public ResponseEntity<ErrorDto> demasiadosIntentos(DemasiadosIntentosException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDto> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> rutaNoExiste(NoResourceFoundException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado", req);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorDto> conflicto(ConflictoException ex, HttpServletRequest req) {
        ErrorDto body = new ErrorDto(Instant.now(), 409, "Conflict", ex.getMessage(), req.getRequestURI(),
                Map.of(ex.getCampo(), ex.getMessage()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", req);
    }

    private ResponseEntity<ErrorDto> respuesta(HttpStatus status, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorDto.de(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI()));
    }
}
