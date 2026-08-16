package name.krot.smartlinks.controller;

import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.ResourceNotFoundException;
import name.krot.smartlinks.exception.SmartLinkAlreadyExistsException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Error MethodArgumentNotValidException ", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError fieldError
                    ? fieldError.getField()
                    : objectErrorKey(error);
            String errorMessage = error.getDefaultMessage();
            // Одно поле легко нарушает два правила сразу (@NotBlank и @Pattern):
            // put затирал первое сообщение вторым, и клиент видел только половину причины.
            errors.merge(fieldName, errorMessage,
                    (existing, added) -> existing.equals(added) ? existing : existing + "; " + added);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    private static String objectErrorKey(ObjectError error) {
        return error.getObjectName() == null || error.getObjectName().isBlank()
                ? "request"
                : error.getObjectName();
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        log.error("Error UnsupportedOperationException ", ex);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Error ResourceNotFoundException ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Error IllegalArgumentException ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Request body could not be parsed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed request body");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        log.error("Backing store is unavailable", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, "1")
                .body("Storage temporarily unavailable");
    }

    @ExceptionHandler(SmartLinkNotFoundException.class)
    public ResponseEntity<String> handleSmartLinkNotFoundException(SmartLinkNotFoundException ex) {
        log.error("Error SmartLinkNotFoundException ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SmartLinkAlreadyExistsException.class)
    public ResponseEntity<String> handleSmartLinkAlreadyExistsException(SmartLinkAlreadyExistsException ex) {
        log.warn("Smart Link create conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NoMatchingRuleException.class)
    public ResponseEntity<String> handleNoMatchingRuleException(NoMatchingRuleException ex) {
        log.error("Error NoMatchingRuleException ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            log.warn("Framework rejected request with status {}: {}",
                    errorResponse.getStatusCode(), ex.getMessage());
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .headers(errorResponse.getHeaders())
                    .body(errorResponse.getStatusCode().toString());
        }
        log.error("An unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }

}
