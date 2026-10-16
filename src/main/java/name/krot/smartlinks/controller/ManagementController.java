package name.krot.smartlinks.controller;

import jakarta.validation.Valid;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.model.VersionedSmartLink;
import name.krot.smartlinks.service.SmartLinkService;
import name.krot.smartlinks.service.SmartLinkEditor;
import name.krot.smartlinks.service.SmartLinkUrlFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage/api/smartlinks")
public class ManagementController {
    private final SmartLinkService service;
    private final SmartLinkUrlFactory urls;
    private final SmartLinkEditor editor;

    public ManagementController(SmartLinkService service, SmartLinkUrlFactory urls, SmartLinkEditor editor) {
        this.service = service;
        this.urls = urls;
        this.editor = editor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedLink create(@Valid @RequestBody SmartLink link) {
        service.saveSmartLink(link);
        return new CreatedLink(link.getId(), urls.urlFor(link.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmartLink> read(@PathVariable String id) {
        return response(editor.read(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SmartLink> update(@PathVariable String id, @Valid @RequestBody SmartLink link,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        if (ifMatch == null) throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED);
        if (!ifMatch.matches("\"(0|[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\"")) {
            throw new IllegalArgumentException("If-Match must contain the exact strong ETag returned by GET");
        }
        return response(editor.update(id, link, ifMatch.substring(1, ifMatch.length() - 1)));
    }

    private ResponseEntity<SmartLink> response(VersionedSmartLink saved) {
        return ResponseEntity.ok().eTag("\"" + saved.version() + "\"")
                .header("Content-Location", urls.urlFor(saved.definition().getId())).body(saved.definition());
    }

    public record CreatedLink(String id, String url) { }
}
