package com.duoc.pixelmeet.api.service;

import com.duoc.pixelmeet.api.dto.UsuarioDto;
import com.duoc.pixelmeet.api.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UsuarioRepository usuarios;

    public AdminService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> usuarios() {
        return usuarios.findAll(Sort.by("id")).stream().map(UsuarioDto::desde).toList();
    }
}
