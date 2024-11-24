package name.krot.smartlinks.controller;


import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.ResourceNotFoundException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleSmartLinkNotFoundException() {
        SmartLinkNotFoundException ex = new SmartLinkNotFoundException("Smart Link not found");
        ResponseEntity<String> response = exceptionHandler.handleSmartLinkNotFoundException(ex);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Smart Link not found", response.getBody());
    }

    @Test
    void testHandleNoMatchingRuleException() {
        NoMatchingRuleException ex = new NoMatchingRuleException("No matching rule found");
        ResponseEntity<String> response = exceptionHandler.handleNoMatchingRuleException(ex);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("No matching rule found", response.getBody());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<String> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid argument", response.getBody());
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        ResponseEntity<String> response = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Resource not found", response.getBody());
    }

    @Test
    void testHandleUnsupportedOperationException() {
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation not supported");
        ResponseEntity<String> response = exceptionHandler.handleUnsupportedOperationException(ex);

        assertEquals(501, response.getStatusCodeValue());
        assertEquals("Operation not supported", response.getBody());
    }

    @Test
    void testHandleAllExceptions() {
        Exception ex = new Exception("Internal Server Error");
        ResponseEntity<String> response = exceptionHandler.handleAllExceptions(ex);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Internal Server Error", response.getBody());
    }

}