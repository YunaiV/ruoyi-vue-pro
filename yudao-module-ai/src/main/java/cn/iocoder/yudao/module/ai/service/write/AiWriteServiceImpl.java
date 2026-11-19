package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWritePageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.DictTypeConstants;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogService;
import cn.iocoder.yudao.module.ai.service.requestlog.dto.AiRequestLogCreateReqDTO;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
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
    private AiRequestLogService requestLogService;

    @Resource
    private AiWriteMapper writeMapper;

    @Override
    public Flux<CommonResult<AiWriteGenerateRespVO>> generateWriteContent(AiWriteGenerateReqVO generateReqVO,
                                                                          Long userId) {
        // 1. 校验模型：校验失败返回错误帧，由前端提示原因
        AiModelDO model;
        try {
            model = validateChatModel(generateReqVO.getModelId());
        } catch (ServiceException ex) {
            return Flux.just(error(ex.getCode(), ex.getMessage()));
        }
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        StreamingChatModel chatModel = modalService.getChatModel(model.getId());

        // 2.1 插入写作信息
        String requestId = IdUtil.fastUUID();
        AiWriteDO writeDO = buildWrite(generateReqVO);
        writeDO.setUserId(userId).setPlatform(platform.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                .setRequestId(requestId);
        writeMapper.insert(writeDO);
        // 2.2 构建请求日志
        AiRequestLogCreateReqDTO requestLog = requestLogService.buildCreateReqDTO(requestId, userId,
                AiRequestLogBizTypeEnum.WRITE, writeDO.getId(), model);
        AtomicBoolean finished = new AtomicBoolean(false);

        // 3.1 构建 Prompt，并进行调用：系统提示词由写作场景内置
        Flux<ChatResponse> streamResponse;
        try {
            Prompt prompt = buildPrompt(writeDO, model);
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
            // 不打印请求对象：原文可能很长
            log.error("[generateWriteContent][writeId({}) requestId({}) 发生异常]", writeDO.getId(), requestId, throwable);
            finishWrite(requestLog, finished, writeDO.getId(), null, AiRequestLogStatusEnum.FAIL, throwable);
        }).doOnCancel(() -> {
            log.info("[generateWriteContent][requestId({}) 取消请求]", requestId);
            finishWrite(requestLog, finished, writeDO.getId(), null, AiRequestLogStatusEnum.CANCEL, null);
        }).onErrorResume(error -> Flux.just(error(WRITE_GENERATE_FAIL,
                StrUtil.blankToDefault(error.getMessage(), error.getClass().getName()))));
    }

    /**
     * 校验模型：存在、启用，并且是对话模型
     *
     * @param modelId 模型编号
     * @return 模型
     */
    private AiModelDO validateChatModel(Long modelId) {
        AiModelDO model = modalService.validateModel(modelId);
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.CHAT.getType())) {
            throw exception(MODEL_USE_TYPE_ERROR);
        }
        return model;
    }

    /**
     * 构建写作记录：只保留本场景使用的字段，其余置为 null
     *
     * @param reqVO 请求（已按场景校验）
     * @return 写作记录
     */
    static AiWriteDO buildWrite(AiWriteGenerateReqVO reqVO) {
        AiWriteDO write = new AiWriteDO().setType(reqVO.getType()).setPrompt(StrUtil.nullToEmpty(reqVO.getPrompt()))
                .setOriginalContent(reqVO.getOriginalContent()).setLength(reqVO.getLength()).setFormat(reqVO.getFormat())
                .setTone(reqVO.getTone()).setLanguage(reqVO.getLanguage());
        AiWriteGenerateReqVO.SceneParams params = reqVO.getSceneParams();
        switch (AiWriteTypeEnum.valueOfType(reqVO.getType())) {
            case WRITING, GENERAL -> write.setOriginalContent(null);
            case REPLY -> {
            }
            case WEEKLY_REPORT -> write.setFormat(null).setSceneParams(params == null ? null
                    : new AiWriteDO.SceneParams().setPeriod(params.getPeriod()).setRole(params.getRole()));
            case XIAOHONGSHU -> write.setOriginalContent(null).setFormat(null).setTone(null)
                    .setSceneParams(new AiWriteDO.SceneParams().setAudience(params.getAudience()).setStyle(params.getStyle())
                            .setEmoji(params.getEmoji()).setTagCount(params.getTagCount()));
            // 润色 / 改写保持原文语言，翻译使用目标语言：都不使用格式、输出语言、长度
            case REWRITE -> write.setFormat(null).setLanguage(null).setLength(null)
                    .setSceneParams(new AiWriteDO.SceneParams().setMode(params.getMode()));
            case TRANSLATE -> write.setFormat(null).setLanguage(null).setLength(null)
                    .setSceneParams(new AiWriteDO.SceneParams().setSourceLanguage(params.getSourceLanguage())
                            .setTargetLanguage(params.getTargetLanguage()).setDomain(params.getDomain())
                            .setPreserveFormat(params.getPreserveFormat()));
        }
        return write;
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
                    .setErrorMessage(requestLog.getErrorMessage())
                    .setInputTokens(requestLog.getInputTokens()).setOutputTokens(requestLog.getOutputTokens()).setTotalTokens(requestLog.getTotalTokens());
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

    private Prompt buildPrompt(AiWriteDO write, AiModelDO model) {
        // 1. 构建 message 列表：场景内置的系统提示词 + 按模板拼接的用户消息
        AiWriteTypeEnum type = AiWriteTypeEnum.valueOfType(write.getType());
        List<Message> chatMessages = new ArrayList<>();
        chatMessages.add(new SystemMessage(type.getSystemMessage()));
        chatMessages.add(new UserMessage(buildUserMessage(type, write)));
        // 2. 构建 options 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiChatUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        return new Prompt(chatMessages, options);
    }

    static String buildUserMessage(AiWriteTypeEnum type, AiWriteDO write) {
        String prompt = StrUtil.blankToDefault(write.getPrompt(), "无");
        String original = write.getOriginalContent();
        String tone = getDictLabel(DictTypeConstants.AI_WRITE_TONE, write.getTone());
        String language = getDictLabel(DictTypeConstants.AI_WRITE_LANGUAGE, write.getLanguage());
        String length = getDictLabel(DictTypeConstants.AI_WRITE_LENGTH, write.getLength());
        AiWriteDO.SceneParams params = ObjUtil.defaultIfNull(write.getSceneParams(), new AiWriteDO.SceneParams());
        return switch (type) {
            case WRITING, GENERAL -> StrUtil.format(type.getPrompt(), prompt,
                    getDictLabel(DictTypeConstants.AI_WRITE_FORMAT, write.getFormat()), tone, language, length);
            case REPLY -> StrUtil.format(type.getPrompt(), original, prompt,
                    getDictLabel(DictTypeConstants.AI_WRITE_FORMAT, write.getFormat()), tone, language, length);
            case WEEKLY_REPORT -> StrUtil.format(type.getPrompt(), StrUtil.blankToDefault(params.getPeriod(), "无"),
                    StrUtil.blankToDefault(params.getRole(), "无"), original, prompt, tone, language, length);
            case XIAOHONGSHU -> StrUtil.format(type.getPrompt(), prompt, params.getAudience(), params.getStyle(),
                    BooleanUtil.toString(params.getEmoji(), "是", "否"), params.getTagCount(), language, length);
            case REWRITE -> StrUtil.format(type.getPrompt(), params.getMode(), tone, prompt, original);
            case TRANSLATE -> StrUtil.format(type.getPrompt(),
                    getDictLabel(DictTypeConstants.AI_WRITE_LANGUAGE, params.getSourceLanguage()),
                    getDictLabel(DictTypeConstants.AI_WRITE_LANGUAGE, params.getTargetLanguage()),
                    params.getDomain(), tone, BooleanUtil.toString(params.getPreserveFormat(), "是", "否"), prompt, original);
        };
    }

    private static String getDictLabel(String dictType, Integer value) {
        return DictFrameworkUtils.parseDictDataLabel(dictType, value);
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
