package cn.iocoder.yudao.module.ai1.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class Ai1UtilsTest {

    @Test
    public void testNormalizeHeaders_orderAndValues() {
        // 准备参数
        Map<String, String> input = new LinkedHashMap<>();
        input.put(" X-Z ", "${TEST}");
        input.put("X-A", "{session}");

        // 调用
        Map<String, String> normalized = Ai1Utils.normalizeHeaders(input);

        // 断言
        assertInstanceOf(LinkedHashMap.class, normalized);
        assertEquals(List.of("X-Z", "X-A"), List.copyOf(normalized.keySet()));
        assertEquals("${TEST}", normalized.get("X-Z"));
        assertEquals("{session}", normalized.get("X-A"));
        assertTrue(input.containsKey(" X-Z "));
        assertNotSame(input, normalized);
    }

    @Test
    public void testNormalizeHeaders_invalid() {
        // 准备参数
        Map<String, String> duplicate = new LinkedHashMap<>();
        duplicate.put("X-Test", "static");
        duplicate.put(" x-test ", "{session}");
        Map<String, String> nullValue = new LinkedHashMap<>();
        nullValue.put("X-Test", null);
        Map<String, String> nullKey = new LinkedHashMap<>();
        nullKey.put(null, "value");

        // 调用，并断言异常
        for (Map<String, String> input : List.of(duplicate, nullValue, nullKey, Map.of(" ", "value"))) {
            assertFalse(Ai1Utils.isHeadersValid(input));
            ServiceException ex = assertThrows(ServiceException.class, () -> Ai1Utils.normalizeHeaders(input));
            assertEquals(PROVIDER_HEADERS_INVALID.getCode(), ex.getCode());
            assertFalse(ex.getMessage().contains("static"));
        }
    }

    @Test
    public void testNormalizeHeaders_nullAndEmpty() {
        // 调用，并断言
        assertNull(Ai1Utils.normalizeHeaders(null));
        assertEquals(Map.of(), Ai1Utils.normalizeHeaders(Map.of()));
        assertEquals(Map.of("X-Test", ""), Ai1Utils.normalizeHeaders(Map.of("X-Test", "")));
        assertNull(Ai1Utils.resolveSpringPlaceholders((Map<String, String>) null));
    }

    @Test
    public void testIsHeadersValid_jsonLength() {
        // 调用，并断言
        assertTrue(Ai1Utils.isHeadersValid(Map.of("X", "a".repeat(1992))));
        assertFalse(Ai1Utils.isHeadersValid(Map.of("X", "a".repeat(1993))));
        assertFalse(Ai1Utils.isHeadersValid(Map.of("X", "\"".repeat(1000))));
    }

    @Test
    public void testResolveSpringPlaceholders_unresolved() {
        // 准备参数
        try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
            spring.when(() -> SpringUtil.getBean(Environment.class)).thenReturn(new MockEnvironment());
            // 调用，并断言异常
            for (String value : List.of("literal-private-api-key-${MISSING}", "https://private-token@host/${MISSING}")) {
                ServiceException ex = assertThrows(ServiceException.class, () -> Ai1Utils.resolveSpringPlaceholders(value));
                assertEquals(CONFIG_PLACEHOLDER_NOT_RESOLVED.getCode(), ex.getCode());
                assertFalse(ex.getMessage().contains("private"));
                assertFalse(ex.getMessage().contains("Header"));
                assertFalse(ex.getMessage().contains(value));
            }
            ServiceException env = assertThrows(ServiceException.class, () ->
                    Ai1Utils.resolveSpringPlaceholders(Map.of("MCP_TOKEN", "private-env-${MISSING}")));
            assertFalse(env.getMessage().contains("Header"));
            assertFalse(env.getMessage().contains("private-env"));
        }
    }

    @Test
    public void testResolveSpringPlaceholders_map() {
        // 准备参数
        try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
            spring.when(() -> SpringUtil.getBean(Environment.class))
                    .thenReturn(new MockEnvironment().withProperty("HEADER_TEST", "a\"b\\c"));
            Map<String, String> input = new LinkedHashMap<>();
            input.put("X-Z", "${HEADER_TEST}-{session}");
            input.put("X-A", "unchanged");

            // 调用
            Map<String, String> resolved = Ai1Utils.resolveSpringPlaceholders(input);

            // 断言
            assertEquals("a\"b\\c-{session}", resolved.get("X-Z"));
            assertEquals(List.of("X-Z", "X-A"), List.copyOf(resolved.keySet()));
            assertEquals("${HEADER_TEST}-{session}", input.get("X-Z"));
            ServiceException ex = assertThrows(ServiceException.class, () ->
                    Ai1Utils.resolveSpringPlaceholders(Map.of("X-Test", "literal-secret-${MISSING_HEADER_TEST}")));
            assertEquals(CONFIG_PLACEHOLDER_NOT_RESOLVED.getCode(), ex.getCode());
            assertFalse(ex.getMessage().contains("literal-secret"));
        }
    }
}
