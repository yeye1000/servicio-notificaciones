package com.logistica.notificaciones.domain.model;

public enum TipoEventoNotificacion {
    GUIA_EMITIDA("Confirmación de guía emitida"),
    ESTADO_ACTUALIZADO("Actualización de estado de tu envío"),
    ENVIO_CANCELADO("Confirmación de cancelación de guía"),
    DEVOLUCION_GENERADA("Guía de devolución generada"),
    LOTE_ASIGNADO("Nueva ruta y lote asignado"),
    ALERTA_OPERATIVA("Alerta operativa de flota"),
    ALERTA_CUENTA("Aviso sobre el estado de tu cuenta");

    private final String tituloPorDefecto;

    TipoEventoNotificacion(String tituloPorDefecto) {
        this.tituloPorDefecto = tituloPorDefecto;
    }

    public String getTituloPorDefecto() {
        return tituloPorDefecto;
    }
}