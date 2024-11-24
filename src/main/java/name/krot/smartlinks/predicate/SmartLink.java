package name.krot.smartlinks.predicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartLink implements Serializable {
    @Serial
    private static final long serialVersionUID = -4862926644813433702L;

    private String id;
    private List<Rule> rules = new ArrayList<>();
}
