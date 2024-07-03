package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.write.*;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.qianfan.QianFanChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Objects;

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
    @Resource
    private AiChatModelService chatModalService;
    @Resource
    private AiWriteMapper writeMapper;


    @Override
    public Flux<CommonResult<String>> generateWriteContent(AiWriteGenerateReqVO generateReqVO, Long userId) {
        //TODO 芋艿 写作的模型配置放哪好 先用千问测试
        // 1.1 校验模型
        AiChatModelDO model = chatModalService.validateChatModel(14L);
        StreamingChatModel chatClient = apiKeyService.getStreamingChatClient(model.getKeyId());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());

        //1.2 插入写作信息
        AiWriteDO writeDO = BeanUtils.toBean(generateReqVO, AiWriteDO.class);
        writeMapper.insert(writeDO.setUserId(userId).setModel(model.getModel()).setPlatform(platform.getPlatform()));

        //2.1 构建提示词
        Prompt prompt = new Prompt(buildWritingPrompt(generateReqVO), chatOptions);
        Flux<ChatResponse> streamResponse = chatClient.stream(prompt);
        // 2.2 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getContent() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果
            return success(newContent);
        }).doOnComplete(() -> {
            writeMapper.updateById(new AiWriteDO().setId(writeDO.getId()).setGeneratedContent(contentBuffer.toString()));
        }).doOnError(throwable -> {
            log.error("[AI Write][generateReqVO({}) 发生异常]", generateReqVO, throwable);
            writeMapper.updateById(new AiWriteDO().setId(writeDO.getId()).setErrorMessage(throwable.getMessage()));
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.WRITE_STREAM_ERROR)));
    }


    private String buildWritingPrompt(AiWriteGenerateReqVO generateReqVO) {
        String template;
        Integer writeType = generateReqVO.getType();
        String format = AiWriteFormatEnum.valueOfFormat(generateReqVO.getFormat()).getName();
        String tone = AiWriteToneEnum.valueOfTone(generateReqVO.getTone()).getName();
        String language = AiLanguageEnum.valueOfLanguage(generateReqVO.getLanguage()).getName();
        String length = AiWriteLengthEnum.valueOfLength(generateReqVO.getLength()).getName();
        if (Objects.equals(writeType, AiWriteTypeEnum.WRITING.getType())) {
            template = "请撰写一篇关于 [{}] 的文章。文章的内容格式为：[{}]，语气为：[{}]，语言为：[{}]，长度为：[{}]。请确保涵盖主要内容，不需要除了正文内容外的其他回复，如标题、额外的解释或道歉。";
            return StrUtil.format(template, generateReqVO.getPrompt(), format, tone, language, length);
        } else if (Objects.equals(writeType, AiWriteTypeEnum.REPLY.getType())) {
            template = "请针对如下内容：[{}] 做个回复。回复内容参考：[{}], 回复的内容格式为：[{}]，语气为：[{}]，语言为：[{}]，长度为：[{}]。不需要除了正文内容外的其他回复，如标题、额外的解释或道歉。";
            return StrUtil.format(template, generateReqVO.getOriginalContent(), generateReqVO.getPrompt(), format, tone, language, length);
        } else {
            throw new IllegalArgumentException(StrUtil.format("未知写作类型({})", writeType));
        }
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
                // TODO 芋艿：貌似 model 只要一设置，就报错
//                return QianFanChatOptions.builder().withModel(model).withTemperature(temperatureF).withMaxTokens(maxTokens).build();
                return QianFanChatOptions.builder().withTemperature(temperatureF).withMaxTokens(maxTokens).build();
            case XING_HUO:
                return new XingHuoOptions().setChatModel(XingHuoChatModel.valueOfModel(model)).setTemperature(temperatureF)
                        .setMaxTokens(maxTokens);
            case QIAN_WEN:
                return TongYiChatOptions.builder().withModel(model).withTemperature(temperature).withMaxTokens(maxTokens).build();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

}
