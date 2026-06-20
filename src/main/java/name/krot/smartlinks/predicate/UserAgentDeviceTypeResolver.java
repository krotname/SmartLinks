package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

@Component
public class UserAgentDeviceTypeResolver implements DeviceTypeResolver {

    @Override
    public String resolve(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("Mobile") || userAgent.contains("iPhone") || userAgent.contains("Android")) {
            return "Mobile";
        }
        return "Desktop";
    }
}
