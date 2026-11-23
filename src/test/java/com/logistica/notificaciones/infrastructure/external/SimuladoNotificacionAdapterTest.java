package com.logistica.notificaciones.infrastructure.external;

import com.logistica.notificaciones.domain.model.Notificacion;
import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import com.logistica.notificaciones.domain.model.TipoEventoNotificacion;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SimuladoNotificacionAdapterTest {

    @Test
    void enviarEnModoSimuladoRetornaResultadoExitoso() {
        // ARRANGE
        SimuladoNotificacionAdapter adapter = new SimuladoNotificacionAdapter();
        Notificacion notificacion = Notificacion.crear(
                TipoEventoNotificacion.LOTE_ASIGNADO,
                "conductor@correo.com",
                "Pedro",
                "Ruta asignada",
                "Tienes 12 paquetes asignados.",
                "ASIG-01",
                Instant.now()
        );

        // ACT
        ResultadoEnvio resultado = adapter.enviar(notificacion);

        // ASSERT
        assertTrue(resultado.enviado());
        assertEquals("SIMULADO", resultado.proveedor());
        assertTrue(resultado.idExterno().startsWith("SIM-"));
    }
}