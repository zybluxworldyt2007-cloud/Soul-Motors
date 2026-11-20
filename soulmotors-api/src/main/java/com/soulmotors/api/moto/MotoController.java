package com.soulmotors.api.moto;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soulmotors.api.cliente.ClienteController.Paginacion;
import com.soulmotors.api.common.Pagina;
import com.soulmotors.api.moto.dto.MotoDto;
import com.soulmotors.api.moto.dto.MotoRequest;

import jakarta.validation.Valid;

/** HU-03 Motocicletas. Se crean desde POST /api/clientes/{id}/motos. */
@RestController
@RequestMapping("/motos")
public class MotoController {

    private final MotoService service;

    public MotoController(MotoService service) {
        this.service = service;
    }

    /** GET /api/motos?placa=ABC&page=0&size=20 — búsqueda parcial por placa. */
    @GetMapping
    public Pagina<MotoDto> listar(@RequestParam(required = false) String placa,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return service.listar(placa, Paginacion.de(page, size, Sort.by("placa")));
    }

    @GetMapping("/{id}")
    public MotoDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** PUT /api/motos/{id} — editar datos o traspasar a otro cliente con clienteId. */
    @PutMapping("/{id}")
    public MotoDto actualizar(@PathVariable Long id, @Valid @RequestBody MotoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
