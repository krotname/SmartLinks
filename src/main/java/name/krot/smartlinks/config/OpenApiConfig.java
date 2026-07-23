package name.krot.smartlinks.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import name.krot.smartlinks.security.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /** Referenced from {@code @SecurityRequirement} on the write operation only. */
    public static final String API_KEY_SCHEME = "ApiKeyAuth";

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL SmartLinks API")
                        .version("1.0")
                        .description("Документация API для сервиса сокращения URL. "
                                + "Операции записи требуют заголовок " + ApiKeyAuthenticationFilter.API_KEY_HEADER
                                + "; редиректы остаются публичными."))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(ApiKeyAuthenticationFilter.API_KEY_HEADER)
                                .description("Общий ключ из smartlinks.api-key (env SMARTLINKS_API_KEY)")));
    }
}
