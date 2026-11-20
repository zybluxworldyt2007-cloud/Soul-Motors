package com.soulmotors.api.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soulmotors.api.auth.dto.LoginRequest;
import com.soulmotors.api.auth.dto.LoginResponse;
import com.soulmotors.api.auth.dto.UsuarioDto;
import com.soulmotors.api.usuario.Usuario;
import com.soulmotors.api.usuario.UsuarioRepository;

@Service
public class AuthService {

    static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    /** Hash ficticio para que un usuario inexistente tarde lo mismo que uno real. */
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFicticio = passwordEncoder.encode("usuario-inexistente");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        Usuario usuario = usuarios.findByUsername(username).orElse(null);

        if (usuario == null) {
            passwordEncoder.matches(request.password(), hashFicticio);
            throw new BadCredentialsException(MENSAJE_CREDENCIALES);
        }
        if (!passwordEncoder.matches(request.password(), usuario.getPassword()) || !usuario.isActivo()) {
            throw new BadCredentialsException(MENSAJE_CREDENCIALES);
        }

        JwtService.TokenEmitido token = jwtService.emitir(usuario);
        return new LoginResponse(token.token(), "Bearer", token.expiraEn(), UsuarioDto.de(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioDto usuarioActual(String username) {
        return usuarios.findByUsername(username)
                .filter(Usuario::isActivo)
                .map(UsuarioDto::de)
                .orElseThrow(() -> new BadCredentialsException("Sesión no válida"));
    }
}
