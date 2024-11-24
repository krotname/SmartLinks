package name.krot.smartlinks.model;


import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SmartLinkTest {

    @Test
    void testSmartLinkGettersAndSetters() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        assertEquals("smartlink123", smartLink.getId());

        Rule rule = new Rule();
        rule.setRedirectTo("https://example.com");
        smartLink.setRules(Collections.singletonList(rule));
        assertEquals(1, smartLink.getRules().size());
        assertEquals("https://example.com", smartLink.getRules().get(0).getRedirectTo());
    }
}
