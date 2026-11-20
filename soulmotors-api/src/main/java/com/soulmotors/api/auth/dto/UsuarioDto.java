package com.soulmotors.api.auth.dto;

import com.soulmotors.api.usuario.Rol;
import com.soulmotors.api.usuario.Usuario;

public record UsuarioDto(Long id, String nombre, String username, Rol rol) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getUsername(), u.getRol());
    }
}
