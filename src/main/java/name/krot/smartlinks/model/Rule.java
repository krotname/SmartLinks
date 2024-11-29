package name.krot.smartlinks.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class Rule implements Serializable {
    @Serial
    private static final long serialVersionUID = -4862926644813433701L;
    @NotEmpty(message = "Predicates list cannot be empty")
    private List<String> predicates;
    @NotNull(message = "Args cannot be null")
    private Map<String, Object> args;
    @NotNull(message = "Redirect URL cannot be null")
    @Size(max = 2048, message = "Redirect URL cannot exceed 2048 characters")
    @Pattern(regexp = "^(http|https)://.*$", message = "Redirect URL must be a valid URL")
    private String redirectTo;
}