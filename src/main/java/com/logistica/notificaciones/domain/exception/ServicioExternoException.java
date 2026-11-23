package com.logistica.notificaciones.domain.exception;

public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }
}