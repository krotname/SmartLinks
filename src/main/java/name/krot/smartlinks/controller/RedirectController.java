package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.krot.smartlinks.command.Command;
import name.krot.smartlinks.command.CommandFactory;
import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.service.SmartLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final SmartLinkService smartLinkService;
    private final PredicateFactory predicateFactory;

    @PostMapping("/api/smartlinks")
    public ResponseEntity<String> createSmartLink(@RequestBody SmartLink smartLink) {
        log.info("Received POST request, smartLink: {}", smartLink);

        smartLinkService.saveSmartLink(smartLink);
        return ResponseEntity.status(HttpStatus.CREATED).body("Smart Link created successfully");
    }

    @GetMapping("/s/{smartLinkId}")
    public ResponseEntity<?> redirect(@PathVariable String smartLinkId, HttpServletRequest request) {
        log.info("Received GET request, smartLinkId: {}, HttpServletRequest: {}", smartLinkId, request);
        SmartLink smartLink = smartLinkService.getSmartLinkById(smartLinkId);
        if (smartLink == null) {
            throw new SmartLinkNotFoundException("Smart Link not found");
        }

        RequestContext context = buildRequestContext(request);

        CommandFactory commandFactory = new CommandFactory(predicateFactory);
        List<Command<ResponseEntity<?>>> commands = commandFactory.createCommands(context, smartLink);

        for (Command<ResponseEntity<?>> command : commands) {
            ResponseEntity<?> response = command.execute();
            if (response != null) {
                return response;
            }
        }

        throw new NoMatchingRuleException("No matching rule found for this Smart Link");
    }

    private RequestContext buildRequestContext(HttpServletRequest request) {
        RequestContext context = new RequestContext();
        context.setRequestTime(LocalDateTime.now());
        context.setAcceptLanguage(request.getHeader("Accept-Language"));
        context.setUserAgent(request.getHeader("User-Agent"));
        return context;
    }
}
