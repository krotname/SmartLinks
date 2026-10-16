package name.krot.smartlinks.service;

import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.model.VersionedSmartLink;

public interface SmartLinkEditor {
    VersionedSmartLink read(String id);

    VersionedSmartLink update(String id, SmartLink replacement, String expectedVersion);
}
