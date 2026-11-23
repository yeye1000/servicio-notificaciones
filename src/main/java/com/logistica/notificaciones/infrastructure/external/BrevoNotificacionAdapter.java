package com.logistica.notificaciones.infrastructure.external;

import com.logistica.notificaciones.domain.exception.ServicioExternoException;
import com.logistica.notificaciones.domain.gateway.ProveedorNotificacionGateway;
import com.logistica.notificaciones.domain.model.Notificacion;
import com.logistica.notificaciones.domain.model.ResultadoEnvio;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "notificaciones.proveedor", havingValue = "brevo")
public class BrevoNotificacionAdapter implements ProveedorNotificacionGateway {

    private final RestClient restClient;
    private final String apiKey;
    private final String remitenteEmail;
    private final String remitenteNombre;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    @Autowired
    public BrevoNotificacionAdapter(
            @Value("${brevo.base-url}") String baseUrl,
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.remitente-email}") String remitenteEmail,
            @Value("${brevo.remitente-nombre}") String remitenteNombre) {
        this(crearRestClient(baseUrl), apiKey, remitenteEmail, remitenteNombre);
    }

    public BrevoNotificacionAdapter(RestClient restClient,
                                    String apiKey,
                                    String remitenteEmail,
                                    String remitenteNombre) {
        this.restClient = Objects.requireNonNull(restClient, "restClient es obligatorio");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey es obligatorio");
        this.remitenteEmail = Objects.requireNonNull(remitenteEmail, "remitenteEmail es obligatorio");
        this.remitenteNombre = Objects.requireNonNull(remitenteNombre, "remitenteNombre es obligatorio");

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        this.circuitBreaker = CircuitBreaker.of("brevo-api", cbConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(150))
                .build();
        this.retry = Retry.of("brevo-api", retryConfig);
    }

    private static RestClient crearRestClient(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public ResultadoEnvio enviar(Notificacion notificacion) {
        Map<String, Object> cuerpo = Map.of(
                "sender", Map.of("name", remitenteNombre, "email", remitenteEmail),
                "to", List.of(Map.of("email", notificacion.getDestinatario(), "name", notificacion.getNombreDestinatario())),
                "subject", notificacion.getAsunto(),
                "htmlContent", construirHtml(notificacion)
        );

        Supplier<Map<String, Object>> llamadaProtegida = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, () -> ejecutarPeticion(cuerpo))
        );

        try {
            Map<String, Object> respuesta = llamadaProtegida.get();
            String messageId = respuesta != null && respuesta.get("messageId") != null
                    ? respuesta.get("messageId").toString()
                    : "BREVO-OK";

            return new ResultadoEnvio(
                    notificacion.getId(),
                    true,
                    "BREVO",
                    messageId,
                    Instant.now()
            );
        } catch (CallNotPermittedException ex) {
            throw new ServicioExternoException("Circuito abierto hacia el proveedor de correo Brevo", ex);
        } catch (Exception ex) {
            throw new ServicioExternoException("Error al enviar correo a través de Brevo: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> ejecutarPeticion(Map<String, Object> cuerpo) {
        return restClient.post()
                .uri("/smtp/email")
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private String construirHtml(Notificacion n) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                    <h2 style="color: #1a56db; margin-top: 0;">%s</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p style="font-size: 15px; line-height: 1.5; color: #333;">%s</p>
                    <p style="background: #f3f4f6; padding: 10px; border-radius: 4px; font-size: 13px;">
                        <strong>Referencia:</strong> %s<br/>
                        <strong>Evento:</strong> %s
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>
                    <small style="color: #888;">Plataforma de Logística de Envíos y Rastreo</small>
                </div>
                """.formatted(
                n.getAsunto(),
                n.getNombreDestinatario(),
                n.getMensaje(),
                n.getReferencia(),
                n.getTipoEvento().name()
        );
    }
}