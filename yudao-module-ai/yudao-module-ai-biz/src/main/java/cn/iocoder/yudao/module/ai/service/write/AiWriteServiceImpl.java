package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.util.AiUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRolePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.DictTypeConstants;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
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
    private AiChatRoleService chatRoleService;

    @Resource
    private DictDataApi dictDataApi;

    @Resource
    private AiWriteMapper writeMapper;

    @Override
    public Flux<CommonResult<String>> generateWriteContent(AiWriteGenerateReqVO generateReqVO, Long userId) {
        // 1.1 获取写作模型 尝试获取写作助手角色，如果没有则使用默认模型
        AiChatRoleDO writeRole = selectOneWriteRole();
        AiChatModelDO model;
        // TODO @xin：writeRole.getModelId 可能为空。所以，最好是先通过 chatRole 拿。如果它没拿到，通过 getRequiredDefaultChatModel 再拿。
        if (Objects.nonNull(writeRole)) {
            model = chatModalService.getChatModel(writeRole.getModelId());
        } else {
            model = chatModalService.getRequiredDefaultChatModel();
        }
        // 1.2 校验平台
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        StreamingChatModel chatModel = apiKeyService.getChatModel(model.getKeyId());

        // 2. 插入写作信息
        AiWriteDO writeDO = BeanUtils.toBean(generateReqVO, AiWriteDO.class, e -> e.setUserId(userId).setModel(model.getModel()).setPlatform(platform.getPlatform()));
        writeMapper.insert(writeDO);

        // 3.1 构建提示词
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        Prompt prompt = new Prompt(buildWritingPrompt(generateReqVO), chatOptions);
        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

        // 3.2 流式返回
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

    // TODO @xin：chatRoleService 增加一个 getChatRoleListByName；
    private AiChatRoleDO selectOneWriteRole() {
        AiChatRoleDO chatRoleDO = null;
        // TODO @xin："写作助手" 枚举下。
        PageResult<AiChatRoleDO> writeRolePage = chatRoleService.getChatRolePage(new AiChatRolePageReqVO().setName("写作助手"));
        List<AiChatRoleDO> list = writeRolePage.getList();
        // TODO @xin：CollUtil.getFirst 简化下
        if (CollUtil.isNotEmpty(list)) {
            chatRoleDO = list.get(0);
        }
        return chatRoleDO;
    }

    private String buildWritingPrompt(AiWriteGenerateReqVO generateReqVO) {
        // 校验写作类型是否合法
        Integer type = generateReqVO.getType();
        // TODO @xin：这里可以搞到 validator 的校验。InEnum
        AiWriteTypeEnum.validateType(type);
        String format = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_FORMAT, generateReqVO.getFormat());
        String tone = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_TONE, generateReqVO.getTone());
        String language = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_LANGUAGE, generateReqVO.getLanguage());
        String length = dictDataApi.getDictDataLabel(DictTypeConstants.AI_WRITE_LENGTH, generateReqVO.getLength());
        String prompt = generateReqVO.getPrompt();
        if (Objects.equals(type, AiWriteTypeEnum.WRITING.getType())) {
            return StrUtil.format(AiWriteTypeEnum.WRITING.getPrompt(), prompt, format, tone, language, length);
        } else {
            return StrUtil.format(AiWriteTypeEnum.REPLY.getPrompt(), generateReqVO.getOriginalContent(), prompt, format, tone, language, length);
        }
    }

}
