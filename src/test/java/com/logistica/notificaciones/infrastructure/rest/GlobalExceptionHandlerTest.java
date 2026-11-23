package com.logistica.notificaciones.infrastructure.rest;

import com.logistica.notificaciones.domain.exception.ReglaNegocioException;
import com.logistica.notificaciones.domain.exception.ServicioExternoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/v1/notificaciones");
    }

    @Test
    void manejarValidacionRetorna400ConMapaDeErrores() {
        // ARRANGE
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "destinatario", "El correo es obligatorio"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // ACT
        ProblemDetail problem = handler.manejarValidacion(ex, request);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Datos inválidos", problem.getTitle());
        assertNotNull(problem.getProperties());
        assertTrue(problem.getProperties().containsKey("errores"));
    }

    @Test
    void manejarJsonInvalidoRetorna400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON mal formado", mock(HttpInputMessage.class));

        ProblemDetail problem = handler.manejarJsonInvalido(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Petición mal formada", problem.getTitle());
    }

    @Test
    void manejarReglaNegocioRetorna422() {
        ReglaNegocioException ex = new ReglaNegocioException("El formato del correo no es válido");

        ProblemDetail problem = handler.manejarReglaNegocio(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), problem.getStatus());
        assertEquals("Regla de negocio incumplida", problem.getTitle());
        assertEquals("El formato del correo no es válido", problem.getDetail());
    }

    @Test
    void manejarServicioExternoRetorna503() {
        ServicioExternoException ex = new ServicioExternoException("Brevo no responde");

        ProblemDetail problem = handler.manejarServicioExterno(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), problem.getStatus());
        assertEquals("Proveedor externo no disponible", problem.getTitle());
    }
}