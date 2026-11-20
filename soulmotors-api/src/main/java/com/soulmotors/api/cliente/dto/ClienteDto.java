package com.soulmotors.api.cliente.dto;

import java.time.LocalDateTime;

import com.soulmotors.api.cliente.Cliente;

public record ClienteDto(Long id, String documento, String nombre, String telefono,
                         LocalDateTime creadoAt, LocalDateTime actualizadoAt) {

    public static ClienteDto de(Cliente c) {
        return new ClienteDto(c.getId(), c.getDocumento(), c.getNombre(), c.getTelefono(),
                c.getCreadoAt(), c.getActualizadoAt());
    }
}
