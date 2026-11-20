package com.soulmotors.api.cliente;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soulmotors.api.cliente.dto.ClienteDetalleDto;
import com.soulmotors.api.cliente.dto.ClienteDto;
import com.soulmotors.api.cliente.dto.ClienteRequest;
import com.soulmotors.api.common.ConflictoException;
import com.soulmotors.api.common.NoEncontradoException;
import com.soulmotors.api.common.Normalizar;
import com.soulmotors.api.common.Pagina;
import com.soulmotors.api.moto.MotoService;
import com.soulmotors.api.moto.VehiculoRepository;
import com.soulmotors.api.moto.dto.MotoDto;
import com.soulmotors.api.moto.dto.MotoRequest;

@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final VehiculoRepository vehiculos;
    private final MotoService motoService;

    public ClienteService(ClienteRepository clientes, VehiculoRepository vehiculos, MotoService motoService) {
        this.clientes = clientes;
        this.vehiculos = vehiculos;
        this.motoService = motoService;
    }

    @Transactional(readOnly = true)
    public Pagina<ClienteDto> listar(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return Pagina.de(clientes.findAll(pageable), ClienteDto::de);
        }
        return Pagina.de(clientes.buscar(Normalizar.patronBusqueda(q), pageable), ClienteDto::de);
    }

    @Transactional(readOnly = true)
    public ClienteDetalleDto obtener(Long id) {
        Cliente cliente = buscarCliente(id);
        return ClienteDetalleDto.de(cliente, motosDe(id));
    }

    @Transactional
    public ClienteDto crear(ClienteRequest req) {
        String documento = Normalizar.documento(req.documento());
        if (clientes.existsByDocumento(documento)) {
            throw documentoRepetido(documento);
        }
        Cliente cliente = new Cliente(documento, Normalizar.texto(req.nombre()), Normalizar.opcional(req.telefono()));
        return ClienteDto.de(clientes.saveAndFlush(cliente));
    }

    @Transactional
    public ClienteDto actualizar(Long id, ClienteRequest req) {
        Cliente cliente = buscarCliente(id);
        String documento = Normalizar.documento(req.documento());
        if (clientes.existsByDocumentoAndIdNot(documento, id)) {
            throw documentoRepetido(documento);
        }
        cliente.actualizar(documento, Normalizar.texto(req.nombre()), Normalizar.opcional(req.telefono()));
        clientes.flush();
        return ClienteDto.de(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarCliente(id);
        long motos = vehiculos.countByClienteId(id);
        if (motos > 0) {
            throw new ConflictoException("El cliente tiene " + motos
                    + " motocicleta(s) registrada(s). Elimínelas o trasládelas a otro cliente primero.");
        }
        clientes.delete(cliente);
        clientes.flush();
    }

    @Transactional(readOnly = true)
    public List<MotoDto> motosDe(Long clienteId) {
        if (!clientes.existsById(clienteId)) {
            throw noEncontrado(clienteId);
        }
        return vehiculos.findByClienteIdOrderByPlaca(clienteId).stream().map(MotoDto::de).toList();
    }

    @Transactional
    public MotoDto agregarMoto(Long clienteId, MotoRequest req) {
        return motoService.crear(buscarCliente(clienteId), req);
    }

    Cliente buscarCliente(Long id) {
        return clientes.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    private static NoEncontradoException noEncontrado(Long id) {
        return new NoEncontradoException("No existe el cliente con id " + id);
    }

    private static ConflictoException documentoRepetido(String documento) {
        return new ConflictoException("Ya existe un cliente con el documento " + documento,
                Map.of("documento", "Documento ya registrado"));
    }
}
