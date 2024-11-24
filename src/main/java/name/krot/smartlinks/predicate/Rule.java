package name.krot.smartlinks.predicate;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class Rule implements Serializable {
    static final long SerialVersionUID = -4862926644813433702L;

    private List<String> predicates;
    private Map<String, Object> args;
    private String redirectTo;
}