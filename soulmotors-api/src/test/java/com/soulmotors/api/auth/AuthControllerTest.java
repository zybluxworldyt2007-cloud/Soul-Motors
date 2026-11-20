package com.soulmotors.api.auth;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.soulmotors.api.config.CorsConfig;
import com.soulmotors.api.config.JwtConfig;
import com.soulmotors.api.config.SecurityConfig;
import com.soulmotors.api.usuario.Rol;
import com.soulmotors.api.usuario.Usuario;
import com.soulmotors.api.usuario.UsuarioRepository;

@WebMvcTest(controllers = AuthController.class,
        properties = "app.jwt.secret=clave-de-pruebas-de-al-menos-32-caracteres")
@Import({SecurityConfig.class, JwtConfig.class, CorsConfig.class, JwtService.class, AuthService.class})
class AuthControllerTest {

    private static final String HASH = new BCryptPasswordEncoder().encode("SoulMotors2026*");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarios;

    private Usuario mecanico;

    @BeforeEach
    void setUp() {
        mecanico = usuario(2L, "Mecánico Juan", Rol.MECANICO, "empleado1", true);
        when(usuarios.findByUsername(anyString())).thenReturn(Optional.empty());
        when(usuarios.findByUsername("empleado1")).thenReturn(Optional.of(mecanico));
    }

    @Test
    void loginCorrectoDevuelveTokenYRol() throws Exception {
        mockMvc.perform(login("empleado1", "SoulMotors2026*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(not(emptyString())))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("MECANICO"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    void contrasenaIncorrectaDa401() throws Exception {
        mockMvc.perform(login("empleado1", "otra"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(AuthService.MENSAJE_CREDENCIALES));
    }

    @Test
    void usuarioInexistenteDaElMismo401() throws Exception {
        mockMvc.perform(login("nadie", "SoulMotors2026*"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(AuthService.MENSAJE_CREDENCIALES));
    }

    @Test
    void usuarioInactivoNoPuedeEntrar() throws Exception {
        ReflectionTestUtils.setField(mecanico, "activo", false);
        mockMvc.perform(login("empleado1", "SoulMotors2026*"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void camposVaciosDan400() throws Exception {
        mockMvc.perform(login("", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.username").exists())
                .andExpect(jsonPath("$.campos.password").exists());
    }

    @Test
    void meSinTokenDa401() throws Exception {
        mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void meConTokenFalsoDa401() throws Exception {
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meConTokenValidoDevuelveElUsuario() throws Exception {
        String body = mockMvc.perform(login("empleado1", "SoulMotors2026*"))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.token");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("empleado1"))
                .andExpect(jsonPath("$.rol").value("MECANICO"));
    }

    private static org.springframework.test.web.servlet.RequestBuilder login(String user, String pass) {
        return post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}");
    }

    private static Usuario usuario(Long id, String nombre, Rol rol, String username, boolean activo) {
        Usuario u = new Usuario(nombre, rol, username, HASH);
        ReflectionTestUtils.setField(u, "id", id);
        ReflectionTestUtils.setField(u, "activo", activo);
        return u;
    }
}
