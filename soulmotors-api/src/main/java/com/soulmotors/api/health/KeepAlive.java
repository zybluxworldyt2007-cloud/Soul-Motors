package com.soulmotors.api.health;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;

/**
 * Auto-ping: Render Free apaga el servicio tras 15 min sin tráfico entrante.
 * Mientras la app está despierta, se llama a sí misma por su URL pública cada
 * 10 min (pasa por el proxy de Render, así que cuenta como tráfico).
 * Render define RENDER_EXTERNAL_URL; en local queda vacío y no hace nada.
 * El cron externo (cron-job.org) es el respaldo para despertarla si se reinicia.
 */
@Configuration
@EnableScheduling
public class KeepAlive {

    private static final Logger log = LoggerFactory.getLogger(KeepAlive.class);

    private final String url;
    private final boolean activo;
    private final RestClient client;

    public KeepAlive(@Value("${app.keepalive.url:}") String baseUrl,
                     @Value("${app.keepalive.enabled:true}") boolean activo) {
        this.url = baseUrl.isBlank() ? "" : baseUrl.replaceAll("/+$", "") + "/api/health";
        this.activo = activo && !this.url.isEmpty();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.client = RestClient.builder().requestFactory(factory).build();

        if (this.activo) {
            log.info("Auto-ping activo hacia {}", this.url);
        }
    }

    @Scheduled(initialDelayString = "${app.keepalive.initial-delay:PT2M}",
            fixedDelayString = "${app.keepalive.interval:PT10M}")
    void ping() {
        if (!activo) {
            return;
        }
        try {
            client.get().uri(url).retrieve().toBodilessEntity();
            log.debug("Auto-ping OK");
        } catch (Exception ex) {
            log.warn("Auto-ping falló: {}", ex.getMessage());
        }
    }

    boolean isActivo() {
        return activo;
    }

    String getUrl() {
        return url;
    }
}
