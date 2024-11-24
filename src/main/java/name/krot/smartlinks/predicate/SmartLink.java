package name.krot.smartlinks.predicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartLink implements Serializable {
    static final long SerialVersionUID = -4862926644813433702L;
    private String id;
    private List<Rule> rules  = new ArrayList<>();
}