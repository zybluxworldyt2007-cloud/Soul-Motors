package com.soulmotors.api.cliente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import com.soulmotors.api.moto.Vehiculo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    /** Los timestamps los llena la BD (DEFAULT y trigger); Hibernate los relee. */
    @Generated(event = EventType.INSERT)
    @Column(name = "creado_at", insertable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "actualizado_at", insertable = false, updatable = false)
    private LocalDateTime actualizadoAt;

    /** Relación 1 a N: un cliente puede tener varias motocicletas. */
    @OneToMany(mappedBy = "cliente")
    @OrderBy("placa")
    private List<Vehiculo> motos = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String documento, String nombre, String telefono) {
        actualizar(documento, nombre, telefono);
    }

    public final void actualizar(String documento, String nombre, String telefono) {
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDateTime getCreadoAt() {
        return creadoAt;
    }

    public LocalDateTime getActualizadoAt() {
        return actualizadoAt;
    }

    public List<Vehiculo> getMotos() {
        return motos;
    }
}
