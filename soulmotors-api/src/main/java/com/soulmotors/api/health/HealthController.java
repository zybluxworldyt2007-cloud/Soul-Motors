package com.soulmotors.api.health;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * /health  -> liviano, no toca la BD. Es el que usa el auto-ping/cron de Render
 *             para que Neon pueda suspenderse y no gaste horas de cómputo.
 * /health/db -> verifica la conexión a Neon con SELECT 1.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "soulmotors-api");
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> database() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            body.put("status", "UP");
            body.put("database", "UP");
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            body.put("status", "DOWN");
            body.put("database", "DOWN");
            body.put("error", ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }
}
