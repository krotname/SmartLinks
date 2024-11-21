package name.krot.smartlinks.command;

import org.springframework.http.ResponseEntity;

@FunctionalInterface
public interface ResponseHandler {
    ResponseEntity<?> handle(Object result);
}