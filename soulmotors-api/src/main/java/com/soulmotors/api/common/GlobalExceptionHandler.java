package com.soulmotors.api.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Formato único de error para el frontend: {timestamp, status, error, mensaje, campos}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(Instant timestamp, int status, String error, String mensaje,
                           Map<String, String> campos) {

        public static ResponseEntity<ApiError> of(HttpStatus status, String mensaje, Map<String, String> campos) {
            return ResponseEntity.status(status)
                    .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), mensaje, campos));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ApiError.of(HttpStatus.BAD_REQUEST, "Datos inválidos", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> cuerpoInvalido(HttpMessageNotReadableException ex) {
        return ApiError.of(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> credenciales(BadCredentialsException ex) {
        return ApiError.of(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> sinPermiso(AccessDeniedException ex) {
        return ApiError.of(HttpStatus.FORBIDDEN, "Su rol no tiene permiso para esta acción", null);
    }

    @ExceptionHandler(NoEncontradoException.class)
    ResponseEntity<ApiError> noEncontrado(NoEncontradoException ex) {
        return ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictoException.class)
    ResponseEntity<ApiError> conflicto(ConflictoException ex) {
        return ApiError.of(HttpStatus.CONFLICT, ex.getMessage(), ex.getCampos());
    }

    /** Respaldo si dos peticiones simultáneas chocan con un UNIQUE o FK de la BD. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex) {
        return ApiError.of(HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes (registro duplicado o en uso)", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return ApiError.of(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + ex.getName(), null);
    }
}
