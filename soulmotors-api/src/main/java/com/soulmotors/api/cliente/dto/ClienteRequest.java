package com.soulmotors.api.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "El documento es obligatorio")
        @Pattern(regexp = "^\\s*[0-9A-Za-z][0-9A-Za-z.\\-\\s]{3,24}\\s*$",
                message = "Documento inválido (solo números, letras, puntos o guion)")
        String documento,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String nombre,

        @Pattern(regexp = "^\\s*$|^\\s*\\+?[0-9][0-9\\s-]{5,18}\\s*$",
                message = "Teléfono inválido")
        String telefono) {
}
