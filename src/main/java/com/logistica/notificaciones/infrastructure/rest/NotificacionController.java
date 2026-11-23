package com.logistica.notificaciones.infrastructure.rest;

import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import com.logistica.notificaciones.infrastructure.rest.dto.NotificacionRequest;
import com.logistica.notificaciones.infrastructure.rest.dto.NotificacionResponse;
import com.logistica.notificaciones.usecases.EnviarNotificacionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones", description = "Despacho de notificaciones por correo electrónico")
public class NotificacionController {

    private final EnviarNotificacionUseCase enviarNotificacionUseCase;

    public NotificacionController(EnviarNotificacionUseCase enviarNotificacionUseCase) {
        this.enviarNotificacionUseCase = Objects.requireNonNull(enviarNotificacionUseCase);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar una notificación por correo electrónico")
    public NotificacionResponse enviar(@Valid @RequestBody NotificacionRequest request) {
        EnviarNotificacionUseCase.Comando comando = new EnviarNotificacionUseCase.Comando(
                request.tipoEvento(),
                request.destinatario(),
                request.nombreDestinatario(),
                request.asunto(),
                request.mensaje(),
                request.referencia()
        );
        ResultadoEnvio resultado = enviarNotificacionUseCase.ejecutar(comando);
        return NotificacionResponse.desde(resultado);
    }
}