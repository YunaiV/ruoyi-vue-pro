package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.*;

/**
 * AI 对话的工具类
 *
 * 构建对话的 ChatOptions、Message、工具上下文，读取对话响应的内容。
 * 思考档位见 {@link AiReasoningUtils}，Token 用量与响应元数据见 {@link AiUsageUtils}
 *
 * @author 芋道源码
 */
public class AiChatUtils {

    public static final String TOOL_CONTEXT_LOGIN_USER = "LOGIN_USER";
    public static final String TOOL_CONTEXT_TENANT_ID = "TENANT_ID";

    /**
     * 通义千问支持多模态的模型
     *
     * @see <a href="https://bailian.console.aliyun.com/cn-beijing/?tab=model#/model-market/all?providers=qwen&capabilities=VU">模型广场</a>
     * @see <a href="https://help.aliyun.com/zh/model-studio/error-code#error-url">必须开启 withMultiModel 参数</a>
     */
    public static final Set<String> TONG_YI_MULTI_MODELS = SetUtils.asSet(
            // qwen3.5 / 3.6 / 3.7 / 3.8 系列（统一多模态主干）
            // DONE @AI：3.8 搞进来；新的模型哈；（已按百炼文档加入 qwen3.8-max、qwen3.8-flash 及全模态 qwen3.8-omni-flash）
            "qwen3.8-max", "qwen3.8-flash",
            "qwen3.7-max", "qwen3.7-plus", "qwen3.7-flash",
            "qwen3.6-plus", "qwen3.6-flash",
            "qwen3.5-plus", "qwen3.5-flash",
            // qwen-vl 视觉理解
            "qwen3-vl-plus", "qwen3-vl-flash",
            "qwen-vl-max", "qwen-vl-plus",
            "qwen2.5-vl-72b-instruct", "qwen2.5-vl-32b-instruct",
            "qwen2.5-vl-7b-instruct", "qwen2.5-vl-3b-instruct",
            // qvq 视觉推理
            "qvq-max", "qvq-plus",
            // qwen-omni 全模态
            "qwen3.8-omni-flash",
            "qwen3.5-omni-plus", "qwen3.5-omni-flash",
            "qwen3-omni-flash", "qwen-omni-turbo"
    );

    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens) {
        return buildChatOptions(platform, model, temperature, maxTokens, null, null);
    }

    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens,
                                               List<ToolCallback> toolCallbacks, Map<String, Object> toolContext) {
        return buildChatOptions(platform, model, temperature, maxTokens, toolCallbacks, toolContext, false);
    }

    /**
     * 构建 ChatOptions
     *
     * @param hasMedia 本次 Prompt 是否含 Media（包括选入上下文的历史消息）。通义含 Media 时必须走多模态接口
     */
    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens,
                                               List<ToolCallback> toolCallbacks, Map<String, Object> toolContext,
                                               boolean hasMedia) {
        return buildChatOptions(platform, model, temperature, maxTokens, toolCallbacks, toolContext, hasMedia, null);
    }

    /**
     * 构建 ChatOptions（带思考参数）
     *
     * @param hasMedia 本次 Prompt 是否含 Media（包括选入上下文的历史消息）。通义含 Media 时必须走多模态接口
     * @param reasoningOptions 思考参数（{@link AiReasoningUtils#getReasoningOptions} 返回的深拷贝），null 表示未配置思考，与原有行为一致。
     *                       通义映射到原生字段；OpenAI 路由 reasoning_effort 写强类型字段、其余键追加到 extraBody；
     *                       DeepSeek 族不处理，参数已装在 ChatModel 实例的 HTTP 层
     */
    public static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens,
                                               List<ToolCallback> toolCallbacks, Map<String, Object> toolContext,
                                               boolean hasMedia, Map<String, Object> reasoningOptions) {
        toolCallbacks = ObjUtil.defaultIfNull(toolCallbacks, Collections.emptyList());
        toolContext = ObjUtil.defaultIfNull(toolContext, Collections.emptyMap());
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                DashScopeChatOptions.DashScopeChatOptionsBuilder dashScopeBuilder = DashScopeChatOptions.builder()
                        .model(model).temperature(temperature).maxToken(maxTokens)
                        .enableThinking(true) // TODO 芋艿：默认都开启 thinking 模式，后续可以让用户配置
                        .multiModel(hasMedia || TONG_YI_MULTI_MODELS.contains(model)) // 含 Media 必须走多模态接口；纯文本按名单保持原通道
                        .toolCallbacks(toolCallbacks).toolContext(toolContext);
                if (reasoningOptions != null) {
                    // 已配置思考：保存时已保证 enable_thinking 为布尔值、thinking_budget 为正整数
                    dashScopeBuilder.enableThinking(
                            (Boolean) reasoningOptions.get(AiReasoningUtils.TONG_YI_ENABLE_THINKING));
                    Object budget = reasoningOptions.get(AiReasoningUtils.TONG_YI_THINKING_BUDGET);
                    if (budget instanceof Integer) {
                        dashScopeBuilder.thinkingBudget((Integer) budget);
                    }
                }
                return dashScopeBuilder.build();
            case DEEP_SEEK:
            case DOU_BAO: // 复用 DeepSeek 客户端
            case HUN_YUAN: // 复用 DeepSeek 客户端
            case SILICON_FLOW: // 复用 DeepSeek 客户端
            case YI_YAN: // 复用 DeepSeek 客户端
            case ZHI_PU: // 复用 DeepSeek 客户端
            case XING_HUO: // 复用 DeepSeek 客户端
            case MINI_MAX: // 复用 DeepSeek 客户端
            case MOONSHOT: // 复用 DeepSeek 客户端
            case BAI_CHUAN: // 复用 DeepSeek 客户端
            case STEP_FUN: // 复用 DeepSeek 客户端
            case MI_MO: // 复用 DeepSeek 客户端
                return DeepSeekChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case OPENAI:
            case GEMINI: // 复用 OpenAI 客户端
            case GROK: // 复用 OpenAI 客户端
                OpenAiChatOptions.Builder openAiBuilder = OpenAiChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .streamUsage(platform == AiPlatformEnum.OPENAI) // OpenAI 流式需开启才返回用量；复用客户端的厂商保持不发送
                        .toolCallbacks(toolCallbacks).toolContext(toolContext);
                if (reasoningOptions != null) {
                    // 已配置思考：reasoning_effort 写强类型字段；其余键只追加到 extraBody（保存时已拒绝与强类型字段同名的键和 null）
                    Map<String, Object> extraBody = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> entry : reasoningOptions.entrySet()) {
                        if (AiReasoningUtils.OPENAI_REASONING_EFFORT.equals(entry.getKey())) {
                            openAiBuilder.reasoningEffort((String) entry.getValue());
                        } else {
                            extraBody.put(entry.getKey(), entry.getValue());
                        }
                    }
                    if (!extraBody.isEmpty()) {
                        openAiBuilder.extraBody(extraBody);
                    }
                }
                return openAiBuilder.build();
            case AZURE_OPENAI:
                return AzureOpenAiChatOptions.builder().deploymentName(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case ANTHROPIC:
                return AnthropicChatOptions.builder().model(model).temperature(temperature).maxTokens(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            case OLLAMA:
                return OllamaChatOptions.builder().model(model).temperature(temperature).numPredict(maxTokens)
                        .toolCallbacks(toolCallbacks).toolContext(toolContext).build();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    public static Message buildMessage(String type, String content) {
        if (MessageType.USER.getValue().equals(type)) {
            return new UserMessage(content);
        }
        if (MessageType.ASSISTANT.getValue().equals(type)) {
            return new AssistantMessage(content);
        }
        if (MessageType.SYSTEM.getValue().equals(type)) {
            return new SystemMessage(content);
        }
        if (MessageType.TOOL.getValue().equals(type)) {
            throw new UnsupportedOperationException("暂不支持 tool 消息：" + content);
        }
        throw new IllegalArgumentException(StrUtil.format("未知消息类型({})", type));
    }

    public static Map<String, Object> buildCommonToolContext() {
        Map<String, Object> context = new HashMap<>();
        context.put(TOOL_CONTEXT_LOGIN_USER, SecurityFrameworkUtils.getLoginUser());
        context.put(TOOL_CONTEXT_TENANT_ID, TenantContextHolder.getTenantId());
        return context;
    }

    @SuppressWarnings("ConstantValue")
    public static String getChatResponseContent(ChatResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            return null;
        }
        return response.getResult().getOutput().getText();
    }

    @SuppressWarnings("ConstantValue")
    public static String getChatResponseReasoningContent(ChatResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            return null;
        }
        AssistantMessage output = response.getResult().getOutput();
        // DeepSeek 通过专属 AssistantMessage 暴露 reasoningContent
        if (output instanceof DeepSeekAssistantMessage) {
            return ((DeepSeekAssistantMessage) output).getReasoningContent();
        }
        // 通义千问等通过 metadata 透传 reasoningContent
        return MapUtil.getStr(output.getMetadata(), "reasoningContent");
    }

}
