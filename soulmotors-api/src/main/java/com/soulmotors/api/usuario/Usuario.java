package com.soulmotors.api.usuario;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Rol rol;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Hash BCrypt, nunca la contraseña en texto plano. */
    @Column(nullable = false, length = 255)
    private String password;

    @Column
    private Boolean activo = Boolean.TRUE;

    @Column(name = "creado_at", insertable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at", insertable = false, updatable = false)
    private LocalDateTime actualizadoAt;

    protected Usuario() {
    }

    public Usuario(String nombre, Rol rol, String username, String password) {
        this.nombre = nombre;
        this.rol = rol;
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActivo() {
        return !Boolean.FALSE.equals(activo);
    }

    public LocalDateTime getCreadoAt() {
        return creadoAt;
    }

    public LocalDateTime getActualizadoAt() {
        return actualizadoAt;
    }
}
