package com.taller.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> responseStatus(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", exception.getReason() == null ? "No fue posible completar la operación" : exception.getReason()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getDefaultMessage()).orElse("Datos no válidos");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> uploadSize() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("message", "La fotografía debe pesar menos de 7 MB"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> integrity() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "No se pudo guardar el registro porque ya existe información duplicada"));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> generic() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Ocurrió un error al procesar la solicitud"));
    }
}
