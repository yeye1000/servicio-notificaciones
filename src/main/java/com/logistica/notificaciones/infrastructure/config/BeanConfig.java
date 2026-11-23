package com.logistica.notificaciones.infrastructure.config;

import com.logistica.notificaciones.domain.gateway.ProveedorNotificacionGateway;
import com.logistica.notificaciones.usecases.EnviarNotificacionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public EnviarNotificacionUseCase enviarNotificacionUseCase(
            ProveedorNotificacionGateway proveedorGateway,
            Clock clock) {
        return new EnviarNotificacionUseCase(proveedorGateway, clock);
    }
}
