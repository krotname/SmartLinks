package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.command.CommandFactory;
import name.krot.smartlinks.model.RequestData;
import name.krot.smartlinks.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UrlController {

    private final CommandFactory commandFactory;

    @Autowired
    public UrlController(UrlService urlService) {
        this.commandFactory = new CommandFactory(urlService);
    }

    @RequestMapping(value = {"/api/shorten", "/api/statistics/{id}", "/{id}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> handleRequest(HttpServletRequest request,
                                           @RequestBody(required = false) String body) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        try {
            return commandFactory.executeCommand(method, path, body);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Operation not supported");
        }
    }
}