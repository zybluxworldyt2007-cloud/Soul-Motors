package com.soulmotors.api.moto;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soulmotors.api.cliente.Cliente;
import com.soulmotors.api.cliente.ClienteRepository;
import com.soulmotors.api.common.ConflictoException;
import com.soulmotors.api.common.NoEncontradoException;
import com.soulmotors.api.common.Normalizar;
import com.soulmotors.api.common.Pagina;
import com.soulmotors.api.moto.dto.MotoDto;
import com.soulmotors.api.moto.dto.MotoRequest;

@Service
public class MotoService {

    private final VehiculoRepository vehiculos;
    private final ClienteRepository clientes;

    public MotoService(VehiculoRepository vehiculos, ClienteRepository clientes) {
        this.vehiculos = vehiculos;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public Pagina<MotoDto> listar(String placa, Pageable pageable) {
        if (placa == null || placa.isBlank()) {
            return Pagina.de(vehiculos.findAllBy(pageable), MotoDto::de);
        }
        return Pagina.de(vehiculos.findByPlacaContaining(Normalizar.placa(placa), pageable), MotoDto::de);
    }

    @Transactional(readOnly = true)
    public MotoDto obtener(Long id) {
        return MotoDto.de(buscar(id));
    }

    /** Registra una moto para un cliente ya cargado (lo usa ClienteService). */
    @Transactional
    public MotoDto crear(Cliente cliente, MotoRequest req) {
        String placa = Normalizar.placa(req.placa());
        if (vehiculos.existsByPlaca(placa)) {
            throw placaRepetida(placa);
        }
        Vehiculo moto = new Vehiculo(cliente, placa, Normalizar.texto(req.marca()), Normalizar.texto(req.modelo()));
        return MotoDto.de(vehiculos.saveAndFlush(moto));
    }

    @Transactional
    public MotoDto actualizar(Long id, MotoRequest req) {
        Vehiculo moto = buscar(id);
        String placa = Normalizar.placa(req.placa());
        if (vehiculos.existsByPlacaAndIdNot(placa, id)) {
            throw placaRepetida(placa);
        }
        if (req.clienteId() != null && !req.clienteId().equals(moto.getCliente().getId())) {
            Cliente nuevo = clientes.findById(req.clienteId())
                    .orElseThrow(() -> new NoEncontradoException("No existe el cliente con id " + req.clienteId()));
            moto.asignarCliente(nuevo);
        }
        moto.actualizar(placa, Normalizar.texto(req.marca()), Normalizar.texto(req.modelo()));
        vehiculos.flush();
        return MotoDto.de(moto);
    }

    @Transactional
    public void eliminar(Long id) {
        vehiculos.delete(buscar(id));
        vehiculos.flush();
    }

    private Vehiculo buscar(Long id) {
        return vehiculos.findWithClienteById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la motocicleta con id " + id));
    }

    private static ConflictoException placaRepetida(String placa) {
        return new ConflictoException("Ya existe una motocicleta con la placa " + placa,
                Map.of("placa", "Placa ya registrada"));
    }
}
