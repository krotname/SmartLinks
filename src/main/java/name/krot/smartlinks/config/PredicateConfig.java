package name.krot.smartlinks.config;

import name.krot.smartlinks.predicate.DateRangePredicate;
import name.krot.smartlinks.predicate.DeviceTypePredicate;
import name.krot.smartlinks.predicate.LanguagePredicate;
import name.krot.smartlinks.predicate.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PredicateConfig {

    @Bean(name = "DateRange")
    public Predicate dateRangePredicate() {
        return new DateRangePredicate();
    }

    @Bean(name = "Language")
    public Predicate languagePredicate() {
        return new LanguagePredicate();
    }

    @Bean(name = "DeviceType")
    public Predicate deviceTypePredicate() {
        return new DeviceTypePredicate();
    }
}
