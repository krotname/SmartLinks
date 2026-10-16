package name.krot.smartlinks.controller;

import jakarta.validation.Valid;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.service.SmartLinkService;
import name.krot.smartlinks.service.SmartLinkUrlFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage/api/smartlinks")
public class ManagementController {
    private final SmartLinkService service;
    private final SmartLinkUrlFactory urls;

    public ManagementController(SmartLinkService service, SmartLinkUrlFactory urls) {
        this.service = service;
        this.urls = urls;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedLink create(@Valid @RequestBody SmartLink link) {
        service.saveSmartLink(link);
        return new CreatedLink(link.getId(), urls.urlFor(link.getId()));
    }

    @GetMapping("/{id}")
    public SmartLink read(@PathVariable String id) {
        return service.findSmartLinkById(id)
                .orElseThrow(() -> new SmartLinkNotFoundException("Ссылка не найдена"));
    }

    public record CreatedLink(String id, String url) { }
}
