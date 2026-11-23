package com.logistica.notificaciones.domain.gateway;

import com.logistica.notificaciones.domain.model.Notificacion;
import com.logistica.notificaciones.domain.model.ResultadoEnvio;

public interface ProveedorNotificacionGateway {
    ResultadoEnvio enviar(Notificacion notificacion);
}