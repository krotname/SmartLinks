package name.krot.smartlinks.controller;

import lombok.extern.slf4j.Slf4j;
import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.ResourceNotFoundException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Error MethodArgumentNotValidException ", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
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

    @ExceptionHandler(SmartLinkNotFoundException.class)
    public ResponseEntity<String> handleSmartLinkNotFoundException(SmartLinkNotFoundException ex) {
        log.error("Error SmartLinkNotFoundException ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NoMatchingRuleException.class)
    public ResponseEntity<String> handleNoMatchingRuleException(NoMatchingRuleException ex) {
        log.error("Error NoMatchingRuleException ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred {}", Arrays.toString(ex.getStackTrace()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }

}
