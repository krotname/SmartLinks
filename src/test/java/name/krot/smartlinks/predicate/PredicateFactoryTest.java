package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

class PredicateFactoryTest {

    @Test
    void testCreatePredicate() {
        Predicate mockPredicate = mock(Predicate.class);
        when(mockPredicate.name()).thenReturn("DateRange");

        PredicateFactoryImpl predicateFactory = new PredicateFactoryImpl(List.of(mockPredicate));

        Predicate predicate = predicateFactory.createPredicate("DateRange");

        assertNotNull(predicate);
        assertEquals(mockPredicate, predicate);
    }

    @Test
    void throwsForUnknownPredicate() {
        PredicateFactoryImpl predicateFactory = new PredicateFactoryImpl(List.of());

        assertThrows(IllegalArgumentException.class, () -> predicateFactory.createPredicate("Unknown"));
    }
}
