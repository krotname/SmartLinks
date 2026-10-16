package name.krot.smartlinks.controller;

import name.krot.smartlinks.config.ManagementSecurityConfig;
import name.krot.smartlinks.service.PublicSmartLinkUrls;
import name.krot.smartlinks.service.SmartLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static name.krot.smartlinks.support.SmartLinkJsonFixtures.fallbackSmartLinkJson;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ManagementController.class, ManagementPageController.class}, properties = {
        "smartlinks.owner.username=owner", "smartlinks.owner.password-hash={noop}test-password"})
@Import({ManagementSecurityConfig.class, PublicSmartLinkUrls.class})
class ManagementControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean SmartLinkService service;

    private MockHttpServletRequestBuilder create() {
        return post("https://links.n-0.ru/manage/api/smartlinks").secure(true)
                .contentType("application/json").content(fallbackSmartLinkJson());
    }

    @Test void anonymousCannotCreateEvenWithCsrf() throws Exception {
        mvc.perform(create().with(csrf())).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void ownerNeedsCsrf() throws Exception {
        mvc.perform(create().with(user("owner").roles("OWNER"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void apexCannotReachManagement() throws Exception {
        mvc.perform(post("https://n-0.ru/manage/api/smartlinks").secure(true)
                .with(user("owner").roles("OWNER")).with(csrf())
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isNotFound());
        verifyNoInteractions(service);
    }

    @Test void insecureManagementIsRejected() throws Exception {
        mvc.perform(get("http://links.n-0.ru/manage/csrf")).andExpect(status().isForbidden());
    }

    @Test void creationUsesServiceAndCanonicalOrigin() throws Exception {
        mvc.perform(create().with(user("owner").roles("OWNER")).with(csrf().asHeader()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SMART_LINK_ID))
                .andExpect(jsonPath("$.url").value("https://n-0.ru/" + SMART_LINK_ID));
        verify(service).saveSmartLink(any());
    }

    @Test void missingLinkReturns404() throws Exception {
        when(service.findSmartLinkById("missing")).thenReturn(java.util.Optional.empty());
        mvc.perform(get("https://links.n-0.ru/manage/api/smartlinks/missing").secure(true)
                .with(user("owner").roles("OWNER"))).andExpect(status().isNotFound());
    }

    @Test void formLoginRequiresCsrfAndChecksPassword() throws Exception {
        mvc.perform(post("https://links.n-0.ru/manage/login").secure(true)
                .param("username", "owner").param("password", "test-password"))
                .andExpect(status().isForbidden());
        mvc.perform(post("https://links.n-0.ru/manage/login").secure(true).with(csrf())
                .param("username", "owner").param("password", "wrong"))
                .andExpect(redirectedUrl("/manage/login?error"));
        mvc.perform(post("https://links.n-0.ru/manage/login").secure(true).with(csrf())
                .param("username", "owner").param("password", "test-password"))
                .andExpect(redirectedUrl("/manage/"));
    }
    @Test void contextPathPreservesApiAndPageAuthentication() throws Exception {
        mvc.perform(post("https://links.n-0.ru/short/manage/api/smartlinks").contextPath("/short")
                .secure(true).with(csrf()).contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("https://links.n-0.ru/short/manage/").contextPath("/short").secure(true))
                .andExpect(redirectedUrl("/short/manage/login"));
        verifyNoInteractions(service);
    }

    @Test void contextPathCanonicalizesDirectoryForRelativeAssets() throws Exception {
        mvc.perform(get("https://links.n-0.ru/short/manage").contextPath("/short").secure(true)
                .with(user("owner").roles("OWNER")))
                .andExpect(redirectedUrl("/short/manage/"));
        mvc.perform(get("https://links.n-0.ru/short/manage/login").contextPath("/short").secure(true))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"login\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("src=\"login.js\"")));
    }

}
