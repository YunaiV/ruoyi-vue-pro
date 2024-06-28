package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenOptions;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.YiYanChatOptions;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI 写作 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiWriteServiceImpl implements AiWriteService {

    @Resource
    private AiApiKeyService apiKeyService;


    @Override
    public Flux<CommonResult<String>> generateComposition(AiWriteGenerateReqVO generateReqVO) {
        StreamingChatClient chatClient = apiKeyService.getStreamingChatClient(6L);
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform("QianWen");
        ChatOptions chatOptions = buildChatOptions(platform, "qwen-72b-chat", 1.0, 1000);
        Prompt prompt = new Prompt(buildWritingPrompt(generateReqVO), chatOptions);
        Flux<ChatResponse> streamResponse = chatClient.stream(prompt);
        // 3.3 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getContent() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果
            return success(newContent);
        }).doOnComplete(() -> {
            log.info("generateComposition complete, content: {}", contentBuffer);
        }).onErrorResume(error -> {
            log.error("[AI 写作] 发生异常", error);
            return Flux.just(error(ErrorCodeConstants.AI_CHAT_STREAM_ERROR));
        });
    }


    private String buildWritingPrompt(AiWriteGenerateReqVO generateReqVO) {
        String template = "请直接写一篇关于 [{}] 的文章，格式为：{}，语气为：{}，语言为：{}，长度为：{}。请确保涵盖主要内容，不需要任何额外的解释或道歉。";
        String content = generateReqVO.getContent();
        String format = generateReqVO.getFormat();
        String tone = generateReqVO.getTone();
        String language = generateReqVO.getLanguage();
        String length = generateReqVO.getLength();
        return StrUtil.format(template, content, format, tone, language, length);
    }

    // TODO 芋艿：复用
    private static ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens) {
        Float temperatureF = temperature != null ? temperature.floatValue() : null;
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                return OpenAiChatOptions.builder().withModel(model).withTemperature(temperatureF).withMaxTokens(maxTokens).build();
            case OLLAMA:
                return OllamaOptions.create().withModel(model).withTemperature(temperatureF).withNumPredict(maxTokens);
            case YI_YAN:
                // TODO @fan：增加一个 model
                return new YiYanChatOptions().setTemperature(temperatureF).setMaxOutputTokens(maxTokens);
            case XING_HUO:
                return new XingHuoOptions().setChatModel(XingHuoChatModel.valueOfModel(model)).setTemperature(temperatureF)
                        .setMaxTokens(maxTokens);
            case QIAN_WEN:
                // TODO @fan:增加 model、temperature 参数
                return new QianWenOptions().setMaxTokens(maxTokens);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

}
