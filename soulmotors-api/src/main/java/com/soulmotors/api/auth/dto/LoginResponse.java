package com.soulmotors.api.auth.dto;

import java.time.Instant;

public record LoginResponse(String token, String tipo, Instant expiraEn, UsuarioDto usuario) {
}
