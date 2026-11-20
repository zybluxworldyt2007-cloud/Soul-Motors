package com.soulmotors.api.moto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    long countByClienteId(Long clienteId);

    List<Vehiculo> findByClienteIdOrderByPlaca(Long clienteId);

    @EntityGraph(attributePaths = "cliente")
    Optional<Vehiculo> findWithClienteById(Long id);

    @EntityGraph(attributePaths = "cliente")
    Page<Vehiculo> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "cliente")
    Page<Vehiculo> findByPlacaContaining(String placa, Pageable pageable);
}
