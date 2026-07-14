package name.krot.smartlinks.repository;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import name.krot.smartlinks.model.SmartLink;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.DeserializationFeature;

class RedisSmartLinkRepositoryTest {

    @Test
    void cachedValuesCannotBeMutatedWithoutSaving() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), anyString())).thenReturn(true);
        RedisSmartLinkRepository repository = new RedisSmartLinkRepository(
                template, JsonMapper.builder().build());
        SmartLink original = smartLink(fallbackRule("https://example.com/original"));

        repository.saveIfAbsent(original);
        original.getRules().getFirst().setRedirectTo("https://example.com/mutated-after-save");
        SmartLink firstRead = repository.findById(SMART_LINK_ID).orElseThrow();
        firstRead.getRules().getFirst().setRedirectTo("https://example.com/mutated-read");
        SmartLink secondRead = repository.findById(SMART_LINK_ID).orElseThrow();

        assertEquals("https://example.com/original", secondRead.getRules().getFirst().getRedirectTo());
    }

    @Test
    void malformedRedisValueIsNotCachedAndIsReportedAsServerStateFailure() throws Exception {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        JsonMapper mapper = JsonMapper.builder().build();
        SmartLink expected = smartLink(fallbackRule("https://example.com/valid"));
        when(template.opsForValue()).thenReturn(values);
        when(values.get("sl:" + SMART_LINK_ID))
                .thenReturn("{broken-json", mapper.writeValueAsString(expected));
        RedisSmartLinkRepository repository = new RedisSmartLinkRepository(template, mapper);

        assertThrows(IllegalStateException.class, () -> repository.findById(SMART_LINK_ID));
        SmartLink recovered = repository.findById(SMART_LINK_ID).orElseThrow();

        assertEquals("https://example.com/valid", recovered.getRules().getFirst().getRedirectTo());
        verify(values, times(2)).get("sl:" + SMART_LINK_ID);
    }

    @Test
    void ignoresUnknownStoredFieldsDuringRollingDeployments() throws Exception {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        JsonMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        String jsonFromNewerVersion = mapper.writeValueAsString(
                        smartLink(fallbackRule("https://example.com/compatible")))
                .replaceFirst("\\{", "{\"newerVersionMetadata\":true,");
        when(template.opsForValue()).thenReturn(values);
        when(values.get("sl:" + SMART_LINK_ID)).thenReturn(jsonFromNewerVersion);
        RedisSmartLinkRepository repository = new RedisSmartLinkRepository(template, mapper);

        SmartLink loaded = repository.findById(SMART_LINK_ID).orElseThrow();

        assertEquals("https://example.com/compatible", loaded.getRules().getFirst().getRedirectTo());
    }

    @Test
    void rejectsStoredPayloadWhoseIdDoesNotMatchRedisKey() throws Exception {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        JsonMapper mapper = JsonMapper.builder().build();
        SmartLink mismatched = smartLink(fallbackRule("https://example.com/wrong"));
        mismatched.setId("different-id");
        when(template.opsForValue()).thenReturn(values);
        when(values.get("sl:" + SMART_LINK_ID)).thenReturn(mapper.writeValueAsString(mismatched));
        RedisSmartLinkRepository repository = new RedisSmartLinkRepository(template, mapper);

        assertThrows(IllegalStateException.class, () -> repository.findById(SMART_LINK_ID));
    }
}
