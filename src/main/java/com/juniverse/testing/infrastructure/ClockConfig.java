package com.juniverse.testing.infrastructure;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Expone el reloj del sistema como bean para poder sustituirlo en las pruebas. */
@Configuration
public class ClockConfig {

    /**
     * Reloj del sistema.
     *
     * @return reloj UTC
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
