package com.logistica.notificaciones.usecases;

import com.logistica.notificaciones.domain.exception.ReglaNegocioException;
import com.logistica.notificaciones.domain.gateway.ProveedorNotificacionGateway;
import com.logistica.notificaciones.domain.model.Notificacion;
import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import com.logistica.notificaciones.domain.model.TipoEventoNotificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnviarNotificacionUseCaseTest {

    @Mock
    private ProveedorNotificacionGateway proveedorGateway;

    private Clock clockFijo;
    private EnviarNotificacionUseCase useCase;

    @BeforeEach
    void setUp() {
        clockFijo = Clock.fixed(Instant.parse("2026-11-23T10:00:00Z"), ZoneOffset.UTC);
        useCase = new EnviarNotificacionUseCase(proveedorGateway, clockFijo);
    }

    @Test
    void enviarNotificacionCorrectamente() {
        // ARRANGE
        var comando = new EnviarNotificacionUseCase.Comando(
                TipoEventoNotificacion.GUIA_EMITIDA,
                "cliente@correo.com",
                "Laura Díaz",
                "Tu guía fue emitida",
                "Tu paquete con guía LG-20261123-001 ha sido registrado.",
                "LG-20261123-001"
        );

        when(proveedorGateway.enviar(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            return new ResultadoEnvio(n.getId(), true, "SIMULADO", "SIM-12345678", clockFijo.instant());
        });

        // ACT
        ResultadoEnvio resultado = useCase.ejecutar(comando);

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.enviado());
        assertEquals("SIMULADO", resultado.proveedor());
        assertEquals("SIM-12345678", resultado.idExterno());

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(proveedorGateway, times(1)).enviar(captor.capture());
        assertEquals("cliente@correo.com", captor.getValue().getDestinatario());
        assertEquals("Laura Díaz", captor.getValue().getNombreDestinatario());
        assertEquals("LG-20261123-001", captor.getValue().getReferencia());
    }

    @Test
    void enviarNotificacionAplicaValoresPorDefectoCuandoOpcionalesVienenVacios() {
        // ARRANGE
        var comando = new EnviarNotificacionUseCase.Comando(
                TipoEventoNotificacion.ESTADO_ACTUALIZADO,
                "destino@correo.com",
                "   ",
                null,
                "El envío va en camino a su destino.",
                ""
        );

        when(proveedorGateway.enviar(any(Notificacion.class))).thenReturn(
                new ResultadoEnvio(UUID.randomUUID(), true, "SIMULADO", "SIM-0001", clockFijo.instant())
        );

        // ACT
        useCase.ejecutar(comando);

        // ASSERT
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(proveedorGateway).enviar(captor.capture());
        Notificacion enviada = captor.getValue();

        assertEquals("Cliente", enviada.getNombreDestinatario());
        assertEquals(TipoEventoNotificacion.ESTADO_ACTUALIZADO.getTituloPorDefecto(), enviada.getAsunto());
        assertEquals("N/A", enviada.getReferencia());
    }

    @Test
    void noEnviarNotificacionCuandoCorreoEsNuloOInvalido() {
        // ARRANGE
        var comandoNulo = new EnviarNotificacionUseCase.Comando(
                TipoEventoNotificacion.GUIA_EMITIDA, null, "Ana", "Asunto", "Mensaje", "REF-1"
        );
        var comandoMalFormato = new EnviarNotificacionUseCase.Comando(
                TipoEventoNotificacion.GUIA_EMITIDA, "correo-sin-arroba", "Ana", "Asunto", "Mensaje", "REF-1"
        );

        // ACT & ASSERT
        ReglaNegocioException ex1 = assertThrows(ReglaNegocioException.class, () -> useCase.ejecutar(comandoNulo));
        assertEquals("El correo del destinatario es obligatorio", ex1.getMessage());

        ReglaNegocioException ex2 = assertThrows(ReglaNegocioException.class, () -> useCase.ejecutar(comandoMalFormato));
        assertTrue(ex2.getMessage().contains("no es válido"));

        verifyNoInteractions(proveedorGateway);
    }

    @Test
    void noEnviarNotificacionCuandoMensajeEstaVacio() {
        // ARRANGE
        var comando = new EnviarNotificacionUseCase.Comando(
                TipoEventoNotificacion.ENVIO_CANCELADO, "ana@correo.com", "Ana", "Cancelado", "   ", "REF-2"
        );

        // ACT & ASSERT
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> useCase.ejecutar(comando));
        assertEquals("El contenido del mensaje no puede estar vacío", ex.getMessage());
        verifyNoInteractions(proveedorGateway);
    }

    @Test
    void noEnviarNotificacionCuandoComandoEsNulo() {
        assertThrows(NullPointerException.class, () -> useCase.ejecutar(null));
        verifyNoInteractions(proveedorGateway);
    }
}