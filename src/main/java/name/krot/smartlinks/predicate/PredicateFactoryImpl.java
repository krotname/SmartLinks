package name.krot.smartlinks.predicate;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PredicateFactoryImpl implements PredicateFactory {

    private final ApplicationContext applicationContext;

    @Override
    public Predicate createPredicate(String name) {
        return (Predicate) applicationContext.getBean(name);
    }
}