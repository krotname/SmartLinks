package name.krot.smartlinks.controller;

import name.krot.smartlinks.config.ManagementSecurityConfig;
import name.krot.smartlinks.service.PublicSmartLinkUrls;
import name.krot.smartlinks.service.SmartLinkService;
import name.krot.smartlinks.service.SmartLinkEditor;
import name.krot.smartlinks.model.VersionedSmartLink;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
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
    @MockitoBean SmartLinkEditor editor;

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
        when(editor.read("missing")).thenThrow(new SmartLinkNotFoundException("not found"));
        mvc.perform(get("https://links.n-0.ru/manage/api/smartlinks/missing").secure(true)
                .with(user("owner").roles("OWNER"))).andExpect(status().isNotFound());
    }

    @Test void editingRequiresExplicitVersion() throws Exception {
        mvc.perform(put("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")).with(csrf().asHeader())
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().is(428));
        verifyNoInteractions(service);
    }

    @Test void editingRejectsWildcardVersion() throws Exception {
        mvc.perform(put("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")).with(csrf().asHeader())
                .header("If-Match", "*").contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isBadRequest());
    }

    @Test void anonymousCannotEditEvenWithCsrf() throws Exception {
        mvc.perform(put("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(csrf()).header("If-Match", "\"0\"")
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test void editingRequiresCsrf() throws Exception {
        mvc.perform(put("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")).header("If-Match", "\"0\"")
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"W/\"0\"", "\"0\", \"0\"", "0", "\"invalid\""})
    void editingRejectsAmbiguousOrMalformedVersions(String version) throws Exception {
        mvc.perform(put("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")).with(csrf().asHeader()).header("If-Match", version)
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(editor);
    }

    @Test void apexCannotEditEvenWithOwnerAndCsrf() throws Exception {
        mvc.perform(put("https://n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")).with(csrf().asHeader()).header("If-Match", "\"0\"")
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isNotFound());
        verifyNoInteractions(editor);
    }

    @Test void readingReturnsStrongVersionWithoutChangingTheDefinitionFormat() throws Exception {
        var definition = name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink(
                name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule("https://example.com/old"));
        when(editor.read(SMART_LINK_ID)).thenReturn(new VersionedSmartLink(definition, "0"));
        mvc.perform(get("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk()).andExpect(header().string("ETag", "\"0\""))
                .andExpect(jsonPath("$.id").value(SMART_LINK_ID))
                .andExpect(jsonPath("$.rules[0].redirectTo").value("https://example.com/old"));
    }

    @Test void conditionalEditReturnsUpdatedDefinitionAndVersion() throws Exception {
        var definition = name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink(
                name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule("https://example.com/new"));
        when(editor.update(eq(SMART_LINK_ID), any(), eq("0")))
                .thenReturn(new VersionedSmartLink(definition, "d04899ef-48f9-4b91-a316-7f0cdfabc989"));
        mvc.perform(put("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")).with(csrf().asHeader()).header("If-Match", "\"0\"")
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"d04899ef-48f9-4b91-a316-7f0cdfabc989\""))
                .andExpect(jsonPath("$.rules[0].redirectTo").value("https://example.com/new"));
    }

    @Test void staleEditReturns412() throws Exception {
        when(editor.update(eq(SMART_LINK_ID), any(), eq("0")))
                .thenThrow(new name.krot.smartlinks.exception.SmartLinkVersionConflictException());
        mvc.perform(put("https://links.n-0.ru/manage/api/smartlinks/" + SMART_LINK_ID).secure(true)
                .with(user("owner").roles("OWNER")).with(csrf().asHeader()).header("If-Match", "\"0\"")
                .contentType("application/json").content(fallbackSmartLinkJson()))
                .andExpect(status().isPreconditionFailed());
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
