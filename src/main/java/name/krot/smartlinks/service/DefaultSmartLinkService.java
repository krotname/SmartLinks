package name.krot.smartlinks.service;

import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.exception.SmartLinkAlreadyExistsException;
import name.krot.smartlinks.repository.SmartLinkRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DefaultSmartLinkService implements SmartLinkService {

    private final SmartLinkRepository smartLinkRepository;
    private final SmartLinkDefinitionValidator definitionValidator;

    public DefaultSmartLinkService(SmartLinkRepository smartLinkRepository,
                                   SmartLinkDefinitionValidator definitionValidator) {
        this.smartLinkRepository = smartLinkRepository;
        this.definitionValidator = definitionValidator;
    }

    @Override
    public void saveSmartLink(SmartLink smartLink) {
        definitionValidator.validate(smartLink);
        if (!smartLinkRepository.saveIfAbsent(smartLink)) {
            throw new SmartLinkAlreadyExistsException("Smart Link already exists: " + smartLink.getId());
        }
    }

    @Override
    public Optional<SmartLink> findSmartLinkById(String id) {
        return smartLinkRepository.findById(id);
    }
}
