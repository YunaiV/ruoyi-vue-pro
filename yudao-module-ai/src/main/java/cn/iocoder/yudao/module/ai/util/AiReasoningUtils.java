package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 思考（reasoning）档位的工具类
 *
 * 包括平台路由、保存校验、发送时解析档位与思考参数、思考参数的规范化 JSON 与哈希
 *
 * @author 芋道源码
 */
public class AiReasoningUtils {

    /**
     * 关闭思考的档位值；只是约定的名字，实际关闭靠该档的思考参数
     */
    public static final String REASONING_LEVEL_OFF = "off";

    /**
     * 通义的思考参数键
     */
    public static final String TONG_YI_ENABLE_THINKING = "enable_thinking";
    public static final String TONG_YI_THINKING_BUDGET = "thinking_budget";

    /**
     * OpenAI 路由中写入强类型字段的键
     */
    public static final String OPENAI_REASONING_EFFORT = "reasoning_effort";

    /**
     * Gemini 的思考参数键：reasoning_effort 与 OpenAI 路由同名，映射到 thinkingLevel；thinking_budget 映射到 thinkingBudget
     */
    public static final String GEMINI_THINKING_BUDGET = "thinking_budget";
    /**
     * Gemini 关闭思考的 reasoning_effort 值，映射为 thinkingBudget = 0
     */
    public static final String GEMINI_REASONING_EFFORT_NONE = "none";
    /**
     * Gemini 支持的 reasoning_effort 值，对应 GoogleGenAiThinkingLevel 的 MINIMAL、LOW、MEDIUM、HIGH，以及关闭思考的 none
     */
    private static final Set<String> GEMINI_REASONING_EFFORTS = SetUtils.asSet(
            "minimal", "low", "medium", "high", GEMINI_REASONING_EFFORT_NONE);
    /**
     * Gemini 模型名中的主版本号，例如 gemini-2.5-flash 为 2、gemini-3-pro-preview 为 3
     */
    private static final Pattern GEMINI_MAJOR_VERSION_PATTERN = Pattern.compile("gemini-(\\d+)");
    /**
     * Gemini 支持 thinkingLevel 的最小主版本号：Google 文档说明 thinkingLevel 用于 Gemini 3 及以上，更早的模型使用会报错；
     * Gemini 3 及以上也不能关闭思考
     */
    private static final int GEMINI_THINKING_LEVEL_MIN_VERSION = 3;
    /**
     * Gemini 3 以前不能关闭思考的型号：Google 文档说明 2.5 Pro 不能关闭思考（2.5 Flash、Flash-Lite 可以用 thinking_budget = 0 关闭）
     */
    private static final String GEMINI_25_PRO_MODEL = "gemini-2.5-pro";

    /**
     * HTTP 路由顶层禁止的键：由 SDK 负责，思考参数改了会破坏请求
     */
    private static final Set<String> REASONING_HTTP_FORBIDDEN_KEYS = SetUtils.asSet(
            "model", "messages", "stream", "stream_options", "tools", "tool_choice");

    /**
     * OpenAI 路由禁止的键：与 Chat Completions 请求强类型字段同名。
     * extraBody 由 Spring AI 2.x 写入 OpenAI SDK 的 additionalBodyProperties，展平到请求体顶层，同名会出现重复键。
     * reasoning_effort 单独写入强类型字段，不在此列
     */
    private static final Set<String> REASONING_OPENAI_FORBIDDEN_KEYS = SetUtils.asSet(
            "audio", "frequency_penalty", "logit_bias", "logprobs", "max_completion_tokens", "max_tokens",
            "messages", "metadata", "modalities", "model", "n", "parallel_tool_calls", "presence_penalty",
            "prompt_cache_key", "response_format", "safety_identifier", "seed", "service_tier", "stop", "store",
            "stream", "stream_options", "temperature", "tool_choice", "tools", "top_logprobs", "top_p", "user",
            "verbosity", "web_search_options", "extra_body");

    /**
     * 规范化 JSON 使用的 ObjectMapper：Map 键排序
     */
    private static final ObjectMapper REASONING_CANONICAL_MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();

    private static final TypeReference<LinkedHashMap<String, Object>> REASONING_OPTIONS_TYPE =
            new TypeReference<LinkedHashMap<String, Object>>() {};

    /**
     * 思考参数的落地方式
     */
    public enum ReasoningRoute {

        /**
         * 复用 DeepSeek 客户端的平台：在客户端实例的 HTTP 层做 RFC 7396 合并
         */
        HTTP,
        /**
         * OpenAI 兼容客户端：reasoning_effort 写强类型字段，其余键追加到 extraBody
         */
        OPENAI,
        /**
         * 通义：只认 enable_thinking、thinking_budget，经百炼 OpenAI 兼容接口的 extraBody 发送
         */
        TONG_YI,
        /**
         * Gemini：Spring AI 2.x 使用 Google GenAI 原生客户端，只认 reasoning_effort、thinking_budget，映射到 ThinkingConfig
         */
        GEMINI,
        /**
         * 不支持思考配置
         */
        UNSUPPORTED

    }

    /**
     * 获得平台的思考参数路由（Spring AI 2.x）
     *
     * @param platform 平台
     * @return 路由
     */
    public static ReasoningRoute getReasoningRoute(AiPlatformEnum platform) {
        if (platform == null) {
            return ReasoningRoute.UNSUPPORTED;
        }
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case DEEP_SEEK:
            case DOU_BAO:
            case HUN_YUAN:
            case SILICON_FLOW:
            case YI_YAN:
            case ZHI_PU:
            case XING_HUO:
            case MINI_MAX:
            case MOONSHOT:
            case BAI_CHUAN:
            case STEP_FUN:
            case MI_MO:
                return ReasoningRoute.HTTP;
            case OPENAI:
            case GROK:
                return ReasoningRoute.OPENAI;
            case GEMINI: // JDK25 的 Gemini 走 Google GenAI 原生客户端，不是 OpenAI 兼容层
                return ReasoningRoute.GEMINI;
            case TONG_YI:
                return ReasoningRoute.TONG_YI;
            default:
                return ReasoningRoute.UNSUPPORTED;
        }
    }

    public static boolean isReasoningHttpRoute(AiPlatformEnum platform) {
        return getReasoningRoute(platform) == ReasoningRoute.HTTP;
    }

    /**
     * 模型是否配置了思考档位；配置为 null、档位为空都视为未配置
     */
    public static boolean isReasoningConfigured(AiModelDO model) {
        return model != null && isReasoningConfigured(model.getReasoningConfig());
    }

    /**
     * 思考配置是否有档位；为 null、档位为空都视为未配置
     */
    public static boolean isReasoningConfigured(AiModelDO.ReasoningConfig config) {
        return config != null && CollUtil.isNotEmpty(config.getLevels());
    }

    // ========== 思考（reasoning）档位：保存 ==========

    /**
     * 校验思考配置的业务规则（创建、更新共用）：模型类型、平台路由、平台与 API 密钥一致、路由的键规则
     *
     * 档位格式、判重、默认档位、思考参数 null 由 AiModelSaveReqVO 校验；未配置（null 或档位为空）时不校验
     *
     * @param platform 模型平台
     * @param apiKeyPlatform API 密钥的平台
     * @param type 模型类型
     * @param model 模型标志，Gemini 按实际模型的版本校验
     * @param config 思考配置
     */
    public static void validateReasoningConfig(AiPlatformEnum platform, AiPlatformEnum apiKeyPlatform, Integer type,
                                               String model, AiModelDO.ReasoningConfig config) {
        // 1. 未配置或清空：不做平台相关的校验
        if (!isReasoningConfigured(config)) {
            return;
        }

        // 2. 模型类型、平台路由、平台一致
        if (!Objects.equals(type, AiModelTypeEnum.CHAT.getType())) {
            throw exception(MODEL_REASONING_CONFIG_INVALID, "只有对话模型可以配置思考");
        }
        ReasoningRoute route = getSupportedReasoningRoute(platform);
        if (platform != apiKeyPlatform) {
            throw exception(MODEL_REASONING_PLATFORM_MISMATCH, platform.getPlatform(),
                    apiKeyPlatform == null ? null : apiKeyPlatform.getPlatform());
        }

        // 3. 逐个档位：思考参数满足路由的键规则
        for (AiModelDO.ReasoningLevel item : config.getLevels()) {
            validateReasoningOptionKeys(route, model, item.getLevel(), item.getOptions());
        }
    }

    /**
     * 按路由校验思考参数的顶层键，避免覆盖 SDK 负责的字段或传入平台不认的字段
     *
     * @param route 平台路由
     * @param model 模型标志，Gemini 按实际模型的版本校验
     * @param level 档位值，用于错误提示
     * @param options 该档位的思考参数
     */
    private static void validateReasoningOptionKeys(ReasoningRoute route, String model, String level,
                                                    Map<String, Object> options) {
        switch (route) {
            case HTTP:
                // HTTP：整体合并到请求体，只禁止 SDK 负责的键
                for (String key : options.keySet()) {
                    if (REASONING_HTTP_FORBIDDEN_KEYS.contains(key)) {
                        throw exception(MODEL_REASONING_CONFIG_INVALID,
                                "档位(" + level + ")的思考参数不能包含 " + key);
                    }
                }
                return;
            case OPENAI:
                // OPENAI：extraBody 展平到顶层，不能与强类型字段同名
                for (String key : options.keySet()) {
                    if (REASONING_OPENAI_FORBIDDEN_KEYS.contains(key)) {
                        throw exception(MODEL_REASONING_CONFIG_INVALID,
                                "档位(" + level + ")的思考参数不能覆盖已有字段：" + key);
                    }
                }
                // reasoning_effort 写入强类型字段，只能是非空字符串
                Object effort = options.get(OPENAI_REASONING_EFFORT);
                if (effort != null && (!(effort instanceof String) || StrUtil.isBlank((String) effort))) {
                    throw exception(MODEL_REASONING_CONFIG_INVALID,
                            "档位(" + level + ")的 reasoning_effort 必须是非空字符串");
                }
                return;
            case TONG_YI:
                // TONG_YI：经百炼 OpenAI 兼容接口的 extraBody 发送，只认 enable_thinking（必填布尔）、thinking_budget（可选正整数）
                for (String key : options.keySet()) {
                    if (!TONG_YI_ENABLE_THINKING.equals(key) && !TONG_YI_THINKING_BUDGET.equals(key)) {
                        throw exception(MODEL_REASONING_CONFIG_INVALID,
                                "档位(" + level + ")的通义思考参数只支持 enable_thinking、thinking_budget，不支持 " + key);
                    }
                }
                if (!(options.get(TONG_YI_ENABLE_THINKING) instanceof Boolean)) {
                    throw exception(MODEL_REASONING_CONFIG_INVALID,
                            "档位(" + level + ")的通义思考参数必须包含布尔值 enable_thinking");
                }
                if (options.containsKey(TONG_YI_THINKING_BUDGET)) {
                    Object budget = options.get(TONG_YI_THINKING_BUDGET);
                    if (!(budget instanceof Integer) || (Integer) budget <= 0) {
                        throw exception(MODEL_REASONING_CONFIG_INVALID,
                                "档位(" + level + ")的 thinking_budget 必须是正整数");
                    }
                }
                return;
            case GEMINI:
                // GEMINI：映射到 Google GenAI 原生字段，reasoning_effort、thinking_budget 只能二选一（Gemini 不允许同时设置）
                for (String key : options.keySet()) {
                    if (!OPENAI_REASONING_EFFORT.equals(key) && !GEMINI_THINKING_BUDGET.equals(key)) {
                        throw exception(MODEL_REASONING_CONFIG_INVALID,
                                "档位(" + level + ")的 Gemini 思考参数只支持 reasoning_effort、thinking_budget，不支持 " + key);
                    }
                }
                if (options.size() != 1) {
                    throw exception(MODEL_REASONING_CONFIG_INVALID,
                            "档位(" + level + ")的 Gemini 思考参数必须且只能包含 reasoning_effort、thinking_budget 其中一个");
                }
                if (options.containsKey(OPENAI_REASONING_EFFORT)) {
                    validateGeminiReasoningEffort(model, level, options.get(OPENAI_REASONING_EFFORT));
                }
                if (options.containsKey(GEMINI_THINKING_BUDGET)) {
                    // -1 表示动态思考，0 表示关闭思考，正数为思考 Token 上限
                    Object budget = options.get(GEMINI_THINKING_BUDGET);
                    if (!(budget instanceof Integer) || (Integer) budget < -1) {
                        throw exception(MODEL_REASONING_CONFIG_INVALID,
                                "档位(" + level + ")的 thinking_budget 必须是不小于 -1 的整数");
                    }
                    if ((Integer) budget == 0) {
                        validateGeminiDisableThinking(model, level, "thinking_budget = 0");
                    }
                }
                return;
            default:
                throw exception(MODEL_REASONING_CONFIG_INVALID, "不支持的路由");
        }
    }

    /**
     * 按实际模型的版本，校验 Gemini 的 reasoning_effort
     *
     * 1. minimal、low、medium、high 映射为 thinkingLevel，只适用于 Gemini 3 及以上；
     *    更早的模型（如 2.5）请改用 thinking_budget，这里不按档位估算预算
     * 2. none 映射为 thinkingBudget = 0 关闭思考，与 thinking_budget = 0 按同一规则校验，参见 {@link #validateGeminiDisableThinking}
     * 3. 无法从模型名识别版本时，无法确定映射，请改用 thinking_budget
     *
     * @param model 模型标志
     * @param level 档位值，用于错误提示
     * @param effort reasoning_effort 的值
     */
    private static void validateGeminiReasoningEffort(String model, String level, Object effort) {
        if (!GEMINI_REASONING_EFFORTS.contains(effort)) {
            throw exception(MODEL_REASONING_CONFIG_INVALID,
                    "档位(" + level + ")的 reasoning_effort 只支持 minimal、low、medium、high、none");
        }
        Integer version = getGeminiMajorVersion(model);
        if (version == null) {
            throw exception(MODEL_REASONING_CONFIG_INVALID, "档位(" + level + ")：无法从模型(" + model
                    + ")识别 Gemini 版本，reasoning_effort 无法确定映射，请改用 thinking_budget");
        }
        if (GEMINI_REASONING_EFFORT_NONE.equals(effort)) {
            validateGeminiDisableThinking(model, level, "reasoning_effort = none");
            return;
        }
        if (version < GEMINI_THINKING_LEVEL_MIN_VERSION) {
            throw exception(MODEL_REASONING_CONFIG_INVALID, "档位(" + level + ")：模型(" + model
                    + ")早于 Gemini 3，不支持 thinkingLevel，reasoning_effort 只能为 none，其它请改用 thinking_budget");
        }
    }

    /**
     * 校验 Gemini 能否关闭思考：reasoning_effort = none 与 thinking_budget = 0 意图相同，按同一规则
     *
     * Google 文档说明 Gemini 3 及以上、2.5 Pro 不能关闭思考；无法从模型名识别版本时不猜测，交给厂商校验
     *
     * @param model 模型标志
     * @param level 档位值，用于错误提示
     * @param param 关闭思考的参数，用于错误提示
     */
    private static void validateGeminiDisableThinking(String model, String level, String param) {
        Integer version = getGeminiMajorVersion(model);
        if (version == null) {
            return;
        }
        String name;
        if (version >= GEMINI_THINKING_LEVEL_MIN_VERSION) {
            name = "Gemini 3 及以上";
        } else if (model.toLowerCase(Locale.ROOT).contains(GEMINI_25_PRO_MODEL)) {
            name = "Gemini 2.5 Pro";
        } else {
            return;
        }
        throw exception(MODEL_REASONING_CONFIG_INVALID, "档位(" + level + ")：模型(" + model
                + ")为 " + name + "，不能关闭思考，不支持 " + param);
    }

    /**
     * 从模型名解析 Gemini 主版本号
     *
     * @param model 模型标志，例如 gemini-2.5-flash、models/gemini-3-pro-preview
     * @return 主版本号；无法识别时为 null
     */
    private static Integer getGeminiMajorVersion(String model) {
        if (StrUtil.isEmpty(model)) {
            return null;
        }
        Matcher matcher = GEMINI_MAJOR_VERSION_PATTERN.matcher(model.toLowerCase(Locale.ROOT));
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

    // ========== 思考（reasoning）档位：发送 ==========

    /**
     * 解析本次生效的思考档位
     *
     * @param model 模型
     * @param requested 请求的档位，可为空
     * @return 生效档位；null 表示模型未配置思考，保持原有行为
     */
    public static String resolveReasoningLevel(AiModelDO model, String requested) {
        if (!isReasoningConfigured(model)) {
            if (StrUtil.isNotEmpty(requested)) {
                throw exception(MODEL_REASONING_NOT_CONFIGURED);
            }
            return null;
        }
        AiModelDO.ReasoningConfig config = model.getReasoningConfig();
        if (StrUtil.isEmpty(requested)) {
            // 保存时已保证非空且在列表中；库里的数据被改坏时报配置错误，不静默回退
            String defaultLevel = config.getDefaultLevel();
            if (StrUtil.isEmpty(defaultLevel) || findReasoningLevel(config, defaultLevel) == null) {
                throw exception(MODEL_REASONING_CONFIG_INVALID, "默认档位不在档位列表中");
            }
            return defaultLevel;
        }
        if (findReasoningLevel(config, requested) == null) {
            throw exception(MODEL_REASONING_LEVEL_NOT_SUPPORTED, requested);
        }
        return requested;
    }

    private static AiModelDO.ReasoningLevel findReasoningLevel(AiModelDO.ReasoningConfig config, String level) {
        if (config == null) {
            return null;
        }
        return CollUtil.findOne(config.getLevels(), item -> item != null && Objects.equals(item.getLevel(), level));
    }

    /**
     * 校验已配置思考的模型，其平台路由仍被支持（例如库里改过平台）
     *
     * @param platform 平台（已校验与 API 密钥一致）
     * @param model 模型
     */
    public static void validateReasoningRoute(AiPlatformEnum platform, AiModelDO model) {
        if (!isReasoningConfigured(model)) {
            return;
        }
        getSupportedReasoningRoute(platform);
    }

    /**
     * 发送前，按实际模型校验 Gemini 档位的思考参数
     *
     * JDK17 的 Gemini 走 OpenAI 兼容层，库里沿用的思考参数（例如 2.5 的 reasoning_effort = high、兼容层的其它键），
     * 在 Google GenAI 原生客户端下可能不再适用。这里与保存时同一规则，报清楚的配置错误，不静默丢弃、不发送模型不支持的 thinkingLevel
     *
     * @param platform 平台（已校验与 API 密钥一致）
     * @param model 模型标志
     * @param level 生效档位，可为 null
     * @param options 该档位的思考参数
     */
    public static void validateGeminiReasoningOptions(AiPlatformEnum platform, String model, String level,
                                                      Map<String, Object> options) {
        if (level == null || getReasoningRoute(platform) != ReasoningRoute.GEMINI) {
            return;
        }
        validateReasoningOptionKeys(ReasoningRoute.GEMINI, model, level, options);
    }

    private static ReasoningRoute getSupportedReasoningRoute(AiPlatformEnum platform) {
        ReasoningRoute route = getReasoningRoute(platform);
        if (route == ReasoningRoute.UNSUPPORTED) {
            throw exception(MODEL_REASONING_PLATFORM_NOT_SUPPORTED, platform == null ? null : platform.getPlatform());
        }
        return route;
    }

    /**
     * 获得档位对应的思考参数（深拷贝，调用方可以随意修改）
     *
     * @param model 模型
     * @param level 生效档位，可为 null
     * @return 思考参数；level 为 null 时返回 null
     */
    public static Map<String, Object> getReasoningOptions(AiModelDO model, String level) {
        if (level == null) {
            return null;
        }
        AiModelDO.ReasoningLevel item = findReasoningLevel(model.getReasoningConfig(), level);
        if (item == null || item.getOptions() == null) {
            throw exception(MODEL_REASONING_CONFIG_INVALID, "档位(" + level + ")缺少思考参数");
        }
        return deepCopyReasoningOptions(item.getOptions());
    }

    // ========== 思考（reasoning）档位：规范化 JSON ==========

    /**
     * 思考参数的规范化 JSON：各层对象键递归排序，数组保持原顺序
     *
     * @param value Map / List / 基本类型
     * @return JSON 字符串
     */
    public static String toCanonicalReasoningJson(Object value) {
        try {
            // 先读回成 LinkedHashMap / List 结构，再按键排序序列化。不能直接写出：
            // 1. ORDER_MAP_ENTRIES_BY_KEYS 遇到 SortedMap 会认为已排好序，自定义比较器（如倒序）的顺序原样输出
            // 2. 读回统一了数值写法，BigDecimal 的 1.50 与 1.5 规范化后一致
            Object tree = REASONING_CANONICAL_MAPPER.readValue(REASONING_CANONICAL_MAPPER.writeValueAsBytes(value), Object.class);
            return REASONING_CANONICAL_MAPPER.writeValueAsString(tree);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("思考参数序列化失败", e);
        }
    }

    /**
     * 思考参数哈希：sha256Hex(toCanonicalReasoningJson(options))，用于客户端缓存键
     */
    public static String hashReasoningOptions(Map<String, Object> options) {
        return DigestUtil.sha256Hex(toCanonicalReasoningJson(options));
    }

    private static Map<String, Object> deepCopyReasoningOptions(Map<String, Object> options) {
        try {
            return REASONING_CANONICAL_MAPPER.readValue(REASONING_CANONICAL_MAPPER.writeValueAsBytes(options), REASONING_OPTIONS_TYPE);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("思考参数复制失败", e);
        }
    }

}
