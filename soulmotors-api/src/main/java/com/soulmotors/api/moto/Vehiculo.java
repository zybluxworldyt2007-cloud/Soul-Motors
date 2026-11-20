package com.soulmotors.api.moto;

import java.time.LocalDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import com.soulmotors.api.cliente.Cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Motocicleta de un cliente (tabla vehiculo). */
@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, unique = true, length = 20)
    private String placa;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Generated(event = EventType.INSERT)
    @Column(name = "creado_at", insertable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "actualizado_at", insertable = false, updatable = false)
    private LocalDateTime actualizadoAt;

    protected Vehiculo() {
    }

    public Vehiculo(Cliente cliente, String placa, String marca, String modelo) {
        this.cliente = cliente;
        actualizar(placa, marca, modelo);
    }

    public final void actualizar(String placa, String marca, String modelo) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
    }

    public void asignarCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public LocalDateTime getCreadoAt() {
        return creadoAt;
    }

    public LocalDateTime getActualizadoAt() {
        return actualizadoAt;
    }
}
