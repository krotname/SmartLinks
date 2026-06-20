package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeviceTypePredicate implements Predicate {

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
        return devices.contains(deviceType);
    }
}
