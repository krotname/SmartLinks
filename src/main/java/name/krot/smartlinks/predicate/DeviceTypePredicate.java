package name.krot.smartlinks.predicate;

import java.util.List;
import java.util.Map;

public class DeviceTypePredicate implements Predicate {

    @Override
    public boolean evaluate(RequestContext context, Map<String, Object> args) {
        List<String> devices = (List<String>) args.get("devices");
        String userAgent = context.getUserAgent();
        // Логика определения типа устройства по userAgent
        String deviceType = getDeviceType(userAgent);
        return devices.contains(deviceType);
    }

    private String getDeviceType(String userAgent) {
        // Простая проверка, можно использовать библиотеку для парсинга userAgent
        if (userAgent.contains("Mobile")) {
            return "Mobile";
        } else {
            return "Desktop";
        }
    }
}