package name.krot.smartlinks.predicate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeviceTypePredicate implements Predicate {

    private final DeviceTypeResolver deviceTypeResolver;

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
