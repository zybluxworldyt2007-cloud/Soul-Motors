package com.soulmotors.api;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SoulmotorsApiApplication {

	public static void main(String[] args) {
		// Neon (pooler) siempre guarda CURRENT_TIMESTAMP en UTC. Hibernate lee las fechas
		// como UTC (hibernate.jdbc.time_zone) y las entrega en esta zona, igual en local y en Render.
		TimeZone.setDefault(TimeZone.getTimeZone(System.getenv().getOrDefault("APP_TZ", "America/Bogota")));
		SpringApplication.run(SoulmotorsApiApplication.class, args);
	}

}
