package com.logistica.notificaciones.infrastructure.external;

import com.logistica.notificaciones.domain.exception.ServicioExternoException;
import com.logistica.notificaciones.domain.model.Notificacion;
import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import com.logistica.notificaciones.domain.model.TipoEventoNotificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BrevoNotificacionAdapterTest {

    private MockRestServiceServer mockServer;
    private BrevoNotificacionAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.brevo.com/v3");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new BrevoNotificacionAdapter(
                builder.build(),
                "api-key-prueba",
                "notificaciones@logistica.com",
                "Plataforma Logística"
        );
    }

    @Test
    void enviarCorreoConBrevoExitoso() {
        // ARRANGE
        Notificacion notificacion = Notificacion.crear(
                TipoEventoNotificacion.GUIA_EMITIDA,
                "cliente@correo.com",
                "Paula",
                "Guía creada",
                "Tu paquete va en camino.",
                "LG-999",
                Instant.now()
        );

        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "api-key-prueba"))
                .andRespond(withSuccess("{\"messageId\":\"<20261123@brevo.com>\"}", MediaType.APPLICATION_JSON));

        // ACT
        ResultadoEnvio resultado = adapter.enviar(notificacion);

        // ASSERT
        mockServer.verify();
        assertTrue(resultado.enviado());
        assertEquals("BREVO", resultado.proveedor());
        assertEquals("<20261123@brevo.com>", resultado.idExterno());
    }

    @Test
    void lanzarServicioExternoExceptionCuandoBrevoFalla() {
        // ARRANGE
        Notificacion notificacion = Notificacion.crear(
                TipoEventoNotificacion.ALERTA_CUENTA,
                "cliente@correo.com",
                "Paula",
                "Alerta",
                "Cuenta suspendida.",
                "CLI-1",
                Instant.now()
        );

        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withServerError());
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withServerError());

        // ACT & ASSERT
        assertThrows(ServicioExternoException.class, () -> adapter.enviar(notificacion));
        mockServer.verify();
    }

    @Test
    void retornaBrevoOkCuandoRespuestaNoTraeMessageIdYConstructorPorDefectoFunciona() {
        // ARRANGE: Cubre el constructor @Autowired + crearRestClient
        BrevoNotificacionAdapter adapterPorDefecto = new BrevoNotificacionAdapter(
                "https://api.brevo.com/v3", "api-key", "remitente@logistica.com", "Logística"
        );
        assertNotNull(adapterPorDefecto);

        Notificacion notificacion = Notificacion.crear(
                TipoEventoNotificacion.ENVIO_CANCELADO,
                "cliente@correo.com",
                "Paula",
                "Cancelado",
                "Guía cancelada.",
                "LG-500",
                Instant.now()
        );

        // Respuesta JSON válida pero sin el campo messageId
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        // ACT
        ResultadoEnvio resultado = adapter.enviar(notificacion);

        // ASSERT
        mockServer.verify();
        assertTrue(resultado.enviado());
        assertEquals("BREVO-OK", resultado.idExterno());
    }

    @Test
    void abrirCircuitBreakerTrasMultiplesFallosYRechazarLlamadaInmediata() {
        // ARRANGE
        Notificacion notificacion = Notificacion.crear(
                TipoEventoNotificacion.ALERTA_OPERATIVA,
                "supervisor@correo.com",
                "Carlos",
                "Alerta",
                "Mantenimiento próximo.",
                "VEH-101",
                Instant.now()
        );

        // Cada llamada hace 2 intentos (Retry maxAttempts=2). 10 llamadas = 20 peticiones fallidas
        for (int i = 0; i < 20; i++) {
            mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                    .andRespond(withServerError());
        }

        // ACT: Ejecutamos 10 llamadas fallidas para alcanzar minimumNumberOfCalls(10)
        for (int i = 0; i < 10; i++) {
            assertThrows(ServicioExternoException.class, () -> adapter.enviar(notificacion));
        }

        // ASSERT: La llamada 11 encuentra el circuito abierto y falla al instante sin tocar la red
        ServicioExternoException exCircuito = assertThrows(ServicioExternoException.class,
                () -> adapter.enviar(notificacion));
        assertTrue(exCircuito.getMessage().contains("Circuito abierto"));
        mockServer.verify();
    }
}