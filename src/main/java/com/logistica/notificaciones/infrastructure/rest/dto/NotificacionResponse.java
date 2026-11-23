package com.logistica.notificaciones.infrastructure.rest.dto;

import com.logistica.notificaciones.domain.model.ResultadoEnvio;

import java.time.Instant;
import java.util.UUID;

public record NotificacionResponse(
        UUID notificacionId,
        boolean enviado,
        String proveedor,
        String idExterno,
        Instant fechaEnvio
) {
    public static NotificacionResponse desde(ResultadoEnvio resultado) {
        return new NotificacionResponse(
                resultado.notificacionId(),
                resultado.enviado(),
                resultado.proveedor(),
                resultado.idExterno(),
                resultado.fechaEnvio()
        );
    }
}