package name.krot.smartlinks.repository;

import java.util.Optional;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.model.VersionedSmartLink;

/** Persistence operations required by the editing use case only. */
public interface ConditionalSmartLinkRepository {
    Optional<VersionedSmartLink> findVersionedById(String id);

    Optional<VersionedSmartLink> replaceIfVersionMatches(SmartLink replacement, String expectedVersion);
}
