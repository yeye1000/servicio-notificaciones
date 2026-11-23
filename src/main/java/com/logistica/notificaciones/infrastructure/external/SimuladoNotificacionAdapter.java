package com.logistica.notificaciones.infrastructure.external;


import com.logistica.notificaciones.domain.gateway.ProveedorNotificacionGateway;
import com.logistica.notificaciones.domain.model.Notificacion;
import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "notificaciones.proveedor", havingValue = "simulado", matchIfMissing = true)
public class SimuladoNotificacionAdapter implements ProveedorNotificacionGateway {

    private static final Logger log = LoggerFactory.getLogger(SimuladoNotificacionAdapter.class);

    @Override
    public ResultadoEnvio enviar(Notificacion notificacion) {
        String idSimulado = "SIM-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[NOTIFICACION SIMULADA] Evento={} | Para={} ({}) | Ref={} | Asunto='{}' | Mensaje='{}'",
                notificacion.getTipoEvento(),
                notificacion.getDestinatario(),
                notificacion.getNombreDestinatario(),
                notificacion.getReferencia(),
                notificacion.getAsunto(),
                notificacion.getMensaje());

        return new ResultadoEnvio(
                notificacion.getId(),
                true,
                "SIMULADO",
                idSimulado,
                Instant.now()
        );
    }
}