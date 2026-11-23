package com.logistica.notificaciones.domain.model;

import com.logistica.notificaciones.domain.exception.ReglaNegocioException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class Notificacion {

    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UUID id;
    private final TipoEventoNotificacion tipoEvento;
    private final String destinatario;
    private final String nombreDestinatario;
    private final String asunto;
    private final String mensaje;
    private final String referencia;
    private final Instant fechaCreacion;

    public Notificacion(UUID id,
                        TipoEventoNotificacion tipoEvento,
                        String destinatario,
                        String nombreDestinatario,
                        String asunto,
                        String mensaje,
                        String referencia,
                        Instant fechaCreacion) {
        this.id = Objects.requireNonNull(id, "El id de la notificación es obligatorio");
        this.tipoEvento = Objects.requireNonNull(tipoEvento, "El tipo de evento es obligatorio");
        this.destinatario = validarEmail(destinatario);
        this.nombreDestinatario = (nombreDestinatario == null || nombreDestinatario.isBlank())
                ? "Cliente"
                : nombreDestinatario.trim();
        this.asunto = (asunto == null || asunto.isBlank())
                ? tipoEvento.getTituloPorDefecto()
                : asunto.trim();
        this.mensaje = validarTextoObligatorio(mensaje, "El contenido del mensaje no puede estar vacío");
        this.referencia = (referencia == null || referencia.isBlank()) ? "N/A" : referencia.trim();
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación es obligatoria");
    }

    public static Notificacion crear(TipoEventoNotificacion tipoEvento,
                                     String destinatario,
                                     String nombreDestinatario,
                                     String asunto,
                                     String mensaje,
                                     String referencia,
                                     Instant ahora) {
        return new Notificacion(
                UUID.randomUUID(),
                tipoEvento,
                destinatario,
                nombreDestinatario,
                asunto,
                mensaje,
                referencia,
                ahora
        );
    }

    private static String validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ReglaNegocioException("El correo del destinatario es obligatorio");
        }
        String limpio = email.trim();
        if (!PATRON_EMAIL.matcher(limpio).matches()) {
            throw new ReglaNegocioException("El formato del correo electrónico no es válido: " + limpio);
        }
        return limpio;
    }

    private static String validarTextoObligatorio(String valor, String error) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(error);
        }
        return valor.trim();
    }

    public UUID getId() { return id; }
    public TipoEventoNotificacion getTipoEvento() { return tipoEvento; }
    public String getDestinatario() { return destinatario; }
    public String getNombreDestinatario() { return nombreDestinatario; }
    public String getAsunto() { return asunto; }
    public String getMensaje() { return mensaje; }
    public String getReferencia() { return referencia; }
    public Instant getFechaCreacion() { return fechaCreacion; }
}
