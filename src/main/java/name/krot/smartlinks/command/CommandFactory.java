package name.krot.smartlinks.command;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.RequestData;
import name.krot.smartlinks.model.Url;
import name.krot.smartlinks.service.UrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class CommandFactory {
    private final UrlService urlService;
    private final Map<String, Function<RequestData, Command<?>>> commandMap = new HashMap<>();
    private final Map<String, ResponseHandler> responseHandlerMap = new HashMap<>();
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public CommandFactory(UrlService urlService) {
        this.urlService = urlService;
        initializeCommands();
    }

    private void initializeCommands() {
        commandMap.put("POST:/api/shorten", rd -> new ShortenUrlCommand(urlService, rd.getBody()));
        commandMap.put("GET:/api/statistics/{id}", rd -> new GetStatisticsCommand(urlService, rd.getPathVariable("id")));
        commandMap.put("GET:/{id}", rd -> new RedirectUrlCommand(urlService, rd.getPathVariable("id")));

        responseHandlerMap.put("POST:/api/shorten", result -> {
            String shortId = (String) result;
            String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/{id}")
                    .buildAndExpand(shortId)
                    .toUriString();
            return ResponseEntity.ok(shortUrl);
        });
        responseHandlerMap.put("GET:/api/statistics/{id}", result -> ResponseEntity.ok(result));
        responseHandlerMap.put("GET:/{id}", result -> {
            Url url = (Url) result;
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(url.getLongUrl()));
            return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
        });
    }

    public ResponseEntity<?> executeCommand(String method, String path, String body) {
        CommandKey commandKey = findMatchingKey(method, path);
        RequestData requestData = new RequestData(body, path, commandKey.pattern);
        Command<?> command = commandMap.get(commandKey.key).apply(requestData);
        Object result = command.execute();
        ResponseHandler handler = responseHandlerMap.get(commandKey.key);
        return handler.handle(result);
    }

    private CommandKey findMatchingKey(String method, String path) {
        for (String key : commandMap.keySet()) {
            String[] parts = key.split(":", 2);
            String keyMethod = parts[0];
            String keyPath = parts[1];
            if (method.equalsIgnoreCase(keyMethod) && pathMatcher.match(keyPath, path)) {
                return new CommandKey(key, keyPath);
            }
        }
        throw new UnsupportedOperationException("Unsupported operation");
    }

    private class CommandKey {
        String key;
        String pattern;

        CommandKey(String key, String pattern) {
            this.key = key;
            this.pattern = pattern;
        }
    }


}