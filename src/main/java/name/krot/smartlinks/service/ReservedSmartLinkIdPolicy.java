package name.krot.smartlinks.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReservedSmartLinkIdPolicy implements SmartLinkIdPolicy {
    private final Set<String> reserved = new HashSet<>(Set.of(
            "api", "manage", "s", "actuator", "healthz", "assets", "privacy", "consent",
            "robots.txt", "sitemap.xml", "index.html", "404.html", "maintenance.html",
            "favicon.ico", "login", "logout", "error", "v3", "swagger-ui", "swagger-ui.html"));

    public ReservedSmartLinkIdPolicy(@Value("${smartlinks.reserved-codes:}") String extra) {
        for (String id : extra.split(",")) {
            if (!id.isBlank()) reserved.add(id.strip().toLowerCase(Locale.ROOT));
        }
    }

    @Override
    public void validate(String id) {
        if (id.startsWith(".") || reserved.contains(id.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Код занят служебным адресом сайта");
        }
    }
}
