package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.service.RedirectResolver;
import name.krot.smartlinks.service.SmartLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController implements RedirectControllerApi {

    private static final Logger log = LoggerFactory.getLogger(RedirectController.class);

    private final SmartLinkService smartLinkService;
    private final RedirectResolver redirectResolver;
    private final RequestContextFactory requestContextFactory;

    public RedirectController(
            SmartLinkService smartLinkService,
            RedirectResolver redirectResolver,
            RequestContextFactory requestContextFactory
    ) {
        this.smartLinkService = smartLinkService;
        this.redirectResolver = redirectResolver;
        this.requestContextFactory = requestContextFactory;
    }

    @Override
    public ResponseEntity<String> createSmartLink(@Valid @RequestBody SmartLink smartLink) {
        log.info("Received POST request, smartLink: {}", smartLink);

        smartLinkService.saveSmartLink(smartLink);
        return ResponseEntity.status(HttpStatus.CREATED).body("Smart Link created successfully");
    }

    @Override
    public ResponseEntity<Void> redirect(@Valid @PathVariable String smartLinkId, HttpServletRequest request) {
        if (!SmartLink.isValidId(smartLinkId)) {
            throw new IllegalArgumentException("Smart Link id contains unsupported characters or is too long");
        }
        log.info("Received GET request, smartLinkId: {}, HttpServletRequest: {}", smartLinkId, request);
        URI redirectUri = redirectResolver.resolveRedirect(smartLinkId, requestContextFactory.from(request));
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }
}
