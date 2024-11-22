package name.krot.smartlinks.command;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.exception.ResourceNotFoundException;
import name.krot.smartlinks.model.Url;
import name.krot.smartlinks.service.UrlService;

@RequiredArgsConstructor
public class RedirectUrlCommand implements Command<Url> {
    private final UrlService urlService;
    private final String shortId;

    @Override
    public Url execute() {
        Url url = urlService.getLongUrl(shortId);
        if (url == null) {
            throw new ResourceNotFoundException("URL not found");
        }
        return url;
    }
}