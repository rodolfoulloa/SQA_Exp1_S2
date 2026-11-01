package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Convierte los errores de la API en paginas amigables, sin mostrar detalles internos. */
@ControllerAdvice
public class ApiErrorAdvice {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorAdvice.class);

    @ExceptionHandler(ApiException.class)
    public String manejar(ApiException ex, HttpServletRequest request, HttpServletResponse response, Model model) {
        switch (ex.getTipo()) {
            case NO_AUTENTICADO -> {
                // El JWT vencio o fue rechazado: se cierra la sesion local y se pide login nuevamente.
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();
                return "redirect:/login?expirado";
            }
            case NO_ENCONTRADO -> response.setStatus(404);
            case PROHIBIDO -> response.setStatus(403);
            case VALIDACION -> response.setStatus(400);
            default -> {
                log.warn("Problema al consultar la API: {}", ex.getMessage());
                response.setStatus(503);
            }
        }
        model.addAttribute("status", response.getStatus());
        return "error";
    }
}
