package name.krot.smartlinks.service;

import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.repository.SmartLinkRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import name.krot.smartlinks.exception.SmartLinkAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class SmartLinkServiceTest {

    @InjectMocks
    private DefaultSmartLinkService smartLinkService;

    @Mock
    private SmartLinkRepository smartLinkRepository;

    @Mock
    private SmartLinkDefinitionValidator definitionValidator;

    @Test
    void testGetSmartLinkById() {
        SmartLink smartLink = smartLink();

        when(smartLinkRepository.findById(SMART_LINK_ID)).thenReturn(java.util.Optional.of(smartLink));

        SmartLink result = smartLinkService.findSmartLinkById(SMART_LINK_ID).orElseThrow();

        assertNotNull(result);
        assertEquals(SMART_LINK_ID, result.getId());
    }

    @Test
    void testSaveSmartLink() {
        SmartLink smartLink = smartLink();
        when(smartLinkRepository.saveIfAbsent(smartLink)).thenReturn(true);

        smartLinkService.saveSmartLink(smartLink);

        verify(definitionValidator).validate(smartLink);
        verify(smartLinkRepository, times(1)).saveIfAbsent(smartLink);
    }

    @Test
    void duplicateIdIsRejectedInsteadOfOverwritingExistingRedirects() {
        SmartLink smartLink = smartLink();
        when(smartLinkRepository.saveIfAbsent(smartLink)).thenReturn(false);

        assertThrows(SmartLinkAlreadyExistsException.class,
                () -> smartLinkService.saveSmartLink(smartLink));
    }
}
