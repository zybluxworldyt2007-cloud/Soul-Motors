package com.soulmotors.api.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
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
}
