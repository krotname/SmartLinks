package name.krot.smartlinks.predicate;

import java.time.LocalDateTime;

public record RequestContext(
        LocalDateTime requestTime,
        String acceptLanguage,
        String userAgent
) {
}
