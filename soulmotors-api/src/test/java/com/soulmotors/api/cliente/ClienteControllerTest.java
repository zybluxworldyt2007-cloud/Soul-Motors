package com.soulmotors.api.cliente;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.soulmotors.api.cliente.dto.ClienteDto;
import com.soulmotors.api.common.ConflictoException;
import com.soulmotors.api.config.CorsConfig;
import com.soulmotors.api.config.JwtConfig;
import com.soulmotors.api.config.SecurityConfig;
import com.soulmotors.api.moto.dto.MotoDto;

@WebMvcTest(controllers = ClienteController.class,
        properties = "app.jwt.secret=clave-de-pruebas-de-al-menos-32-caracteres")
@Import({SecurityConfig.class, JwtConfig.class, CorsConfig.class})
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    void sinTokenDa401() throws Exception {
        mockMvc.perform(get("/clientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void crearClienteDevuelve201ConLocation() throws Exception {
        when(service.crear(any())).thenReturn(new ClienteDto(5L, "123456", "Ana", null, null, null));

        mockMvc.perform(post("/clientes").with(rol("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"123456\",\"nombre\":\"Ana\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clientes/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void datosInvalidosDan400ConCampos() throws Exception {
        mockMvc.perform(post("/clientes").with(rol("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"\",\"nombre\":\"\",\"telefono\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.documento").exists())
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.telefono").exists());
        verify(service, never()).crear(any());
    }

    @Test
    void placaInvalidaDa400() throws Exception {
        mockMvc.perform(post("/clientes/1/motos").with(rol("MECANICO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"12345\",\"marca\":\"Yamaha\",\"modelo\":\"FZ\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.placa").exists());
    }

    @Test
    void placaRepetidaDa409() throws Exception {
        when(service.agregarMoto(anyLong(), any()))
                .thenThrow(new ConflictoException("Ya existe una motocicleta con la placa AAA123",
                        Map.of("placa", "Placa ya registrada")));

        mockMvc.perform(post("/clientes/1/motos").with(rol("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"AAA123\",\"marca\":\"Yamaha\",\"modelo\":\"FZ25\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.placa").value("Placa ya registrada"));
    }

    @Test
    void agregarMotoDevuelve201() throws Exception {
        when(service.agregarMoto(anyLong(), any()))
                .thenReturn(new MotoDto(9L, "ABC12D", "Yamaha", "FZ25", 1L, "Carlos", null, null));

        mockMvc.perform(post("/clientes/1/motos").with(rol("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"abc-12d\",\"marca\":\"Yamaha\",\"modelo\":\"FZ25\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/motos/9"));
    }

    @Test
    void vendedorNoPuedeEliminarClientes() throws Exception {
        mockMvc.perform(delete("/clientes/1").with(rol("VENDEDOR")))
                .andExpect(status().isForbidden());
        verify(service, never()).eliminar(anyLong());
    }

    @Test
    void administradorEliminaCliente() throws Exception {
        mockMvc.perform(delete("/clientes/1").with(rol("ADMINISTRADOR")))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }

    @Test
    void idNoNumericoDa400() throws Exception {
        mockMvc.perform(get("/clientes/abc").with(rol("VENDEDOR")))
                .andExpect(status().isBadRequest());
    }

    private static RequestPostProcessor rol(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}
