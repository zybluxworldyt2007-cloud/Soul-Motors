package com.soulmotors.api.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KeepAliveTest {

    @Test
    void sinUrlNoHaceNada() {
        KeepAlive k = new KeepAlive("", true);
        assertThat(k.isActivo()).isFalse();
        k.ping(); // no debe lanzar
    }

    @Test
    void armaLaUrlDeHealth() {
        KeepAlive k = new KeepAlive("https://soulmotors-api.onrender.com/", true);
        assertThat(k.isActivo()).isTrue();
        assertThat(k.getUrl()).isEqualTo("https://soulmotors-api.onrender.com/api/health");
    }

    @Test
    void sePuedeDesactivar() {
        assertThat(new KeepAlive("https://x.onrender.com", false).isActivo()).isFalse();
    }
}
