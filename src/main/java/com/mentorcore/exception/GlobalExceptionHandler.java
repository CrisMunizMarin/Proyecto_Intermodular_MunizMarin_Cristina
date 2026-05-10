package com.mentorcore.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, RuntimeException.class})
    public String handleRuntimeExceptions(Exception ex, HttpServletRequest request, Model model) {
        log.error("Error controlado procesando {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage(), ex);
        model.addAttribute("errorMsg", ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "No se pudo completar la operación en este momento.");
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedExceptions(Exception ex, HttpServletRequest request, Model model) {
        log.error("Error inesperado procesando {} {}", request.getMethod(), request.getRequestURI(), ex);
        model.addAttribute("errorMsg", "No se pudo completar la operación en este momento.");
        return "error/500";
    }
}
