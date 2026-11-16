package lk.ceylonbites.exception;

import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(
            Map.of(
                "status",
                status.value(),
                "message",
                message,
                "timestamp",
                Instant.now().toString()));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<?> api(ApiException e) {
    return response(e.getStatus(), e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    String msg =
        e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .sorted()
            .findFirst()
            .orElse("Invalid input");
    return response(HttpStatus.BAD_REQUEST, msg);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<?> json(Exception e) {
    return response(HttpStatus.BAD_REQUEST, "Invalid JSON, unknown field or invalid enum value");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception e) {
    return response(HttpStatus.CONFLICT, "This record conflicts with existing data");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<?> denied(Exception e) {
    return response(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unexpected(Exception e) {
    log.error("Unhandled API failure: {}", e.getClass().getSimpleName());
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error. Please try again.");
  }
}
