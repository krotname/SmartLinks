package name.krot.smartlinks.service;

import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.repository.SmartLinkRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DefaultSmartLinkService implements SmartLinkService {

    private final SmartLinkRepository smartLinkRepository;

    public DefaultSmartLinkService(SmartLinkRepository smartLinkRepository) {
        this.smartLinkRepository = smartLinkRepository;
    }

    @Override
    public void saveSmartLink(SmartLink smartLink) {
        smartLinkRepository.save(smartLink);
    }

    @Override
    public Optional<SmartLink> findSmartLinkById(String id) {
        return smartLinkRepository.findById(id);
    }
}
