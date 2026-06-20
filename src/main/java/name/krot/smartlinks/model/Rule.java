package name.krot.smartlinks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Rule implements Serializable {
    @Serial
    private static final long serialVersionUID = -4862926644813433701L;
    @NotNull(message = "Predicates list cannot be null")
    private List<@NotBlank(message = "Predicate name cannot be blank") String> predicates = new ArrayList<>();
    @NotNull(message = "Args cannot be null")
    private Map<String, Object> args = new HashMap<>();
    @NotNull(message = "Redirect URL cannot be null")
    @Size(max = 2048, message = "Redirect URL cannot exceed 2048 characters")
    private String redirectTo;

    @JsonIgnore
    @AssertTrue(message = "Redirect URL must be an absolute http(s) URL with host")
    public boolean isRedirectToValid() {
        return isValidRedirectUrl(redirectTo);
    }

    public static boolean isValidRedirectUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return false;
            }
            return uri.getRawUserInfo() == null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
