package com.duoc.pixelmeet.web.form;

import com.duoc.pixelmeet.web.client.ApiDtos.EventoRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Formulario para publicar un evento. Replica las validaciones de la API para dar feedback inmediato;
 * la API vuelve a validar todo (nunca se confia solo en la validacion del cliente).
 */
public class EventoForm {

    private static final String TEXTO_GEO = "^[\\p{L} .'-]*$";

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "Debe tener entre 3 y 120 caracteres")
    private String nombre;

    @NotBlank(message = "El videojuego es obligatorio")
    @Size(min = 2, max = 60, message = "Debe tener entre 2 y 60 caracteres")
    private String videojuego;

    @NotBlank(message = "El tipo es obligatorio")
    @Size(min = 2, max = 40, message = "Debe tener entre 2 y 40 caracteres")
    private String tipo;

    @NotBlank(message = "Selecciona la modalidad")
    @Pattern(regexp = "^(PRESENCIAL|ONLINE)$", message = "Modalidad no válida")
    private String modalidad = "PRESENCIAL";

    @NotBlank(message = "El país es obligatorio")
    @Size(min = 2, max = 60, message = "Debe tener entre 2 y 60 caracteres")
    @Pattern(regexp = TEXTO_GEO, message = "Solo letras y espacios")
    private String pais;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(min = 2, max = 60, message = "Debe tener entre 2 y 60 caracteres")
    @Pattern(regexp = TEXTO_GEO, message = "Solo letras y espacios")
    private String ciudad;

    @NotNull(message = "La fecha y hora son obligatorias")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHora;

    @NotBlank(message = "El resumen es obligatorio")
    @Size(min = 10, max = 200, message = "Debe tener entre 10 y 200 caracteres")
    private String resumen;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 3000, message = "Debe tener entre 10 y 3000 caracteres")
    private String descripcion;

    @Size(max = 200, message = "No puede superar 200 caracteres")
    private String ubicacion;

    @Size(max = 300, message = "No puede superar 300 caracteres")
    @Pattern(regexp = "^$|^https://[^\\s<>\"']+$", message = "Debe comenzar con https://")
    private String enlaceAcceso;

    @NotBlank(message = "Los requisitos son obligatorios")
    @Size(max = 1000, message = "No puede superar 1000 caracteres")
    private String requisitos;

    @NotBlank(message = "La inscripción es obligatoria")
    @Size(max = 1000, message = "No puede superar 1000 caracteres")
    private String inscripcion;

    @NotBlank(message = "El precio es obligatorio")
    @Size(max = 40, message = "No puede superar 40 caracteres")
    private String precio;

    @Min(value = 1, message = "Debe ser al menos 1")
    @Max(value = 100000, message = "No puede superar 100.000")
    private Integer cupos;

    public EventoRequest aRequest() {
        return new EventoRequest(nombre, videojuego, tipo, modalidad, pais, ciudad, fechaHora, resumen,
                descripcion, ubicacion, enlaceAcceso, requisitos, inscripcion, precio, cupos);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getVideojuego() { return videojuego; }
    public void setVideojuego(String videojuego) { this.videojuego = videojuego; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getEnlaceAcceso() { return enlaceAcceso; }
    public void setEnlaceAcceso(String enlaceAcceso) { this.enlaceAcceso = enlaceAcceso; }
    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }
    public String getInscripcion() { return inscripcion; }
    public void setInscripcion(String inscripcion) { this.inscripcion = inscripcion; }
    public String getPrecio() { return precio; }
    public void setPrecio(String precio) { this.precio = precio; }
    public Integer getCupos() { return cupos; }
    public void setCupos(Integer cupos) { this.cupos = cupos; }
}
