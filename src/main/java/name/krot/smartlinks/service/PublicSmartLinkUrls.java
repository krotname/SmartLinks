package name.krot.smartlinks.service;

import java.net.URI;
import name.krot.smartlinks.model.SmartLink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PublicSmartLinkUrls implements SmartLinkUrlFactory {
    private final String base;

    public PublicSmartLinkUrls(@Value("${smartlinks.public-base-url:https://n-0.ru}") String base) {
        URI uri = URI.create(base);
        if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null
                || uri.getRawQuery() != null || uri.getRawFragment() != null
                || !(uri.getPath().isEmpty() || "/".equals(uri.getPath()))) {
            throw new IllegalArgumentException("public-base-url must be an HTTPS origin");
        }
        this.base = base.replaceAll("/$", "");
    }

    @Override
    public String urlFor(String id) {
        if (!SmartLink.isValidId(id)) throw new IllegalArgumentException("Invalid Smart Link id");
        return base + "/" + id;
    }
}
