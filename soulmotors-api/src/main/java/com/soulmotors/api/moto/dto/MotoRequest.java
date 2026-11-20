package com.soulmotors.api.moto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos de una motocicleta. Placa colombiana de moto (ABC12D) o formato ABC123 / ABC12;
 * se aceptan espacios o guion y se guarda en mayúsculas sin separadores.
 * clienteId solo se usa en PUT /motos/{id} para traspasar la moto a otro cliente.
 */
public record MotoRequest(
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^\\s*[A-Za-z]{3}[\\s-]?[0-9]{2}[A-Za-z0-9]?\\s*$",
                message = "Placa inválida (ej: ABC12D o ABC123)")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca admite máximo 50 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 50, message = "El modelo admite máximo 50 caracteres")
        String modelo,

        @Positive(message = "clienteId inválido")
        Long clienteId) {
}
