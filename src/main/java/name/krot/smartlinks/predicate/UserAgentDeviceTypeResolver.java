package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class UserAgentDeviceTypeResolver implements DeviceTypeResolver {

    private static final List<DeviceTypeRule> RULES = List.of(
            new DeviceTypeRule(userAgent -> userAgent.filter(UserAgentDeviceTypeResolver::isMobile)
                    .map(ignored -> "Mobile")),
            new DeviceTypeRule(userAgent -> userAgent.filter(currentUserAgent -> !isMobile(currentUserAgent))
                    .map(ignored -> "Desktop")),
            new DeviceTypeRule(userAgent -> Optional.of("Unknown"))
    );

    @Override
    public String resolve(String userAgent) {
        Optional<String> currentUserAgent = Optional.ofNullable(userAgent)
                .filter(current -> !current.isBlank());
        return RULES.stream()
                .map(rule -> rule.resolve(currentUserAgent))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse("Unknown");
    }

    private static boolean isMobile(String userAgent) {
        String normalized = userAgent.toLowerCase(Locale.ROOT);
        return Stream.of("mobile", "iphone", "ipad", "android").anyMatch(normalized::contains);
    }

    private record DeviceTypeRule(Function<Optional<String>, Optional<String>> resolver) {
        private Optional<String> resolve(Optional<String> userAgent) {
            return resolver.apply(userAgent);
        }
    }
}
