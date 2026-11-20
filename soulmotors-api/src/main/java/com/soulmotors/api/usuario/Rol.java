package com.soulmotors.api.usuario;

/**
 * Valores permitidos por el CHECK de la tabla usuario.
 * Según los Docs, los roles operativos son ADMINISTRADOR, MECANICO y VENDEDOR;
 * GERENTE y EMPLEADO se mapean para que la lectura de la tabla nunca falle.
 */
public enum Rol {
    ADMINISTRADOR,
    GERENTE,
    MECANICO,
    VENDEDOR,
    EMPLEADO
}
