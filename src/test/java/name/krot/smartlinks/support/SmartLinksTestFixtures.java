package name.krot.smartlinks.support;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.PredicateArguments;
import name.krot.smartlinks.predicate.RequestContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SmartLinksTestFixtures {

    public static final String SMART_LINK_ID = "smartlink123";
    public static final String RU_REDIRECT_URL = "https://otus.ru/ru";
    public static final String DEFAULT_REDIRECT_URL = "https://otus.ru/default";
    public static final LocalDateTime REQUEST_TIME = LocalDateTime.of(2024, 11, 15, 12, 0);

    private SmartLinksTestFixtures() {
    }

    public static SmartLink smartLink(Rule... rules) {
        SmartLink smartLink = new SmartLink();
        smartLink.setId(SMART_LINK_ID);
        smartLink.setRules(Arrays.asList(rules));
        return smartLink;
    }

    public static Rule rule(List<String> predicates, String redirectTo) {
        return rule(predicates, redirectTo, defaultPredicateArgumentsMap());
    }

    public static Rule rule(List<String> predicates, String redirectTo, Map<String, Object> args) {
        Rule rule = new Rule();
        rule.setPredicates(predicates);
        rule.setArgs(args);
        rule.setRedirectTo(redirectTo);
        return rule;
    }

    public static Rule fallbackRule(String redirectTo) {
        return rule(List.of(), redirectTo, Map.of());
    }

    public static RequestContext requestContext(String acceptLanguage) {
        return new RequestContext(REQUEST_TIME, acceptLanguage, null);
    }

    public static RequestContext requestContext(String acceptLanguage, String userAgent) {
        return new RequestContext(REQUEST_TIME, acceptLanguage, userAgent);
    }

    public static RequestContext requestContextAt(LocalDateTime requestTime) {
        return new RequestContext(requestTime, null, null);
    }

    public static PredicateArguments languageArguments(List<String> languages) {
        return new PredicateArguments(Map.of("language", languages));
    }

    public static PredicateArguments deviceArguments(List<String> devices) {
        return new PredicateArguments(Map.of("devices", devices));
    }

    public static PredicateArguments dateRangeArguments(String start, String end) {
        Map<String, Object> args = new HashMap<>();
        args.put("startWith", start);
        args.put("endWith", end);
        return new PredicateArguments(args);
    }

    public static Map<String, Object> defaultPredicateArgumentsMap() {
        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");
        args.put("language", Arrays.asList("ru", "ru-RU"));
        return args;
    }
}
