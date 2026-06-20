package name.krot.smartlinks.service;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.repository.SmartLinkRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultSmartLinkService implements SmartLinkService {

    private final SmartLinkRepository smartLinkRepository;

    @Override
    public void saveSmartLink(SmartLink smartLink) {
        smartLinkRepository.save(smartLink);
    }

    @Override
    public Optional<SmartLink> findSmartLinkById(String id) {
        return smartLinkRepository.findById(id);
    }
}
