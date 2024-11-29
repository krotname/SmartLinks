package name.krot.smartlinks.service;

import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.repository.SmartLinkRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmartLinkServiceTest {

    @InjectMocks
    private SmartLinkService smartLinkService;

    @Mock
    private SmartLinkRepository smartLinkRepository;

    public SmartLinkServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSmartLinkById() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");

        when(smartLinkRepository.findById("smartlink123")).thenReturn(smartLink);

        SmartLink result = smartLinkService.getSmartLinkById("smartlink123");

        assertNotNull(result);
        assertEquals("smartlink123", result.getId());
    }

    @Test
    void testSaveSmartLink() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");

        smartLinkService.saveSmartLink(smartLink);

        verify(smartLinkRepository, times(1)).save(smartLink);
    }
}