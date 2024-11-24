package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PredicateFactoryTest {

    @InjectMocks
    private PredicateFactoryImpl predicateFactory;

    @Mock
    private ApplicationContext applicationContext;

    public PredicateFactoryTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePredicate() {
        Predicate mockPredicate = mock(Predicate.class);
        when(applicationContext.getBean("DateRange")).thenReturn(mockPredicate);

        Predicate predicate = predicateFactory.createPredicate("DateRange");

        assertNotNull(predicate);
        assertEquals(mockPredicate, predicate);
    }
}