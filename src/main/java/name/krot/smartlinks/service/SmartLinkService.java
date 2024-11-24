package name.krot.smartlinks.service;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.predicate.SmartLink;
import name.krot.smartlinks.repository.SmartLinkRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmartLinkService {

    private final SmartLinkRepository smartLinkRepository;
    public void saveSmartLink(SmartLink smartLink) {
        smartLinkRepository.save(smartLink);
    }

    public SmartLink getSmartLinkById(String id) {
        return smartLinkRepository.findById(id);
    }

}
