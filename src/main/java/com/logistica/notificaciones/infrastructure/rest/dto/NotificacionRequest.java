package com.logistica.notificaciones.infrastructure.rest.dto;

import com.logistica.notificaciones.domain.model.TipoEventoNotificacion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificacionRequest(
        @NotNull(message = "El tipoEvento es obligatorio")
        TipoEventoNotificacion tipoEvento,

        @NotBlank(message = "El correo destinatario es obligatorio")
        @Email(message = "El formato del correo destinatario no es válido")
        String destinatario,

        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombreDestinatario,

        @Size(max = 150, message = "El asunto no puede superar 150 caracteres")
        String asunto,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 2000, message = "El mensaje no puede superar 2000 caracteres")
        String mensaje,

        @Size(max = 80, message = "La referencia no puede superar 80 caracteres")
        String referencia
) {}