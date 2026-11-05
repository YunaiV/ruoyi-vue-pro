package cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.openai.OpenAiDelegateChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;

/**
 * 通义千问 {@link ChatModel} 实现类
 *
 * 基于阿里云百炼的 OpenAI 兼容接口，复用 {@link OpenAiChatModel}，流式思考的处理参见 {@link OpenAiDelegateChatModel}。
 * 原因：spring-ai-alibaba-dashscope 2.0.0-M1.1 基于 Spring AI 2.0.0-M1 编译，其 DashScopeChatModel 与 Spring AI 2.0.x 二进制不兼容
 * （DashScopeChatOptions 缺少 ChatOptions#mutate，ModelOptionsUtils#copyToTarget 等已删除）
 *
 * @author 芋道源码
 */
public class TongYiChatModel extends OpenAiDelegateChatModel {

    public static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    public static final String MODEL_DEFAULT = "qwen-plus";

    public TongYiChatModel(ChatModel openAiChatModel) {
        super(openAiChatModel);
    }

}
