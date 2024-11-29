package name.krot.smartlinks.predicate;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestContext {
    private LocalDateTime requestTime;
    private String acceptLanguage;
    private String userAgent;
}
