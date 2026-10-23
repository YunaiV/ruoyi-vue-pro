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

    private static final List<String> MEMORY_SECTIONS = java.util.Arrays.asList("用户画像", "偏好习惯", "关键事实", "目标与意图", "近期进展");

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
        assertEquals(java.util.Arrays.asList("X-Z", "X-A"), new java.util.ArrayList<>(normalized.keySet()));
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
        for (Map<String, String> input : java.util.Arrays.asList(duplicate, nullValue, nullKey, Map.of(" ", "value"))) {
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
            for (String value : java.util.Arrays.asList("literal-private-api-key-${MISSING}", "https://private-token@host/${MISSING}")) {
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
            assertEquals(java.util.Arrays.asList("X-Z", "X-A"), new java.util.ArrayList<>(resolved.keySet()));
            assertEquals("${HEADER_TEST}-{session}", input.get("X-Z"));
            ServiceException ex = assertThrows(ServiceException.class, () ->
                    Ai1Utils.resolveSpringPlaceholders(Map.of("X-Test", "literal-secret-${MISSING_HEADER_TEST}")));
            assertEquals(CONFIG_PLACEHOLDER_NOT_RESOLVED.getCode(), ex.getCode());
            assertFalse(ex.getMessage().contains("literal-secret"));
        }
    }

    @Test
    public void testNormalizeMemoryMarkdown_sections() {
        // 调用，并断言：标题与内容压成一行时拆开，不同维度之间空一行
        assertEquals("## 用户画像\n- 后端工程师\n\n## 偏好习惯\n- 简洁回答",
                Ai1Utils.normalizeMemoryMarkdown("##用户画像-后端工程师##偏好习惯-简洁回答", MEMORY_SECTIONS));
        // 调用，并断言：统一换行，合并多余空行
        assertEquals("## 用户画像\n- 程序员\n\n## 关键事实\n- 住在北京",
                Ai1Utils.normalizeMemoryMarkdown(" ## 用户画像\r\n-程序员\r\n\r\n\r\n## 关键事实\r- 住在北京 ", MEMORY_SECTIONS));
    }

    @Test
    public void testNormalizeMemoryMarkdown_keepsFacts() {
        // 准备参数：C#、#123、URL 锚点、中文破折号都是用户事实，不能被拆开
        String content = "## 关键事实\n- 主力语言 C#，工单#123，文档 https://a.cn/doc#part\n- 常驻北京—上海，周期 3–5 天";

        // 调用，并断言
        assertEquals(content, Ai1Utils.normalizeMemoryMarkdown(content, MEMORY_SECTIONS));
        // 调用，并断言：非固定维度的行内标题不拆行
        assertEquals("## 关键事实\n- 标签##其他", Ai1Utils.normalizeMemoryMarkdown("## 关键事实\n- 标签##其他", MEMORY_SECTIONS));
    }

    @Test
    public void testNormalizeMemoryMarkdown_inlineList() {
        // 调用，并断言：非 ASCII 字符后紧跟 ASCII「-」，拆成列表项
        assertEquals("## 偏好习惯\n- 口吻正式\n- 开场先问候",
                Ai1Utils.normalizeMemoryMarkdown("## 偏好习惯\n- 口吻正式-开场先问候", MEMORY_SECTIONS));
        // 调用，并断言：ASCII 字符之间的「-」不拆
        assertEquals("## 关键事实\n- 使用 spring-boot", Ai1Utils.normalizeMemoryMarkdown("## 关键事实\n- 使用 spring-boot", MEMORY_SECTIONS));
    }

    @Test
    public void testNormalizeMemoryMarkdown_blank() {
        // 调用，并断言：为空时原样返回
        assertNull(Ai1Utils.normalizeMemoryMarkdown(null, MEMORY_SECTIONS));
        assertEquals(" ", Ai1Utils.normalizeMemoryMarkdown(" ", MEMORY_SECTIONS));
    }

    @Test
    public void testLimitLength() {
        // 调用，并断言：先去除首尾空白，未超长时不截断
        assertEquals("用户喜欢", Ai1Utils.limitLength("  用户喜欢 \n", 4));
        // 调用，并断言：下标 4、5 的 char 是同一个 emoji，截断不拆开代理对
        assertEquals("用户喜欢", Ai1Utils.limitLength("用户喜欢😀表情", 5));
        assertEquals("用户喜欢😀", Ai1Utils.limitLength("用户喜欢😀表情", 6));
        assertEquals("用户喜欢😀表", Ai1Utils.limitLength("用户喜欢😀表情", 7));
        // 调用，并断言：为空时原样返回
        assertNull(Ai1Utils.limitLength(null, 5));
        assertEquals(" ", Ai1Utils.limitLength(" ", 5));
    }

}
