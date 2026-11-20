package com.soulmotors.api.cliente.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.soulmotors.api.cliente.Cliente;
import com.soulmotors.api.moto.dto.MotoDto;

/** Cliente con sus motocicletas (panel de motos asociadas). */
public record ClienteDetalleDto(Long id, String documento, String nombre, String telefono,
                                LocalDateTime creadoAt, LocalDateTime actualizadoAt,
                                List<MotoDto> motos) {

    public static ClienteDetalleDto de(Cliente c, List<MotoDto> motos) {
        return new ClienteDetalleDto(c.getId(), c.getDocumento(), c.getNombre(), c.getTelefono(),
                c.getCreadoAt(), c.getActualizadoAt(), motos);
    }
}
