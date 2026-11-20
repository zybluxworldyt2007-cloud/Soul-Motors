package com.soulmotors.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") @Size(max = 50) String username,
        @NotBlank(message = "La contraseña es obligatoria") @Size(max = 100) String password) {
}
