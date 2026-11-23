package com.logistica.notificaciones.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ResultadoEnvio(
        UUID notificacionId,
        boolean enviado,
        String proveedor,
        String idExterno,
        Instant fechaEnvio
) {
    public ResultadoEnvio {
        Objects.requireNonNull(notificacionId, "notificacionId es obligatorio");
        Objects.requireNonNull(proveedor, "proveedor es obligatorio");
        Objects.requireNonNull(fechaEnvio, "fechaEnvio es obligatorio");
    }
}