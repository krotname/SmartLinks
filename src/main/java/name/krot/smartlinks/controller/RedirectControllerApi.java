package name.krot.smartlinks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import name.krot.smartlinks.config.OpenApiConfig;
import name.krot.smartlinks.model.SmartLink;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface RedirectControllerApi {
    @PostMapping("/api/smartlinks")
    @SecurityRequirement(name = OpenApiConfig.API_KEY_SCHEME)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Умная ссылка создана"),
            @ApiResponse(responseCode = "401",
                    description = "Отсутствует или неверный заголовок X-API-Key",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Умная ссылка с таким id уже существует",
                    content = @Content)
    })
    @Operation(
            summary = "Создать умную ссылку",
            description = "Метод для создания новой умной ссылки. "
                    + "Требуется заголовок X-API-Key со значением smartlinks.api-key (env SMARTLINKS_API_KEY).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные SmartLink",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmartLink.class),
                            examples = @ExampleObject(
                                    name = "Пример SmartLink",
                                    value = "{\n" +
                                            "  \"id\": \"smartlink123456\",\n" +
                                            "  \"rules\": [\n" +
                                            "    {\n" +
                                            "      \"predicates\": [\"DateRange\", \"Language\"],\n" +
                                            "      \"args\": {\n" +
                                            "        \"startWith\": \"2024-11-01T00:00:00\",\n" +
                                            "        \"endWith\": \"2024-12-01T00:00:00\",\n" +
                                            "        \"language\": [\"ru\", \"ru-RU\"]\n" +
                                            "      },\n" +
                                            "      \"redirectTo\": \"https://otus.ru/ru\"\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"predicates\": [\"Language\"],\n" +
                                            "      \"args\": {\n" +
                                            "        \"language\": [\"en\", \"en-US\"]\n" +
                                            "      },\n" +
                                            "      \"redirectTo\": \"https://otus.ru/en\"\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"predicates\": [],\n" +
                                            "      \"args\": {},\n" +
                                            "      \"redirectTo\": \"https://otus.ru/default\"\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
    )
    ResponseEntity<String> createSmartLink(@Valid @RequestBody SmartLink smartLink);

    @GetMapping("/s/{smartLinkId}")
    @Operation(
            summary = "Осуществить редирект",
            description = "Редирект")
    ResponseEntity<Void> redirect(@Valid @PathVariable String smartLinkId, HttpServletRequest request);
}
