package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.ApiDtos.Perfil;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import com.duoc.pixelmeet.web.form.Catalogos;
import com.duoc.pixelmeet.web.form.PerfilForm;
import com.duoc.pixelmeet.web.security.SesionUsuario;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/** [Privada] Perfil del usuario: se lee y actualiza en la API usando el JWT de la sesion. */
@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private static final Set<String> CAMPOS = Set.of("nombreUsuario", "avatar", "juegos");

    private final PixelMeetApiClient api;

    public PerfilController(PixelMeetApiClient api) {
        this.api = api;
    }

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("juegosDisponibles", Catalogos.JUEGOS);
        model.addAttribute("avatares", Catalogos.AVATARES);
    }

    @GetMapping
    public String ver(Model model) {
        Perfil perfil = api.perfil(SesionUsuario.token());
        model.addAttribute("perfil", perfil);
        model.addAttribute("perfilForm", PerfilForm.desde(perfil));
        return "perfil";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("perfilForm") PerfilForm form, BindingResult result,
                          Model model, RedirectAttributes redirect) {
        String token = SesionUsuario.token();
        if (!result.hasErrors()) {
            try {
                api.actualizarPerfil(form.aRequest(), token);
                redirect.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
                return "redirect:/perfil";
            } catch (ApiException ex) {
                if (ex.getTipo() != ApiException.Tipo.VALIDACION) {
                    throw ex;
                }
                ex.getCampos().forEach((campo, msg) -> {
                    if (CAMPOS.contains(campo)) {
                        result.rejectValue(campo, "api", msg);
                    }
                });
                if (!result.hasErrors()) {
                    result.reject("api", ex.getMessage());
                }
            }
        }
        model.addAttribute("perfil", api.perfil(token));
        return "perfil";
    }
}
