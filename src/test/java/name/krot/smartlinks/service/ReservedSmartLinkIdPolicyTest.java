package name.krot.smartlinks.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReservedSmartLinkIdPolicyTest {
    private final SmartLinkIdPolicy policy = new ReservedSmartLinkIdPolicy("custom");
    @Test void protectsSiteAndServicePathsCaseInsensitively() {
        for (String id : new String[]{"api", "Privacy", "manage", "assets", "robots.txt", ".hidden", "custom"}) {
            assertThrows(IllegalArgumentException.class, () -> policy.validate(id), id);
        }
    }
    @Test void permitsOrdinaryCodes() {
        assertDoesNotThrow(() -> policy.validate("offer_2026.v1-a"));
    }
}
