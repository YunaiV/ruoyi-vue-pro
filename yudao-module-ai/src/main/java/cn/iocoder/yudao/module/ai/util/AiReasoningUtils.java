package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
     * HTTP 路由顶层禁止的键：由 SDK 负责，思考参数改了会破坏请求
     */
    private static final Set<String> REASONING_HTTP_FORBIDDEN_KEYS = SetUtils.asSet(
            "model", "messages", "stream", "stream_options", "tools", "tool_choice");

    /**
     * OpenAI 路由（Spring AI 1.1.8）禁止的键：与 OpenAiApi.ChatCompletionRequest 强类型字段的 @JsonProperty 同名，
     * extraBody 通过 @JsonAnyGetter 展平到顶层，同名会出现重复键。reasoning_effort 单独写入强类型字段，不在此列
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
         * 通义：只认 enable_thinking、thinking_budget，映射到 DashScope 原生字段
         */
        TONG_YI,
        /**
         * 不支持思考配置
         */
        UNSUPPORTED

    }

    /**
     * 获得平台的思考参数路由（Spring AI 1.1.8）
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
            case GEMINI: // JDK17 的 Gemini 走 OpenAI 兼容层
                return ReasoningRoute.OPENAI;
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
     * @param config 思考配置
     */
    public static void validateReasoningConfig(AiPlatformEnum platform, AiPlatformEnum apiKeyPlatform, Integer type,
                                               AiModelDO.ReasoningConfig config) {
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
            validateReasoningOptionKeys(route, item.getLevel(), item.getOptions());
        }
    }

    /**
     * 按路由校验思考参数的顶层键，避免覆盖 SDK 负责的字段或传入平台不认的字段
     *
     * @param route 平台路由
     * @param level 档位值，用于错误提示
     * @param options 该档位的思考参数
     */
    private static void validateReasoningOptionKeys(ReasoningRoute route, String level, Map<String, Object> options) {
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
                // TONG_YI：映射到 DashScope 原生字段，只认 enable_thinking（必填布尔）、thinking_budget（可选正整数）
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
            default:
                throw exception(MODEL_REASONING_CONFIG_INVALID, "不支持的路由");
        }
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
        } catch (IOException e) {
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
        } catch (IOException e) {
            throw new IllegalArgumentException("思考参数复制失败", e);
        }
    }

}
