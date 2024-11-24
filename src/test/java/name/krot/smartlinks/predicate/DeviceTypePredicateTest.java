package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DeviceTypePredicateTest {

    private final DeviceTypePredicate deviceTypePredicate = new DeviceTypePredicate();

    @Test
    void testMobileDevice() {
        RequestContext context = new RequestContext();
        context.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X)");

        Map<String, Object> args = new HashMap<>();
        args.put("devices", List.of("Mobile"));

        boolean result = deviceTypePredicate.evaluate(context, args);
        assertTrue(result);
    }

    @Test
    void testDesktopDevice() {
        RequestContext context = new RequestContext();
        context.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        Map<String, Object> args = new HashMap<>();
        args.put("devices", List.of("Desktop"));

        boolean result = deviceTypePredicate.evaluate(context, args);
        assertTrue(result);
    }

    @Test
    void testUnknownDevice() {
        RequestContext context = new RequestContext();
        context.setUserAgent(null);

        Map<String, Object> args = new HashMap<>();
        args.put("devices", Arrays.asList("Mobile", "Desktop"));

        boolean result = deviceTypePredicate.evaluate(context, args);
        assertFalse(result);
    }

    @Test
    void testDeviceNotInList() {
        RequestContext context = new RequestContext();
        context.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        Map<String, Object> args = new HashMap<>();
        args.put("devices", List.of("Mobile"));

        boolean result = deviceTypePredicate.evaluate(context, args);
        assertFalse(result);
    }
}