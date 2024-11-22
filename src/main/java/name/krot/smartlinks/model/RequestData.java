package name.krot.smartlinks.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Map;

@RequiredArgsConstructor
public class RequestData {
    @Getter
    private final String body;
    private final String path;
    private final String matchingPattern;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public String getPathVariable(String variableName) {
        Map<String, String> variables = pathMatcher.extractUriTemplateVariables(matchingPattern, path);
        return variables.get(variableName);
    }
}