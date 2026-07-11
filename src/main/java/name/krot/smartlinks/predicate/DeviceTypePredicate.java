package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DeviceTypePredicate implements Predicate {

    private static final Set<String> SUPPORTED_DEVICES = Set.of("mobile", "desktop", "unknown");

    private final DeviceTypeResolver deviceTypeResolver;

    public DeviceTypePredicate(DeviceTypeResolver deviceTypeResolver) {
        this.deviceTypeResolver = deviceTypeResolver;
    }

    @Override
    public String name() {
        return "DeviceType";
    }

    @Override
    public boolean evaluate(RequestContext context, PredicateArguments arguments) {
        List<String> devices = arguments.getStringList("devices");
        String deviceType = deviceTypeResolver.resolve(context.userAgent());
        return devices.stream().anyMatch(device -> device.equalsIgnoreCase(deviceType));
    }

    @Override
    public void validateArguments(PredicateArguments arguments) {
        List<String> devices = arguments.getStringList("devices");
        if (devices.isEmpty()) {
            throw new IllegalArgumentException("DeviceType predicate requires at least one device type");
        }
        for (String device : devices) {
            if (device == null || !SUPPORTED_DEVICES.contains(device.toLowerCase(java.util.Locale.ROOT))) {
                throw new IllegalArgumentException("Unsupported device type: " + device);
            }
        }
    }
}
