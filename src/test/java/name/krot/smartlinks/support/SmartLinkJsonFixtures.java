package name.krot.smartlinks.support;

public final class SmartLinkJsonFixtures {

    private SmartLinkJsonFixtures() {
    }

    public static String validSmartLinkJson() {
        return """
                {
                  "id": "smartlink123",
                  "rules": [
                    {
                      "predicates": ["DateRange", "Language"],
                      "args": {
                        "startWith": "2024-11-01T00:00:00",
                        "endWith": "2024-12-01T00:00:00",
                        "language": ["ru", "ru-RU"]
                      },
                      "redirectTo": "https://otus.ru/ru"
                    }
                  ]
                }
                """;
    }

    public static String fallbackSmartLinkJson() {
        return """
                {
                  "id": "smartlink123",
                  "rules": [
                    {
                      "predicates": [],
                      "args": {},
                      "redirectTo": "https://otus.ru/default"
                    }
                  ]
                }
                """;
    }

    public static String invalidRedirectSmartLinkJson() {
        return """
                {
                  "id": "smartlink123",
                  "rules": [
                    {
                      "predicates": ["Language"],
                      "args": {
                        "language": ["ru"]
                      },
                      "redirectTo": "https:otus.ru/no-host"
                    }
                  ]
                }
                """;
    }
}
