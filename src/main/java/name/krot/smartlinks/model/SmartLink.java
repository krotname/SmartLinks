package name.krot.smartlinks.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartLink implements Serializable {
    @Serial
    private static final long serialVersionUID = -4862926644813433702L;

    @NotBlank(message = "Smart Link id cannot be blank")
    @Size(max = 128, message = "Smart Link id cannot exceed 128 characters")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Smart Link id contains unsupported characters")
    private String id;
    @Valid
    @NotEmpty(message = "Rules list cannot be empty")
    private List<Rule> rules = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
