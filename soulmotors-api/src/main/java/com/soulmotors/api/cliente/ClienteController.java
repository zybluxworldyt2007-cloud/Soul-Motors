package com.soulmotors.api.cliente;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soulmotors.api.cliente.dto.ClienteDetalleDto;
import com.soulmotors.api.cliente.dto.ClienteDto;
import com.soulmotors.api.cliente.dto.ClienteRequest;
import com.soulmotors.api.common.Pagina;
import com.soulmotors.api.moto.dto.MotoDto;
import com.soulmotors.api.moto.dto.MotoRequest;

import jakarta.validation.Valid;

/** HU-02 Clientes. Lectura y registro para cualquier rol autenticado; eliminar solo Administrador. */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    /** GET /api/clientes?q=carlos&page=0&size=20 — busca por nombre o documento. */
    @GetMapping
    public Pagina<ClienteDto> listar(@RequestParam(required = false) String q,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return service.listar(q, Paginacion.de(page, size, Sort.by("nombre", "id")));
    }

    /** GET /api/clientes/{id} — incluye sus motocicletas. */
    @GetMapping("/{id}")
    public ClienteDetalleDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ClienteDto> crear(@Valid @RequestBody ClienteRequest req) {
        ClienteDto creado = service.crear(req);
        return ResponseEntity.created(URI.create("/api/clientes/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ClienteDto actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/clientes/{id}/motos — relación 1 a N. */
    @GetMapping("/{id}/motos")
    public List<MotoDto> motos(@PathVariable Long id) {
        return service.motosDe(id);
    }

    /** POST /api/clientes/{id}/motos — registra una moto para el cliente. */
    @PostMapping("/{id}/motos")
    public ResponseEntity<MotoDto> agregarMoto(@PathVariable Long id, @Valid @RequestBody MotoRequest req) {
        MotoDto creada = service.agregarMoto(id, req);
        return ResponseEntity.created(URI.create("/api/motos/" + creada.id())).body(creada);
    }

    /** Límites de paginación compartidos. */
    public static final class Paginacion {
        private Paginacion() {
        }

        public static PageRequest de(int page, int size, Sort sort) {
            return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), sort);
        }
    }
}
