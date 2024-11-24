package name.krot.smartlinks.controller;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.service.SmartLinkService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SmartLinkService smartLinkService;

    @MockBean
    private PredicateFactory predicateFactory;

    @Mock
    private Predicate dateRangePredicate;

    @Mock
    private Predicate languagePredicate;

    @Test
    void testRedirectWithMatchingRule() throws Exception {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        List<Rule> rules = new ArrayList<>();
        Rule rule = new Rule();
        rule.setPredicates(Arrays.asList("DateRange", "Language"));
        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");
        args.put("language", Arrays.asList("ru", "ru-RU"));
        rule.setArgs(args);
        rule.setRedirectTo("https://otus.ru/ru");
        rules.add(rule);
        smartLink.setRules(rules);

        when(smartLinkService.getSmartLinkById("smartlink123")).thenReturn(smartLink);
        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);

        when(dateRangePredicate.evaluate(any(RequestContext.class), eq(args))).thenReturn(true);
        when(languagePredicate.evaluate(any(RequestContext.class), eq(args))).thenReturn(true);

        mockMvc.perform(get("/s/smartlink123")
                        .header("Accept-Language", "ru-RU"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://otus.ru/ru"));
    }

    @Test
    void testRedirectWithNoMatchingRule() throws Exception {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        List<Rule> rules = new ArrayList<>();
        Rule rule = new Rule();
        rule.setPredicates(Arrays.asList("Language"));
        Map<String, Object> args = new HashMap<>();
        args.put("language", Arrays.asList("ru", "ru-RU"));
        rule.setArgs(args);
        rule.setRedirectTo("https://otus.ru/ru");
        rules.add(rule);
        smartLink.setRules(rules);

        when(smartLinkService.getSmartLinkById("smartlink123")).thenReturn(smartLink);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);

        when(languagePredicate.evaluate(any(RequestContext.class), eq(args))).thenReturn(false);

        mockMvc.perform(get("/s/smartlink123")
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No matching rule found for this Smart Link"));
    }

    @Test
    void testRedirectWithNonExistentSmartLink() throws Exception {
        when(smartLinkService.getSmartLinkById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/s/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Smart Link not found"));
    }

    @Test
    void testCreateSmartLink() throws Exception {
        String smartLinkJson = "{\n" +
                "  \"id\": \"smartlink123\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"predicates\": [\"DateRange\", \"Language\"],\n" +
                "      \"args\": {\n" +
                "        \"startWith\": \"2024-11-01T00:00:00\",\n" +
                "        \"endWith\": \"2024-12-01T00:00:00\",\n" +
                "        \"language\": [\"ru\", \"ru-RU\"]\n" +
                "      },\n" +
                "      \"redirectTo\": \"https://example.com/ru\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(smartLinkJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("Smart Link created successfully"));

        ArgumentCaptor<SmartLink> smartLinkCaptor = ArgumentCaptor.forClass(SmartLink.class);
        verify(smartLinkService, times(1)).saveSmartLink(smartLinkCaptor.capture());

        SmartLink capturedSmartLink = smartLinkCaptor.getValue();
        assertEquals("smartlink123", capturedSmartLink.getId());
        assertEquals(1, capturedSmartLink.getRules().size());
    }
}