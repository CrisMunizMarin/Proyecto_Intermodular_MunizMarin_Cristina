package com.mentorcore.util;

import org.slf4j.Logger;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Utilidad para mostrar mensajes seguros al usuario sin exponer
 * detalles técnicos de excepciones en la interfaz web.
 */
public final class ControllerMessageUtil {

    private static final String GENERIC_ERROR_MESSAGE =
            "No se pudo completar la operación. Inténtalo de nuevo.";

    private ControllerMessageUtil() {
    }

    public static void addSafeErrorMessage(RedirectAttributes redirectAttributes,
                                           Logger log,
                                           String context,
                                           Exception exception) {
        addSafeErrorMessage(redirectAttributes, log, context, exception, GENERIC_ERROR_MESSAGE);
    }

    public static void addSafeErrorMessage(RedirectAttributes redirectAttributes,
                                           Logger log,
                                           String context,
                                           Exception exception,
                                           String userMessage) {
        log.error("{}: {}", context, exception.getMessage(), exception);
        redirectAttributes.addFlashAttribute("errorMsg", userMessage);
    }
}
