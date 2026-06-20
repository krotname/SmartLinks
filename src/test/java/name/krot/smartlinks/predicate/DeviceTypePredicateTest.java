package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.deviceArguments;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DeviceTypePredicateTest {

    private final DeviceTypePredicate deviceTypePredicate = new DeviceTypePredicate(new UserAgentDeviceTypeResolver());

    @Test
    void testMobileDevice() {
        RequestContext context = requestContext(
                null,
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X)"
        );

        boolean result = deviceTypePredicate.evaluate(context, deviceArguments(List.of("Mobile")));
        assertTrue(result);
    }

    @Test
    void testDesktopDevice() {
        RequestContext context = requestContext(
                null,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        );

        boolean result = deviceTypePredicate.evaluate(context, deviceArguments(List.of("Desktop")));
        assertTrue(result);
    }

    @Test
    void testUnknownDevice() {
        boolean result = deviceTypePredicate.evaluate(
                requestContext(null, null),
                deviceArguments(Arrays.asList("Mobile", "Desktop"))
        );
        assertFalse(result);
    }

    @Test
    void testDeviceNotInList() {
        RequestContext context = requestContext(
                null,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        );

        boolean result = deviceTypePredicate.evaluate(context, deviceArguments(List.of("Mobile")));
        assertFalse(result);
    }
}
