package com.duoc.pixelmeet.api.controller;

import com.duoc.pixelmeet.api.dto.UsuarioDto;
import com.duoc.pixelmeet.api.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** [Solo ADMIN] Ejemplo de autorizacion por rol. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioDto> usuarios() {
        return adminService.usuarios();
    }
}
