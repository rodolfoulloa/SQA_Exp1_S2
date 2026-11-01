package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.ApiDtos.RegistroRequest;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import com.duoc.pixelmeet.web.form.RegistroForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Set;

@Controller
public class AuthController {

    private static final Set<String> CAMPOS_REGISTRO = Set.of("nombre", "correo", "password");

    private final PixelMeetApiClient api;

    public AuthController(PixelMeetApiClient api) {
        this.api = api;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form, BindingResult result,
                            Model model) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "noCoincide", "Las contraseñas no coinciden");
        }
        if (result.hasErrors()) {
            return "registro";
        }
        try {
            api.registrar(new RegistroRequest(form.getNombre(), form.getCorreo(), form.getPassword()));
        } catch (ApiException ex) {
            if (ex.getTipo() != ApiException.Tipo.VALIDACION) {
                throw ex;
            }
            ex.getCampos().forEach((campo, msg) -> {
                if (CAMPOS_REGISTRO.contains(campo)) {
                    result.rejectValue(campo, "api", msg);
                }
            });
            if (!result.hasErrors()) {
                result.reject("api", ex.getMessage());
            }
            return "registro";
        }
        return "redirect:/login?registrado";
    }
}
