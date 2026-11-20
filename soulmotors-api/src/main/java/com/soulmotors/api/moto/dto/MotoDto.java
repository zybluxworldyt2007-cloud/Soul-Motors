package com.soulmotors.api.moto.dto;

import java.time.LocalDateTime;

import com.soulmotors.api.moto.Vehiculo;

public record MotoDto(Long id, String placa, String marca, String modelo,
                      Long clienteId, String clienteNombre,
                      LocalDateTime creadoAt, LocalDateTime actualizadoAt) {

    public static MotoDto de(Vehiculo v) {
        return new MotoDto(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(),
                v.getCliente().getId(), v.getCliente().getNombre(),
                v.getCreadoAt(), v.getActualizadoAt());
    }
}
