package name.krot.smartlinks.service;

import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.exception.SmartLinkVersionConflictException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.model.VersionedSmartLink;
import name.krot.smartlinks.repository.ConditionalSmartLinkRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultSmartLinkEditor implements SmartLinkEditor {
    private final ConditionalSmartLinkRepository repository;
    private final SmartLinkDefinitionValidator validator;

    public DefaultSmartLinkEditor(ConditionalSmartLinkRepository repository, SmartLinkDefinitionValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public VersionedSmartLink read(String id) {
        if (!SmartLink.isValidId(id)) throw new IllegalArgumentException("Invalid short code");
        return repository.findVersionedById(id)
                .orElseThrow(() -> new SmartLinkNotFoundException("Smart Link not found"));
    }

    @Override
    public VersionedSmartLink update(String id, SmartLink replacement, String expectedVersion) {
        validator.validate(replacement);
        if (!id.equals(replacement.getId())) throw new IllegalArgumentException("The short code cannot be changed");
        VersionedSmartLink current = read(id);
        if (!current.version().equals(expectedVersion)) throw new SmartLinkVersionConflictException();
        return repository.replaceIfVersionMatches(replacement, expectedVersion)
                .orElseThrow(SmartLinkVersionConflictException::new);
    }
}
