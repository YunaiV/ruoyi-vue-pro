package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWritePageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnum;
import cn.iocoder.yudao.module.ai.enums.DictTypeConstants;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogService;
import cn.iocoder.yudao.module.ai.service.requestlog.dto.AiRequestLogCreateReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 写作 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiWriteServiceImpl implements AiWriteService {

    @Resource
    private AiModelService modalService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiRequestLogService requestLogService;

    @Resource
    private AiWriteMapper writeMapper;

    @Override
    public Flux<CommonResult<AiWriteGenerateRespVO>> generateWriteContent(AiWriteGenerateReqVO generateReqVO,
                                                                          Long userId) {
        // 1 获取写作模型。尝试获取写作助手角色，没有则使用默认模型
        AiChatRoleDO writeRole = CollUtil.getFirst(
                chatRoleService.getChatRoleListByName(AiChatRoleEnum.AI_WRITE_ROLE.getName()));
        // 1.1 获取写作执行模型
        AiModelDO model = getModel(writeRole);
        // 1.2 获取角色设定消息
        String systemMessage = Objects.nonNull(writeRole) && StrUtil.isNotBlank(writeRole.getSystemMessage())
                ? writeRole.getSystemMessage() : AiChatRoleEnum.AI_WRITE_ROLE.getSystemMessage();
        // 1.3 校验平台
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        StreamingChatModel chatModel = modalService.getChatModel(model.getId());

        // 2.1 插入写作信息
        String requestId = IdUtil.fastUUID();
        AiWriteDO writeDO = BeanUtils.toBean(generateReqVO, AiWriteDO.class, write -> write.setUserId(userId)
                        .setPlatform(platform.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                        .setRequestId(requestId));
        writeMapper.insert(writeDO);
        // 2.2 构建请求日志
        AiRequestLogCreateReqDTO requestLog = requestLogService.buildCreateReqDTO(requestId, userId,
                AiRequestLogBizTypeEnum.WRITE, writeDO.getId(), model);
        AtomicBoolean finished = new AtomicBoolean(false);

        // 3.1 构建 Prompt，并进行调用
        Flux<ChatResponse> streamResponse;
        try {
            Prompt prompt = buildPrompt(generateReqVO, model, systemMessage);
            streamResponse = chatModel.stream(prompt);
        } catch (RuntimeException ex) {
            finishWrite(requestLog, finished, writeDO.getId(), null, AiRequestLogStatusEnum.FAIL, ex);
            throw ex;
        }

        // 3.2 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getText() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果：用量为请求日志的最新快照，与取消并发时保持一致
            synchronized (requestLog) {
                requestLogService.setCreateReqDTO(requestLog, chunk);
                return success(new AiWriteGenerateRespVO().setId(writeDO.getId()).setContent(newContent)
                        .setInputTokens(requestLog.getInputTokens()).setOutputTokens(requestLog.getOutputTokens()).setTotalTokens(requestLog.getTotalTokens()));
            }
        }).doOnComplete(() -> finishWrite(requestLog, finished, writeDO.getId(), contentBuffer.toString(),
                AiRequestLogStatusEnum.SUCCESS, null)
        ).doOnError(throwable -> {
            log.error("[generateWriteContent][generateReqVO({}) 发生异常]", generateReqVO, throwable);
            finishWrite(requestLog, finished, writeDO.getId(), null, AiRequestLogStatusEnum.FAIL, throwable);
        }).doOnCancel(() -> {
            log.info("[generateWriteContent][requestId({}) 取消请求]", requestId);
            finishWrite(requestLog, finished, writeDO.getId(), null, AiRequestLogStatusEnum.CANCEL, null);
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.WRITE_STREAM_ERROR)));
    }

    /**
     * 结束写作：记录请求日志，并更新写作结果与用量
     *
     * 同一请求仅第一次调用生效；两次写入相互独立，失败只记录日志，不影响已返回的生成内容
     */
    private void finishWrite(AiRequestLogCreateReqDTO requestLog, AtomicBoolean finished, Long writeId,
                             String generatedContent, AiRequestLogStatusEnum status, Throwable error) {
        // 1. 记录请求日志：与 chunk 的用量更新互斥，保证写作与日志的用量一致
        AiWriteDO updateObj;
        synchronized (requestLog) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            requestLog.setStatus(status.getStatus()).setErrorMessage(error != null
                    ? StrUtil.blankToDefault(error.getMessage(), error.getClass().getName()) : null);
            try {
                requestLogService.createRequestLog(requestLog);
            } catch (Exception ex) {
                log.error("[finishWrite][requestId({}) 记录请求日志失败]", requestLog.getRequestId(), ex);
            }
            updateObj = new AiWriteDO().setId(writeId).setGeneratedContent(generatedContent)
                    .setErrorMessage(error != null ? error.getMessage() : null)
                    .setInputTokens(requestLog.getInputTokens()).setOutputTokens(requestLog.getOutputTokens()).setTotalTokens(requestLog.getTotalTokens()); // DONE @AI：setTotalTokens(requestLog.getTotalTokens()) 挪到上面一行，虽然更长了，但是更简洁；其它类似也是；已挪，对话、思维导图同样处理
            updateObj.setUpdater(StrUtil.toStringOrNull(requestLog.getUserId()));
        }

        // 2. 更新写作结果与用量：Flux 异步回调中没有租户上下文，按请求开始时的租户执行
        TenantUtils.execute(requestLog.getTenantId(), () -> {
            try {
                writeMapper.updateById(updateObj);
            } catch (Exception ex) {
                log.error("[finishWrite][writeId({}) requestId({}) 更新写作结果失败]", writeId, requestLog.getRequestId(), ex);
            }
        });
    }

    private AiModelDO getModel(AiChatRoleDO writeRole) {
        AiModelDO model = null;
        if (Objects.nonNull(writeRole) && Objects.nonNull(writeRole.getModelId())) {
            model = modalService.getModel(writeRole.getModelId());
        }
        if (model == null) {
            model = modalService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType());
        }
        // 校验模型存在、且合法
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.CHAT.getType())) {
            throw exception(MODEL_USE_TYPE_ERROR);
        }
        return model;
    }

    private Prompt buildPrompt(AiWriteGenerateReqVO generateReqVO, AiModelDO model, String systemMessage) {
        // 1. 构建 message 列表
        List<Message> chatMessages = buildMessages(generateReqVO, systemMessage);
        // 2. 构建 options 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiChatUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        return new Prompt(chatMessages, options);
    }

    private List<Message> buildMessages(AiWriteGenerateReqVO generateReqVO, String systemMessage) {
        List<Message> chatMessages = new ArrayList<>();
        if (StrUtil.isNotBlank(systemMessage)) {
            // 1.1 角色设定
            chatMessages.add(new SystemMessage(systemMessage));
        }
        // 1.2 用户输入
        chatMessages.add(new UserMessage(buildUserMessage(generateReqVO)));
        return chatMessages;
    }

    private String buildUserMessage(AiWriteGenerateReqVO generateReqVO) {
        String format = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.AI_WRITE_FORMAT, generateReqVO.getFormat());
        String tone = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.AI_WRITE_TONE, generateReqVO.getTone());
        String language = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.AI_WRITE_LANGUAGE, generateReqVO.getLanguage());
        String length = DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.AI_WRITE_LENGTH, generateReqVO.getLength());
        // 格式化 prompt
        String prompt = generateReqVO.getPrompt();
        if (Objects.equals(generateReqVO.getType(), AiWriteTypeEnum.WRITING.getType())) {
            return StrUtil.format(AiWriteTypeEnum.WRITING.getPrompt(), prompt, format, tone, language, length);
        } else {
            return StrUtil.format(AiWriteTypeEnum.REPLY.getPrompt(), generateReqVO.getOriginalContent(), prompt, format, tone, language, length);
        }
    }

    @Override
    public void deleteWrite(Long id) {
        // 校验存在
        validateWriteExists(id);
        // 删除
        writeMapper.deleteById(id);
    }

    private void validateWriteExists(Long id) {
        if (writeMapper.selectById(id) == null) {
            throw exception(WRITE_NOT_EXISTS);
        }
    }

    @Override
    public AiWriteDO getWrite(Long id) {
        return writeMapper.selectById(id);
    }

    @Override
    public PageResult<AiWriteDO> getWritePage(AiWritePageReqVO pageReqVO) {
        return writeMapper.selectPage(pageReqVO);
    }

}
