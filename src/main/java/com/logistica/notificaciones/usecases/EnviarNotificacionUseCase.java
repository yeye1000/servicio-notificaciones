package com.logistica.notificaciones.usecases;

import com.logistica.notificaciones.domain.gateway.ProveedorNotificacionGateway;
import com.logistica.notificaciones.domain.model.Notificacion;
import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import com.logistica.notificaciones.domain.model.TipoEventoNotificacion;

import java.time.Clock;
import java.util.Objects;

public class EnviarNotificacionUseCase {

    private final ProveedorNotificacionGateway proveedorGateway;
    private final Clock clock;

    public EnviarNotificacionUseCase(ProveedorNotificacionGateway proveedorGateway, Clock clock) {
        this.proveedorGateway = Objects.requireNonNull(proveedorGateway, "proveedorGateway es obligatorio");
        this.clock = Objects.requireNonNull(clock, "clock es obligatorio");
    }

    public ResultadoEnvio ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando de notificación es obligatorio");

        Notificacion notificacion = Notificacion.crear(
                comando.tipoEvento(),
                comando.destinatario(),
                comando.nombreDestinatario(),
                comando.asunto(),
                comando.mensaje(),
                comando.referencia(),
                clock.instant()
        );

        return proveedorGateway.enviar(notificacion);
    }

    public record Comando(
            TipoEventoNotificacion tipoEvento,
            String destinatario,
            String nombreDestinatario,
            String asunto,
            String mensaje,
            String referencia
    ) {}
}