package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.predicate.Rule;
import name.krot.smartlinks.predicate.SmartLink;
import name.krot.smartlinks.service.SmartLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
        smartLinkService.saveSmartLink(smartLink);
        return ResponseEntity.status(HttpStatus.CREATED).body("Smart Link created successfully");
    }

    @GetMapping("/s/{smartLinkId}")
    public ResponseEntity<?> redirect(@PathVariable String smartLinkId, HttpServletRequest request) {
        log.info("Received request for SmartLink ID: {}", smartLinkId);

        SmartLink smartLink = smartLinkService.getSmartLinkById(smartLinkId);
        if (smartLink == null) {
            throw new SmartLinkNotFoundException("Smart Link not found");
        }

        List<Rule> rules = smartLink.getRules();
        if (rules == null || rules.isEmpty()) {
            throw new NoMatchingRuleException("No rules defined for this Smart Link");
        }

        RequestContext context = buildRequestContext(request);

        for (Rule rule : rules) {
            boolean allPredicatesTrue = true;
            for (String predicateName : rule.getPredicates()) {
                Predicate predicate = predicateFactory.createPredicate(predicateName);
                if (!predicate.evaluate(context, rule.getArgs())) {
                    allPredicatesTrue = false;
                    break;
                }
            }
            if (allPredicatesTrue) {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(rule.getRedirectTo()));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
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