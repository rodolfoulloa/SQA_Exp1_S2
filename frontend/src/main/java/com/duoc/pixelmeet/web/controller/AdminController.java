package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import com.duoc.pixelmeet.web.security.SesionUsuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** [Solo ADMIN] Listado de usuarios: ejemplo de autorizacion por rol en frontend y API. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PixelMeetApiClient api;

    public AdminController(PixelMeetApiClient api) {
        this.api = api;
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", api.usuarios(SesionUsuario.token()));
        return "admin-usuarios";
    }
}
