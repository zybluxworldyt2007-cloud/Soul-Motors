package com.soulmotors.api.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.soulmotors.api.cliente.dto.ClienteDto;
import com.soulmotors.api.cliente.dto.ClienteRequest;
import com.soulmotors.api.common.ConflictoException;
import com.soulmotors.api.common.NoEncontradoException;
import com.soulmotors.api.moto.MotoService;
import com.soulmotors.api.moto.Vehiculo;
import com.soulmotors.api.moto.VehiculoRepository;
import com.soulmotors.api.moto.dto.MotoDto;
import com.soulmotors.api.moto.dto.MotoRequest;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clientes;
    @Mock
    private VehiculoRepository vehiculos;

    private ClienteService clienteService;
    private MotoService motoService;
    private Cliente carlos;

    @BeforeEach
    void setUp() {
        motoService = new MotoService(vehiculos, clientes);
        clienteService = new ClienteService(clientes, vehiculos, motoService);
        carlos = cliente(1L, "10101010", "Carlos Pérez");
    }

    @Test
    void crearNormalizaDocumentoNombreYTelefono() {
        when(clientes.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        ClienteDto dto = clienteService.crear(new ClienteRequest(" 1.020.304 ", "  Ana   Gómez ", "  "));

        assertThat(dto.documento()).isEqualTo("1020304");
        assertThat(dto.nombre()).isEqualTo("Ana Gómez");
        assertThat(dto.telefono()).isNull();
    }

    @Test
    void crearConDocumentoRepetidoEsConflicto() {
        when(clientes.existsByDocumento("10101010")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crear(new ClienteRequest("10101010", "Otro", null)))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("10101010");
        verify(clientes, never()).saveAndFlush(any());
    }

    @Test
    void actualizarConDocumentoDeOtroClienteEsConflicto() {
        when(clientes.findById(1L)).thenReturn(Optional.of(carlos));
        when(clientes.existsByDocumentoAndIdNot("999", 1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.actualizar(1L, new ClienteRequest("999", "Carlos", null)))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void noSeEliminaUnClienteConMotos() {
        when(clientes.findById(1L)).thenReturn(Optional.of(carlos));
        when(vehiculos.countByClienteId(1L)).thenReturn(2L);

        assertThatThrownBy(() -> clienteService.eliminar(1L))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("2 motocicleta");
        verify(clientes, never()).delete(any());
    }

    @Test
    void clienteInexistenteEs404() {
        when(clientes.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtener(9L)).isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void agregarMotoNormalizaLaPlacaYLaAsociaAlCliente() {
        when(clientes.findById(1L)).thenReturn(Optional.of(carlos));
        when(vehiculos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        MotoDto moto = clienteService.agregarMoto(1L, new MotoRequest("abc-12d", "Yamaha", "FZ25", null));

        assertThat(moto.placa()).isEqualTo("ABC12D");
        assertThat(moto.clienteId()).isEqualTo(1L);
    }

    @Test
    void placaRepetidaEsConflicto() {
        when(clientes.findById(1L)).thenReturn(Optional.of(carlos));
        when(vehiculos.existsByPlaca("AAA123")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.agregarMoto(1L, new MotoRequest("aaa 123", "Honda", "CB190", null)))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("AAA123");
    }

    @Test
    void traspasarMotoAOtroCliente() {
        Cliente ana = cliente(2L, "555", "Ana");
        Vehiculo moto = new Vehiculo(carlos, "AAA123", "Yamaha", "FZ25");
        ReflectionTestUtils.setField(moto, "id", 7L);
        when(vehiculos.findWithClienteById(7L)).thenReturn(Optional.of(moto));
        when(clientes.findById(2L)).thenReturn(Optional.of(ana));

        MotoDto dto = motoService.actualizar(7L, new MotoRequest("AAA123", "Yamaha", "FZ25 2024", 2L));

        assertThat(dto.clienteId()).isEqualTo(2L);
        assertThat(dto.modelo()).isEqualTo("FZ25 2024");
    }

    @Test
    void traspasarAClienteInexistenteEs404() {
        Vehiculo moto = new Vehiculo(carlos, "AAA123", "Yamaha", "FZ25");
        when(vehiculos.findWithClienteById(7L)).thenReturn(Optional.of(moto));
        when(clientes.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> motoService.actualizar(7L, new MotoRequest("AAA123", "Yamaha", "FZ25", 99L)))
                .isInstanceOf(NoEncontradoException.class);
    }

    private static Cliente cliente(Long id, String documento, String nombre) {
        Cliente c = new Cliente(documento, nombre, null);
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }
}
