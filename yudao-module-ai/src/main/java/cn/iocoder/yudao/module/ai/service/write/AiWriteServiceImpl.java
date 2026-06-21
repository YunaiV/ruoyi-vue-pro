package cn.iocoder.yudao.module.ai.service.write;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWritePageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnum;
import cn.iocoder.yudao.module.ai.enums.DictTypeConstants;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.billing.AiBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiCallStatusEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiTokenSourceEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.billing.AiBudgetChecker;
import cn.iocoder.yudao.module.ai.service.billing.AiModelCallLogService;
import cn.iocoder.yudao.module.ai.service.billing.AiModelPricingService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
    private AiWriteMapper writeMapper;

    @Resource
    private AiModelCallLogService callLogService;

    @Resource
    private AiBudgetChecker budgetChecker;

    @Resource
    private AiModelPricingService modelPricingService;

    @Override
    public Flux<CommonResult<String>> generateWriteContent(AiWriteGenerateReqVO generateReqVO, Long userId) {
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
        String userMessageForEstimate = buildUserMessage(generateReqVO);

        // 2. 预算预扣费（必须在落库之前，避免超限时留下脏数据）
        Long tenantId = TenantContextHolder.getTenantId();
        AiBudgetChecker.PreDeductResult preDeductResult = budgetChecker.preDeduct(
                tenantId, userId, estimateCost(model, systemMessage, userMessageForEstimate));
        try {
            // 3. 插入写作信息
            AiWriteDO writeDO = BeanUtils.toBean(generateReqVO, AiWriteDO.class, write -> write.setUserId(userId)
                    .setPlatform(platform.getPlatform()).setModelId(model.getId()).setModel(model.getModel()));
            writeMapper.insert(writeDO);

            // 4.1 构建 Prompt，并进行调用
            Prompt prompt = buildPrompt(generateReqVO, model, systemMessage);
            LocalDateTime requestTime = LocalDateTime.now();
            Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

            // 3.3 流式返回
            StringBuffer contentBuffer = new StringBuffer();
            AtomicReference<ChatResponse> lastChunkRef = new AtomicReference<>();
            return streamResponse.map(chunk -> {
                lastChunkRef.set(chunk);
                String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getText() : null;
                newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
                contentBuffer.append(newContent);
                // 响应结果
                return success(newContent);
            }).doOnComplete(() -> {
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> {
                    writeMapper.updateById(new AiWriteDO().setId(writeDO.getId()).setGeneratedContent(contentBuffer.toString()));
                    // 记录调用日志 + 预算结算
                    createCallLog(model, userId, writeDO.getId(), AiBizTypeEnum.WRITE.getType(),
                            requestTime, lastChunkRef.get(), AiCallStatusEnum.SUCCESS.getStatus(), null,
                            preDeductResult, contentBuffer.length() > 0);
                });
            }).doOnError(throwable -> {
                log.error("[generateWriteContent][generateReqVO({}) 发生异常]", generateReqVO, throwable);
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> {
                    boolean budgetFinalized = false;
                    String errorMessage = throwable.getMessage();
                    try {
                        try {
                            writeMapper.updateById(new AiWriteDO().setId(writeDO.getId()).setErrorMessage(errorMessage));
                        } catch (Exception updateEx) {
                            log.error("[generateWriteContent][writeId({}) 更新失败信息失败]", writeDO.getId(), updateEx);
                            if (StrUtil.isNotBlank(updateEx.getMessage())) {
                                errorMessage = StrUtil.blankToDefault(errorMessage, "")
                                        + "; persist_error=" + updateEx.getMessage();
                            }
                        }
                        // 记录调用日志（失败）
                        createCallLog(model, userId, writeDO.getId(), AiBizTypeEnum.WRITE.getType(),
                                requestTime, lastChunkRef.get(), AiCallStatusEnum.FAIL.getStatus(), errorMessage,
                                preDeductResult, contentBuffer.length() > 0);
                        budgetFinalized = true;
                    } finally {
                        if (!budgetFinalized) {
                            budgetChecker.release(preDeductResult);
                        }
                    }
                });
            }).doOnCancel(() -> {
                log.info("[generateWriteContent][generateReqVO({}) 取消请求]", generateReqVO);
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> createCallLog(model, userId, writeDO.getId(),
                        AiBizTypeEnum.WRITE.getType(), requestTime, lastChunkRef.get(),
                        AiCallStatusEnum.CANCEL.getStatus(), null, preDeductResult,
                        contentBuffer.length() > 0));
            }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.WRITE_STREAM_ERROR)));
        } catch (Exception e) {
            // preDeduct 之后、Flux 回调挂钩之前发生异常时，需要主动释放预扣
            TenantUtils.execute(tenantId, () -> budgetChecker.release(preDeductResult));
            throw e;
        }
    }

    private void createCallLog(AiModelDO model, Long userId, Long bizId, String bizType,
                               LocalDateTime requestTime, ChatResponse chatResponse,
                               String status, String errorMessage,
                               AiBudgetChecker.PreDeductResult preDeductResult,
                               boolean hasOutputContent) {
        AiModelCallLogDO callLog = null;
        try {
            LocalDateTime responseTime = LocalDateTime.now();
            Integer promptTokens = null;
            Integer completionTokens = null;
            Integer totalTokens = null;
            Long costAmount = null;
            String tokenSource = AiTokenSourceEnum.NONE.getSource();
            if (chatResponse != null && chatResponse.getMetadata() != null
                    && chatResponse.getMetadata().getUsage() != null) {
                Usage usage = chatResponse.getMetadata().getUsage();
                promptTokens = usage.getPromptTokens();
                completionTokens = usage.getCompletionTokens();
                totalTokens = usage.getTotalTokens();
                tokenSource = AiTokenSourceEnum.PROVIDER.getSource();
            }
            boolean shouldUseEstimated = preDeductResult != null
                    && !AiTokenSourceEnum.PROVIDER.getSource().equals(tokenSource)
                    && (AiCallStatusEnum.SUCCESS.getStatus().equals(status)
                    || ((AiCallStatusEnum.FAIL.getStatus().equals(status)
                    || AiCallStatusEnum.CANCEL.getStatus().equals(status)) && hasOutputContent));
            if (shouldUseEstimated) {
                // 说明：主流模型在流式最后一个 chunk 通常会返回 usage。
                // 若极少数场景缺失 usage，这里会回落到预估费用，可能偏高。
                tokenSource = AiTokenSourceEnum.ESTIMATED.getSource();
                costAmount = preDeductResult.amount();
            }
            if (errorMessage != null && errorMessage.length() > 1024) {
                errorMessage = errorMessage.substring(0, 1024);
            }
            callLog = AiModelCallLogDO.builder()
                    .userId(userId)
                    .platform(model.getPlatform())
                    .modelId(model.getId())
                    .model(model.getModel())
                    .apiKeyId(model.getKeyId())
                    .bizType(bizType)
                    .bizId(bizId)
                    .requestTime(requestTime)
                    .responseTime(responseTime)
                    .durationMs((int) Duration.between(requestTime, responseTime).toMillis())
                    .status(status)
                    .errorMessage(errorMessage)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .tokenSource(tokenSource)
                    .costAmount(costAmount)
                    .build();
            callLogService.createCallLog(callLog);
        } catch (Exception e) {
            // 调用日志记录失败不应影响主流程和预算结算
            log.error("[createCallLog][userId({}) bizId({}) 记录调用日志失败]", userId, bizId, e);
        } finally {
            // 预算结算：无论日志写入是否成功，都必须结算/释放预扣费，避免 Redis 预扣金额悬挂
            if (preDeductResult != null) {
                try {
                    long actualCost = callLog != null && callLog.getCostAmount() != null ? callLog.getCostAmount() : 0L;
                    budgetChecker.settle(preDeductResult, actualCost);
                } catch (Exception e) {
                    // settle 内部已兜底；此处不再 release，避免反向冲销导致预算负值
                    log.error("[createCallLog][userId({}) bizId({}) 预算结算失败]", userId, bizId, e);
                }
            }
        }
    }

    private long estimateCost(AiModelDO model, String... promptSegments) {
        cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO pricing =
                modelPricingService.getLatestModelPricing(model.getId());
        if (pricing == null) {
            return 0L;
        }
        int maxTokens = model.getMaxTokens() != null ? model.getMaxTokens() : 4096;
        int estimatedPromptTokens = 0;
        if (promptSegments != null) {
            for (String promptSegment : promptSegments) {
                if (StrUtil.isNotBlank(promptSegment)) {
                    estimatedPromptTokens += promptSegment.length();
                }
            }
        }
        estimatedPromptTokens = Math.max(estimatedPromptTokens, maxTokens);
        long priceIn = pricing.getPriceInPer1m() != null ? pricing.getPriceInPer1m() : 0L;
        long priceOut = pricing.getPriceOutPer1m() != null ? pricing.getPriceOutPer1m() : 0L;
        return Math.round((double) estimatedPromptTokens * priceIn / 1_000_000
                + (double) maxTokens * priceOut / 1_000_000);
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
        ChatOptions options = AiUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
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
    public PageResult<AiWriteDO> getWritePage(AiWritePageReqVO pageReqVO) {
        return writeMapper.selectPage(pageReqVO);
    }

}
