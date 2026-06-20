package name.krot.smartlinks.model;


import org.junit.jupiter.api.Test;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.RU_REDIRECT_URL;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SmartLinkTest {

    @Test
    void testSmartLinkGettersAndSetters() {
        SmartLink smartLink = smartLink(fallbackRule(RU_REDIRECT_URL));

        assertEquals(SMART_LINK_ID, smartLink.getId());
        assertEquals(1, smartLink.getRules().size());
        assertEquals(RU_REDIRECT_URL, smartLink.getRules().get(0).getRedirectTo());
    }
}
