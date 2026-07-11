package name.krot.smartlinks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Predicate.not;

public class Rule implements Serializable {
    @Serial
    private static final long serialVersionUID = -4862926644813433701L;
    public static final int MAX_PREDICATES = 16;
    public static final int MAX_ARGUMENTS = 16;
    public static final int MAX_REDIRECT_LENGTH = 2048;

    @NotNull(message = "Predicates list cannot be null")
    @Size(max = MAX_PREDICATES, message = "Predicates list cannot exceed 16 entries")
    private List<@NotBlank(message = "Predicate name cannot be blank") String> predicates = new ArrayList<>();

    @NotNull(message = "Args cannot be null")
    @Size(max = MAX_ARGUMENTS, message = "Args cannot exceed 16 entries")
    private Map<String, Object> args = new HashMap<>();

    @NotNull(message = "Redirect URL cannot be null")
    @Size(max = MAX_REDIRECT_LENGTH, message = "Redirect URL cannot exceed 2048 characters")
    private String redirectTo;

    @JsonIgnore
    @AssertTrue(message = "Redirect URL must be an absolute http(s) URL with host")
    public boolean isRedirectToValid() {
        return isValidRedirectUrl(redirectTo);
    }

    public static boolean isValidRedirectUrl(String value) {
        return Optional.ofNullable(value)
                .filter(current -> current.length() <= MAX_REDIRECT_LENGTH)
                .map(String::trim)
                .filter(not(String::isBlank))
                .flatMap(Rule::parseUri)
                .filter(Rule::hasSupportedScheme)
                .filter(Rule::hasHost)
                .filter(Rule::hasNoUserInfo)
                .isPresent();
    }

    private static Optional<URI> parseUri(String value) {
        try {
            return Optional.of(URI.create(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static boolean hasSupportedScheme(URI uri) {
        return Optional.ofNullable(uri.getScheme())
                .filter(scheme -> scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                .isPresent();
    }

    private static boolean hasHost(URI uri) {
        return Optional.ofNullable(uri.getHost())
                .filter(not(String::isBlank))
                .isPresent();
    }

    private static boolean hasNoUserInfo(URI uri) {
        return uri.getRawUserInfo() == null;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<String> predicates) {
        this.predicates = predicates;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }
}
