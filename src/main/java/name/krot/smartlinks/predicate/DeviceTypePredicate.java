package name.krot.smartlinks.predicate;

import java.util.List;
import java.util.Map;

public class DeviceTypePredicate implements Predicate {

    @Override
    public boolean evaluate(RequestContext context, Map<String, Object> args) {
        List<String> devices = (List<String>) args.get("devices");
        String userAgent = context.getUserAgent();
        String deviceType = getDeviceType(userAgent);
        return devices.contains(deviceType);
    }

    private String getDeviceType(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("Mobile") || userAgent.contains("iPhone") || userAgent.contains("Android")) {
            return "Mobile";
        } else {
            return "Desktop";
        }
    }
}