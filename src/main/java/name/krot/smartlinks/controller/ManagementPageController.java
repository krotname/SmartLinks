package name.krot.smartlinks.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManagementPageController {
    @GetMapping(value = {"/manage", "/manage/"}, produces = MediaType.TEXT_HTML_VALUE)
    public ClassPathResource index() {
        return new ClassPathResource("static/manage/index.html");
    }

    @GetMapping(value = "/manage/login", produces = MediaType.TEXT_HTML_VALUE)
    public ClassPathResource login() {
        return new ClassPathResource("static/manage/login.html");
    }

    @GetMapping("/manage/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
