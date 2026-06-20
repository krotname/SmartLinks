package name.krot.smartlinks.service;

import name.krot.smartlinks.model.SmartLink;

import java.util.Optional;

public interface SmartLinkService {
    void saveSmartLink(SmartLink smartLink);

    Optional<SmartLink> findSmartLinkById(String id);
}
