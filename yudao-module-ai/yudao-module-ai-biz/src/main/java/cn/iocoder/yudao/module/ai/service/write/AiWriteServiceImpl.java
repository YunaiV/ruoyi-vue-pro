package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.util.AiUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.DictTypeConstants;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
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
    private DictDataApi dictDataApi;

    @Resource
    private AiWriteMapper writeMapper;

    @Override
    public Flux<CommonResult<String>> generateWriteContent(AiWriteGenerateReqVO generateReqVO, Long userId) {
        // 1.1 校验模型 TODO 芋艿 是不是取默认的模型也ok？；那可以，有限拿 chatRole 的角色；如果没有，则获取默认的；
        AiChatModelDO model = chatModalService.getRequiredDefaultChatModel();
        StreamingChatModel chatModel = apiKeyService.getChatModel(model.getKeyId());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());

        // 1.2 插入写作信息
        // TODO @xin：建议把 writeDO.setUserId(userId).setModel(model.getModel()).setPlatform(platform.getPlatform())，写在 toBean 的 consumer 里；原因是，让这个 set 保持完整性
        AiWriteDO writeDO = BeanUtils.toBean(generateReqVO, AiWriteDO.class);
        writeMapper.insert(writeDO.setUserId(userId).setModel(model.getModel()).setPlatform(platform.getPlatform()));

        // 2.1 构建提示词
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        Prompt prompt = new Prompt(buildWritingPrompt(generateReqVO), chatOptions);
        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

        // 2.2 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getContent() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果
            return success(newContent);
        }).doOnComplete(() -> {
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() ->
                    writeMapper.updateById(new AiWriteDO().setId(writeDO.getId()).setGeneratedContent(contentBuffer.toString())));
        }).doOnError(throwable -> {
            log.error("[generateWriteContent][generateReqVO({}) 发生异常]", generateReqVO, throwable);
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() ->
                    writeMapper.updateById(new AiWriteDO().setId(writeDO.getId()).setErrorMessage(throwable.getMessage())));
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.WRITE_STREAM_ERROR)));
    }

    private String buildWritingPrompt(AiWriteGenerateReqVO generateReqVO) {
        String template;
        Integer writeType = generateReqVO.getType();
        String format = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_FORMAT, generateReqVO.getFormat());
        String tone = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_TONE, generateReqVO.getFormat());
        String language = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_LANGUAGE, generateReqVO.getFormat());
        String length = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_LENGTH, generateReqVO.getFormat());
        // TODO @xin：建议改成 if return 哈；更简洁；
        if (Objects.equals(writeType, AiWriteTypeEnum.WRITING.getType())) {
            // TODO @xin：写成静态枚举哈
            template = "请撰写一篇关于 [{}] 的文章。文章的内容格式为：[{}]，语气为：[{}]，语言为：[{}]，长度为：[{}]。请确保涵盖主要内容，不需要除了正文内容外的其他回复，如标题、额外的解释或道歉。";
            return StrUtil.format(template, generateReqVO.getPrompt(), format, tone, language, length);
        } else if (Objects.equals(writeType, AiWriteTypeEnum.REPLY.getType())) {
            template = "请针对如下内容：[{}] 做个回复。回复内容参考：[{}], 回复的内容格式为：[{}]，语气为：[{}]，语言为：[{}]，长度为：[{}]。不需要除了正文内容外的其他回复，如标题、额外的解释或道歉。";
            return StrUtil.format(template, generateReqVO.getOriginalContent(), generateReqVO.getPrompt(), format, tone, language, length);
        } else {
            throw new IllegalArgumentException(StrUtil.format("未知写作类型({})", writeType));
        }
    }

}
