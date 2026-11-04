package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiReasoningUtils} 的单元测试类
 *
 * 纯单元测试，不启动 Spring 容器，不请求外部模型
 *
 * @author 芋道源码
 */
public class AiReasoningUtilsTest {

    private static final String ANTHROPIC = AiPlatformEnum.ANTHROPIC.getPlatform();
    private static final String OPENAI = AiPlatformEnum.OPENAI.getPlatform();

    private static final Integer CHAT = AiModelTypeEnum.CHAT.getType();
    private static final Integer IMAGE = AiModelTypeEnum.IMAGE.getType();

    // ========== 思考档位：路由 ==========

    @Test
    public void testGetReasoningRoute() {
        // 调用，并断言：HTTP 路由
        for (AiPlatformEnum platform : Arrays.asList(AiPlatformEnum.DEEP_SEEK, AiPlatformEnum.DOU_BAO,
                AiPlatformEnum.HUN_YUAN, AiPlatformEnum.SILICON_FLOW, AiPlatformEnum.YI_YAN, AiPlatformEnum.ZHI_PU,
                AiPlatformEnum.XING_HUO, AiPlatformEnum.MINI_MAX, AiPlatformEnum.MOONSHOT, AiPlatformEnum.BAI_CHUAN,
                AiPlatformEnum.STEP_FUN, AiPlatformEnum.MI_MO)) {
            assertEquals(AiReasoningUtils.ReasoningRoute.HTTP, AiReasoningUtils.getReasoningRoute(platform), platform.name());
        }
        // 调用，并断言：OpenAI、通义路由
        assertEquals(AiReasoningUtils.ReasoningRoute.OPENAI, AiReasoningUtils.getReasoningRoute(AiPlatformEnum.OPENAI));
        assertEquals(AiReasoningUtils.ReasoningRoute.OPENAI, AiReasoningUtils.getReasoningRoute(AiPlatformEnum.GROK));
        assertEquals(AiReasoningUtils.ReasoningRoute.OPENAI, AiReasoningUtils.getReasoningRoute(AiPlatformEnum.GEMINI));
        assertEquals(AiReasoningUtils.ReasoningRoute.TONG_YI, AiReasoningUtils.getReasoningRoute(AiPlatformEnum.TONG_YI));
        // 调用，并断言：不支持
        assertEquals(AiReasoningUtils.ReasoningRoute.UNSUPPORTED, AiReasoningUtils.getReasoningRoute(AiPlatformEnum.ANTHROPIC));
        assertEquals(AiReasoningUtils.ReasoningRoute.UNSUPPORTED, AiReasoningUtils.getReasoningRoute(AiPlatformEnum.AZURE_OPENAI));
        assertEquals(AiReasoningUtils.ReasoningRoute.UNSUPPORTED, AiReasoningUtils.getReasoningRoute(AiPlatformEnum.OLLAMA));
        assertEquals(AiReasoningUtils.ReasoningRoute.UNSUPPORTED, AiReasoningUtils.getReasoningRoute(null));
    }

    // ========== 思考档位：保存校验 ==========

    @Test
    public void testValidateReasoningConfig_success() {
        // 调用，并断言
        assertDoesNotThrow(() -> validateReasoning(AiPlatformEnum.DEEP_SEEK, deepSeekConfig()));
    }

    @Test
    public void testValidateReasoningConfig_nullOrEmptyLevels() {
        // 调用，并断言：不修改 / 清空，不做平台校验；清空时默认档位由 AiModelSaveReqVO 校验
        assertDoesNotThrow(() -> AiReasoningUtils.validateReasoningConfig(AiPlatformEnum.ANTHROPIC, null, CHAT, null));
        assertDoesNotThrow(() -> AiReasoningUtils.validateReasoningConfig(AiPlatformEnum.ANTHROPIC, null, CHAT,
                config(null)));
        assertDoesNotThrow(() -> AiReasoningUtils.validateReasoningConfig(AiPlatformEnum.ANTHROPIC, null, IMAGE,
                new AiModelDO.ReasoningConfig()));
    }

    @Test
    public void testValidateReasoningConfig_notChat() {
        // 调用，并断言异常
        assertServiceException(() -> AiReasoningUtils.validateReasoningConfig(AiPlatformEnum.DEEP_SEEK,
                AiPlatformEnum.DEEP_SEEK, AiModelTypeEnum.IMAGE.getType(), deepSeekConfig()),
                MODEL_REASONING_CONFIG_INVALID, "只有对话模型可以配置思考");
    }

    @Test
    public void testValidateReasoningConfig_platformNotSupported() {
        // 调用，并断言异常
        assertServiceException(() -> validateReasoning(AiPlatformEnum.ANTHROPIC, deepSeekConfig()),
                MODEL_REASONING_PLATFORM_NOT_SUPPORTED, AiPlatformEnum.ANTHROPIC.getPlatform());
    }

    @Test
    public void testValidateReasoningConfig_platformMismatch() {
        // 调用，并断言异常
        assertServiceException(() -> AiReasoningUtils.validateReasoningConfig(AiPlatformEnum.DEEP_SEEK,
                AiPlatformEnum.OPENAI, CHAT, deepSeekConfig()),
                MODEL_REASONING_PLATFORM_MISMATCH, AiPlatformEnum.DEEP_SEEK.getPlatform(),
                AiPlatformEnum.OPENAI.getPlatform());
    }

    @Test
    public void testValidateReasoningConfig_httpForbiddenKeys() {
        // 调用，并断言异常：顶层禁止的键
        for (String key : Arrays.asList("model", "messages", "stream", "stream_options", "tools", "tool_choice")) {
            AiModelDO.ReasoningLevel off = emptyLevel("off");
            off.getOptions().put(key, "x");
            assertConfigInvalid(AiPlatformEnum.DEEP_SEEK, config("off", off));
        }

        // 准备参数：HTTP 路由允许覆盖其它顶层字段、允许嵌套
        AiModelDO.ReasoningLevel off = emptyLevel("off");
        off.getOptions().put("temperature", 1.0);
        off.getOptions().put("thinking", new HashMap<>(Map.of("type", "disabled")));
        // 调用，并断言
        assertDoesNotThrow(() -> validateReasoning(AiPlatformEnum.ZHI_PU, config("off", off)));
    }

    @Test
    public void testValidateReasoningConfig_openAiRules() {
        // 准备参数：允许 reasoning_effort 和追加的新字段
        AiModelDO.ReasoningLevel ok = emptyLevel("low");
        ok.getOptions().put("reasoning_effort", "low");
        ok.getOptions().put("reasoning", new HashMap<>(Map.of("effort", "low")));
        // 准备参数：reasoning_effort 必须是非空字符串
        AiModelDO.ReasoningLevel badEffort = emptyLevel("low");
        badEffort.getOptions().put("reasoning_effort", 1);

        // 调用，并断言
        assertDoesNotThrow(() -> validateReasoning(AiPlatformEnum.OPENAI, config("low", ok)));
        // 调用，并断言异常
        assertConfigInvalid(AiPlatformEnum.OPENAI, config("low", badEffort));
        // 调用，并断言异常：不能覆盖强类型字段
        for (String key : Arrays.asList("temperature", "max_tokens", "max_completion_tokens", "top_p", "tools")) {
            AiModelDO.ReasoningLevel low = emptyLevel("low");
            low.getOptions().put(key, 1);
            assertConfigInvalid(AiPlatformEnum.GROK, config("low", low));
        }
    }

    @Test
    public void testValidateReasoningConfig_tongYiRules() {
        // 准备参数：合法
        AiModelDO.ReasoningLevel off = level("off", new HashMap<>(Map.of("enable_thinking", false)));
        AiModelDO.ReasoningLevel high = level("high",
                new HashMap<>(Map.of("enable_thinking", true, "thinking_budget", 8192)));
        // 准备参数：enable_thinking 不是布尔
        AiModelDO.ReasoningLevel notBool = level("off", new HashMap<>(Map.of("enable_thinking", "false")));
        // 准备参数：其他键
        AiModelDO.ReasoningLevel otherKey = level("off",
                new HashMap<>(Map.of("enable_thinking", false, "temperature", 1)));
        // 准备参数：预算非正整数
        AiModelDO.ReasoningLevel badBudget = level("high",
                new HashMap<>(Map.of("enable_thinking", true, "thinking_budget", 0)));
        // 准备参数：预算为 null
        Map<String, Object> nullBudgetOptions = new HashMap<>();
        nullBudgetOptions.put("enable_thinking", true);
        nullBudgetOptions.put("thinking_budget", null);
        AiModelDO.ReasoningLevel nullBudget = level("high", nullBudgetOptions);

        // 调用，并断言
        assertDoesNotThrow(() -> validateReasoning(AiPlatformEnum.TONG_YI, config("high", off, high)));
        // 调用，并断言异常：缺 enable_thinking
        assertConfigInvalid(AiPlatformEnum.TONG_YI, config("off", emptyLevel("off")));
        // 调用，并断言异常
        assertConfigInvalid(AiPlatformEnum.TONG_YI, config("off", notBool));
        assertConfigInvalid(AiPlatformEnum.TONG_YI, config("off", otherKey));
        assertConfigInvalid(AiPlatformEnum.TONG_YI, config("high", badBudget));
        assertConfigInvalid(AiPlatformEnum.TONG_YI, config("high", nullBudget));
    }

    @Test
    public void testValidateReasoningConfig_noMaxBytes() {
        // 准备参数：中文按 UTF-8 计 3 字节，1400 个汉字 > 4000 字节；已去掉字节上限
        AiModelDO.ReasoningLevel off = emptyLevel("off");
        off.getOptions().put("note", StrUtil.repeat('思', 1400));

        // 调用，并断言
        assertDoesNotThrow(() -> validateReasoning(AiPlatformEnum.DEEP_SEEK, config("off", off)));
    }

    // ========== 思考档位：发送 ==========

    @Test
    public void testResolveReasoningLevel_unconfigured() {
        // 准备参数
        AiModelDO unmanaged = new AiModelDO();

        // 调用，并断言
        assertNull(AiReasoningUtils.resolveReasoningLevel(unmanaged, null));
        assertNull(AiReasoningUtils.resolveReasoningLevel(unmanaged.setReasoningConfig(config(null)), ""));
        // 调用，并断言异常
        assertServiceException(() -> AiReasoningUtils.resolveReasoningLevel(new AiModelDO(), "high"),
                MODEL_REASONING_NOT_CONFIGURED);
    }

    @Test
    public void testResolveReasoningLevel_configured() {
        // 准备参数
        AiModelDO managed = managedModel();

        // 调用，并断言
        assertEquals("high", AiReasoningUtils.resolveReasoningLevel(managed, null));
        assertEquals("high", AiReasoningUtils.resolveReasoningLevel(managed, ""));
        assertEquals("off", AiReasoningUtils.resolveReasoningLevel(managed, "off"));
        // 调用，并断言异常
        assertServiceException(() -> AiReasoningUtils.resolveReasoningLevel(managed, "ultra"),
                MODEL_REASONING_LEVEL_NOT_SUPPORTED, "ultra");
        // 调用，并断言异常：库里默认档位被改坏，报错，不回退
        AiModelDO broken = managedModel();
        broken.getReasoningConfig().setDefaultLevel("ultra");
        assertServiceException(() -> AiReasoningUtils.resolveReasoningLevel(broken, null),
                MODEL_REASONING_CONFIG_INVALID, "默认档位不在档位列表中");
    }

    @Test
    public void testResolveReasoningLevel_reordered() {
        // 准备参数：档位顺序倒过来，思考参数跟着档位走，不跟着下标走
        AiModelDO model = managedModel();
        Collections.reverse(model.getReasoningConfig().getLevels());

        // 调用，并断言
        assertEquals("high", AiReasoningUtils.resolveReasoningLevel(model, null));
        assertEquals("max", AiReasoningUtils.getReasoningOptions(model, "max").get("reasoning_effort"));
        assertEquals("disabled", asMap(AiReasoningUtils.getReasoningOptions(model, "off").get("thinking")).get("type"));
    }

    @Test
    public void testValidateReasoningRoute() {
        // 调用，并断言
        assertDoesNotThrow(() -> AiReasoningUtils.validateReasoningRoute(AiPlatformEnum.ANTHROPIC, new AiModelDO()));
        assertDoesNotThrow(() -> AiReasoningUtils.validateReasoningRoute(AiPlatformEnum.DEEP_SEEK, managedModel()));
        // 调用，并断言异常
        assertServiceException(() -> AiReasoningUtils.validateReasoningRoute(AiPlatformEnum.OLLAMA, managedModel()),
                MODEL_REASONING_PLATFORM_NOT_SUPPORTED, AiPlatformEnum.OLLAMA.getPlatform());
    }

    @Test
    public void testGetReasoningOptions_deepCopy() {
        // 准备参数
        AiModelDO model = managedModel();

        // 调用，并断言
        assertNull(AiReasoningUtils.getReasoningOptions(model, null));
        Map<String, Object> levelOptions = AiReasoningUtils.getReasoningOptions(model, "high");
        assertEquals("high", levelOptions.get("reasoning_effort"));
        // 断言：修改返回值不影响模型上的思考参数（含嵌套）
        asMap(levelOptions.get("thinking")).put("type", "disabled");
        levelOptions.put("reasoning_effort", "max");
        Map<String, Object> again = AiReasoningUtils.getReasoningOptions(model, "high");
        assertEquals("enabled", asMap(again.get("thinking")).get("type"));
        assertEquals("high", again.get("reasoning_effort"));
    }

    @Test
    public void testGetReasoningOptions_missing() {
        // 准备参数：库里档位的思考参数被改坏
        AiModelDO model = managedModel();
        model.getReasoningConfig().getLevels().get(3).setOptions(null);

        // 调用，并断言异常
        assertServiceException(() -> AiReasoningUtils.getReasoningOptions(model, "max"),
                MODEL_REASONING_CONFIG_INVALID, "档位(max)缺少思考参数");
        assertServiceException(() -> AiReasoningUtils.getReasoningOptions(model, "ultra"),
                MODEL_REASONING_CONFIG_INVALID, "档位(ultra)缺少思考参数");
    }

    @Test
    public void testReasoningConfig_jsonRoundTrip() {
        // 准备参数：JacksonTypeHandler 存储的就是该 JSON；档位顺序非字母序，档位值含纯数字
        AiModelDO.ReasoningConfig config = config("1", emptyLevel("off"), emptyLevel("1"),
                level("high", thinking("enabled", "high")));

        // 调用
        String json = JsonUtils.toJsonString(config);
        AiModelDO.ReasoningConfig result = JsonUtils.parseObject(json, AiModelDO.ReasoningConfig.class);
        // 断言：档位顺序、默认档位、思考参数原样读回；纯数字档位值仍是字符串
        assertEquals(Arrays.asList("off", "1", "high"),
                Arrays.asList(result.getLevels().get(0).getLevel(), result.getLevels().get(1).getLevel(),
                        result.getLevels().get(2).getLevel()));
        assertEquals("1", result.getDefaultLevel());
        assertEquals(config, result);
        assertTrue(json.contains("\"level\":\"1\""), json);
    }

    // ========== 思考档位：规范化 JSON ==========

    @Test
    public void testHashReasoningOptions() {
        // 准备参数：a、b 只是键顺序不同（含嵌套）；c 内容不同；d 嵌套值不同；e 多覆盖一个字段
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("thinking", new LinkedHashMap<>(Map.of("type", "enabled")));
        a.put("reasoning_effort", "high");
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("reasoning_effort", "high");
        b.put("thinking", new LinkedHashMap<>(Map.of("type", "enabled")));
        Map<String, Object> c = new LinkedHashMap<>(b);
        c.put("reasoning_effort", "max");
        Map<String, Object> d = new LinkedHashMap<>(b);
        d.put("thinking", new LinkedHashMap<>(Map.of("type", "disabled")));
        Map<String, Object> e = new LinkedHashMap<>(b);
        e.put("temperature", 1.0);

        // 调用
        String hashA = AiReasoningUtils.hashReasoningOptions(a);
        // 断言：客户端缓存键使用该哈希，键顺序不同的同一思考参数共用客户端
        assertEquals(hashA, AiReasoningUtils.hashReasoningOptions(b));
        // 断言：内容不同、嵌套值不同、多覆盖字段，各自独立的客户端
        assertNotEquals(hashA, AiReasoningUtils.hashReasoningOptions(c));
        assertNotEquals(hashA, AiReasoningUtils.hashReasoningOptions(d));
        assertNotEquals(hashA, AiReasoningUtils.hashReasoningOptions(e));
        // 断言：getReasoningOptions 的深拷贝与原思考参数哈希一致
        AiModelDO model = managedModel();
        assertEquals(AiReasoningUtils.hashReasoningOptions(model.getReasoningConfig().getLevels().get(2).getOptions()),
                AiReasoningUtils.hashReasoningOptions(AiReasoningUtils.getReasoningOptions(model, "high")));
    }

    @Test
    public void testToCanonicalReasoningJson() {
        // 准备参数
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("z", 1);
        inner.put("a", false);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("b", inner);
        value.put("a", Arrays.asList(3, 1, 2));

        // 调用，并断言：各层键排序、数组保持顺序
        assertEquals("{\"a\":[3,1,2],\"b\":{\"a\":false,\"z\":1}}", AiReasoningUtils.toCanonicalReasoningJson(value));
        assertEquals(AiReasoningUtils.hashReasoningOptions(value), AiReasoningUtils.hashReasoningOptions(new TreeMap<>(value)));
        assertEquals(64, AiReasoningUtils.hashReasoningOptions(new HashMap<>()).length());
    }

    @Test
    public void testToCanonicalReasoningJson_readBack() {
        // 准备参数：倒序的 SortedMap（含嵌套），ORDER_MAP_ENTRIES_BY_KEYS 直接写出时不会重排
        Map<String, Object> inner = new TreeMap<>(Comparator.reverseOrder());
        inner.put("a", 1);
        inner.put("z", 2);
        Map<String, Object> reversed = new TreeMap<>(Comparator.reverseOrder());
        reversed.put("a", Arrays.asList(3, 1, 2));
        reversed.put("b", inner);
        // 准备参数：BigDecimal 不同 scale
        Map<String, Object> scale2 = new LinkedHashMap<>(Map.of("t", new BigDecimal("1.50")));
        Map<String, Object> scale1 = new LinkedHashMap<>(Map.of("t", new BigDecimal("1.5")));

        // 调用，并断言：读回后再排序，与普通 Map 一致
        assertEquals("{\"a\":[3,1,2],\"b\":{\"a\":1,\"z\":2}}", AiReasoningUtils.toCanonicalReasoningJson(reversed));
        // 调用，并断言：数值读回后统一写法，哈希一致
        assertEquals("{\"t\":1.5}", AiReasoningUtils.toCanonicalReasoningJson(scale2));
        assertEquals(AiReasoningUtils.hashReasoningOptions(scale1), AiReasoningUtils.hashReasoningOptions(scale2));
        assertEquals(AiReasoningUtils.hashReasoningOptions(Map.<String, Object>of("t", 1.5)), AiReasoningUtils.hashReasoningOptions(scale2));
    }


    // ========== 辅助方法 ==========

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        assertInstanceOf(Map.class, value);
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object value) {
        assertInstanceOf(List.class, value);
        return (List<Map<String, Object>>) value;
    }

    private static void validateReasoning(AiPlatformEnum platform, AiModelDO.ReasoningConfig config) {
        AiReasoningUtils.validateReasoningConfig(platform, platform, CHAT, config);
    }

    private static void assertConfigInvalid(AiPlatformEnum platform, AiModelDO.ReasoningConfig config) {
        ServiceException e = assertThrows(ServiceException.class, () -> validateReasoning(platform, config));
        assertEquals(MODEL_REASONING_CONFIG_INVALID.getCode(), e.getCode());
    }

    private static AiModelDO.ReasoningConfig config(String defaultLevel, AiModelDO.ReasoningLevel... levels) {
        return new AiModelDO.ReasoningConfig(new ArrayList<>(Arrays.asList(levels)), defaultLevel);
    }

    private static AiModelDO.ReasoningLevel level(String level, Map<String, Object> options) {
        return new AiModelDO.ReasoningLevel(level, options);
    }

    private static AiModelDO.ReasoningLevel emptyLevel(String level) {
        return level(level, new LinkedHashMap<>());
    }

    private static AiModelDO.ReasoningConfig deepSeekConfig() {
        return config("high", level("off", thinking("disabled", null)),
                level("low", thinking("enabled", "low")),
                level("high", thinking("enabled", "high")),
                level("max", thinking("enabled", "max")));
    }

    private static Map<String, Object> thinking(String type, String effort) {
        Map<String, Object> levelOptions = new LinkedHashMap<>();
        levelOptions.put("thinking", new LinkedHashMap<>(Map.of("type", type)));
        if (effort != null) {
            levelOptions.put("reasoning_effort", effort);
        }
        return levelOptions;
    }

    private static AiModelDO managedModel() {
        return new AiModelDO().setModel("deepseek-chat").setPlatform(AiPlatformEnum.DEEP_SEEK.getPlatform())
                .setType(CHAT).setReasoningConfig(deepSeekConfig());
    }

}
