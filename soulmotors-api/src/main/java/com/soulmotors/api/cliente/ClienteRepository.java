package com.soulmotors.api.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    /** Búsqueda por nombre o documento; patron ya viene en minúsculas y con %. */
    @Query("""
            select c from Cliente c
            where lower(c.nombre) like :patron or lower(c.documento) like :patron
            """)
    Page<Cliente> buscar(@Param("patron") String patron, Pageable pageable);
}
