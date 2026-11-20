package com.soulmotors.api.common;

/** Se traduce a HTTP 404. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
