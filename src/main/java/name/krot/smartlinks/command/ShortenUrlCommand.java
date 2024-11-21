package name.krot.smartlinks.command;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.service.UrlService;

@RequiredArgsConstructor
public class ShortenUrlCommand implements Command<String> {
    private final UrlService urlService;
    private final String longUrl;

    @Override
    public String execute() {
        return urlService.shortenUrl(longUrl);
    }
}