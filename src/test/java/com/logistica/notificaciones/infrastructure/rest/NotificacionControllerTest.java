package com.logistica.notificaciones.infrastructure.rest;

import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import com.logistica.notificaciones.domain.model.TipoEventoNotificacion;
import com.logistica.notificaciones.infrastructure.rest.dto.NotificacionRequest;
import com.logistica.notificaciones.infrastructure.rest.dto.NotificacionResponse;
import com.logistica.notificaciones.usecases.EnviarNotificacionUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private EnviarNotificacionUseCase enviarNotificacionUseCase;

    @InjectMocks
    private NotificacionController controller;

    @Test
    void enviarNotificacionRetornaRespuestaMapeada() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        Instant ahora = Instant.parse("2026-11-23T10:00:00Z");
        var request = new NotificacionRequest(
                TipoEventoNotificacion.GUIA_EMITIDA,
                "usuario@correo.com",
                "Carlos",
                "Guía emitida",
                "Detalle del envío",
                "LG-123"
        );

        when(enviarNotificacionUseCase.ejecutar(any())).thenReturn(
                new ResultadoEnvio(id, true, "BREVO", "<msg-01@brevo.com>", ahora)
        );

        // ACT
        NotificacionResponse response = controller.enviar(request);

        // ASSERT
        assertNotNull(response);
        assertEquals(id, response.notificacionId());
        assertTrue(response.enviado());
        assertEquals("BREVO", response.proveedor());
        assertEquals("<msg-01@brevo.com>", response.idExterno());
    }
}