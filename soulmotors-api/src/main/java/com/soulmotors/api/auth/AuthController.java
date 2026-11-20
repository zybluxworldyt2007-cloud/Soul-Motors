package com.soulmotors.api.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soulmotors.api.auth.dto.LoginRequest;
import com.soulmotors.api.auth.dto.LoginResponse;
import com.soulmotors.api.auth.dto.UsuarioDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/login  {"username": "...", "password": "..."} */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** GET /api/auth/me  — datos del usuario dueño del token. */
    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal Jwt jwt) {
        return authService.usuarioActual(jwt.getSubject());
    }
}
