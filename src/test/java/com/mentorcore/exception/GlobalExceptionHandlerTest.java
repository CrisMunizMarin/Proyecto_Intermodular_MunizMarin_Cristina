package com.mentorcore.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void runtimeException_devuelveVista500ConMensaje() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/ruta");
        Model model = new ConcurrentModel();

        String vista = handler.handleRuntimeExceptions(
                new IllegalStateException("Fallo controlado"), request, model);

        assertThat(vista).isEqualTo("error/500");
        assertThat(model.getAttribute("errorMsg")).isEqualTo("Fallo controlado");
    }

    @Test
    void exceptionGenerica_devuelveMensajeGenerico() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/ruta");
        Model model = new ConcurrentModel();

        String vista = handler.handleUnexpectedExceptions(
                new Exception("Fallo inesperado"), request, model);

        assertThat(vista).isEqualTo("error/500");
        assertThat(model.getAttribute("errorMsg"))
                .isEqualTo("No se pudo completar la operación en este momento.");
    }
}
