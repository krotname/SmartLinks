package name.krot.smartlinks.repository;

import name.krot.smartlinks.model.SmartLink;

import java.util.Optional;

public interface SmartLinkRepository {
    boolean saveIfAbsent(SmartLink smartLink);

    Optional<SmartLink> findById(String id);
}

