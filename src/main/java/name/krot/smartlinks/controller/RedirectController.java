package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.service.RedirectResolver;
import name.krot.smartlinks.service.SmartLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController implements RedirectControllerApi {

    private final SmartLinkService smartLinkService;
    private final RedirectResolver redirectResolver;
    private final RequestContextFactory requestContextFactory;

    @Override
    public ResponseEntity<String> createSmartLink(@Valid @RequestBody SmartLink smartLink) {
        log.info("Received POST request, smartLink: {}", smartLink);

        smartLinkService.saveSmartLink(smartLink);
        return ResponseEntity.status(HttpStatus.CREATED).body("Smart Link created successfully");
    }

    @Override
    public ResponseEntity<Void> redirect(@Valid @PathVariable String smartLinkId, HttpServletRequest request) {
        log.info("Received GET request, smartLinkId: {}, HttpServletRequest: {}", smartLinkId, request);
        URI redirectUri = redirectResolver.resolveRedirect(smartLinkId, requestContextFactory.from(request));
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }
}
