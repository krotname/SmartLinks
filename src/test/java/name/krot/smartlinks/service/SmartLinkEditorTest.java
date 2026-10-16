package name.krot.smartlinks.service;

import java.util.Optional;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.model.VersionedSmartLink;
import name.krot.smartlinks.repository.ConditionalSmartLinkRepository;
import org.junit.jupiter.api.Test;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmartLinkEditorTest {
    private final ConditionalSmartLinkRepository repository = mock(ConditionalSmartLinkRepository.class);
    private final SmartLinkDefinitionValidator validator = mock(SmartLinkDefinitionValidator.class);
    private final SmartLinkEditor editor = new DefaultSmartLinkEditor(repository, validator);

    @Test void validatesAndAtomicallyReplacesAnExistingDefinition() {
        var replacement = smartLink(fallbackRule("https://example.com/new"));
        var saved = new VersionedSmartLink(replacement, "new-version");
        when(repository.findVersionedById(SMART_LINK_ID))
                .thenReturn(Optional.of(new VersionedSmartLink(replacement, "old-version")));
        when(repository.replaceIfVersionMatches(replacement, "old-version")).thenReturn(Optional.of(saved));
        assertSame(saved, editor.update(SMART_LINK_ID, replacement, "old-version"));
        var order = inOrder(validator, repository);
        order.verify(validator).validate(replacement);
        order.verify(repository).findVersionedById(SMART_LINK_ID);
        order.verify(repository).replaceIfVersionMatches(replacement, "old-version");
    }

    @Test void neverRenamesAShortCode() {
        var replacement = smartLink(fallbackRule("https://example.com/new"));
        assertThrows(IllegalArgumentException.class, () -> editor.update("another", replacement, "0"));
        verifyNoInteractions(repository);
    }

    @Test void unknownCodeCannotBeCreatedByEditing() {
        when(repository.findVersionedById(SMART_LINK_ID)).thenReturn(Optional.empty());
        assertThrows(SmartLinkNotFoundException.class, () -> editor.update(SMART_LINK_ID,
                smartLink(fallbackRule("https://example.com/new")), "0"));
        verify(repository, never()).replaceIfVersionMatches(any(), any());
    }

    @Test void staleVersionCannotOverwriteAnotherEditorsChanges() {
        var replacement = smartLink(fallbackRule("https://example.com/new"));
        when(repository.findVersionedById(SMART_LINK_ID))
                .thenReturn(Optional.of(new VersionedSmartLink(replacement, "latest")));
        assertThrows(name.krot.smartlinks.exception.SmartLinkVersionConflictException.class,
                () -> editor.update(SMART_LINK_ID, replacement, "stale"));
        verify(repository, never()).replaceIfVersionMatches(any(), any());
    }

    @Test void detectsAChangeBetweenReadAndWrite() {
        var replacement = smartLink(fallbackRule("https://example.com/new"));
        when(repository.findVersionedById(SMART_LINK_ID))
                .thenReturn(Optional.of(new VersionedSmartLink(replacement, "0")));
        when(repository.replaceIfVersionMatches(replacement, "0")).thenReturn(Optional.empty());
        assertThrows(name.krot.smartlinks.exception.SmartLinkVersionConflictException.class,
                () -> editor.update(SMART_LINK_ID, replacement, "0"));
    }

    @Test void invalidDefinitionDoesNotTouchStorage() {
        var replacement = smartLink(fallbackRule("javascript:alert(1)"));
        doThrow(new IllegalArgumentException("invalid")).when(validator).validate(replacement);
        assertThrows(IllegalArgumentException.class, () -> editor.update(SMART_LINK_ID, replacement, "0"));
        verifyNoInteractions(repository);
    }
}
