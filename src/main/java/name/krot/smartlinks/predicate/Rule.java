package name.krot.smartlinks.predicate;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class Rule implements Serializable {
    @Serial
    private static final long serialVersionUID = -4862926644813433701L;

    private List<String> predicates;
    private Map<String, Object> args;
    private String redirectTo;
}