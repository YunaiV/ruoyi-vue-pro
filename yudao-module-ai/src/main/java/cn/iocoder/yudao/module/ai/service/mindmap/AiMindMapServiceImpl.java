package cn.iocoder.yudao.module.ai.service.mindmap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.mindmap.AiMindMapDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.dal.mysql.mindmap.AiMindMapMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnum;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.billing.AiBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiCallStatusEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiTokenSourceEnum;
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
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 思维导图 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiMindMapServiceImpl implements AiMindMapService {

    @Resource
    private AiModelService modalService;
    @Resource
    private AiChatRoleService chatRoleService;

    @Resource
    private AiMindMapMapper mindMapMapper;

    @Resource
    private AiModelCallLogService callLogService;

    @Resource
    private AiBudgetChecker budgetChecker;

    @Resource
    private AiModelPricingService modelPricingService;

    @Override
    public Flux<CommonResult<String>> generateMindMap(AiMindMapGenerateReqVO generateReqVO, Long userId) {
        // 1. 获取导图模型。尝试获取思维导图助手角色，如果没有则使用默认模型
        AiChatRoleDO role = CollUtil.getFirst(
                chatRoleService.getChatRoleListByName(AiChatRoleEnum.AI_MIND_MAP_ROLE.getName()));
        // 1.1 获取导图执行模型
        AiModelDO model = getModel(role);
        // 1.2 获取角色设定消息
        String systemMessage = role != null && StrUtil.isNotBlank(role.getSystemMessage())
                ? role.getSystemMessage() : AiChatRoleEnum.AI_MIND_MAP_ROLE.getSystemMessage();
        // 1.3 校验平台
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatModel chatModel = modalService.getChatModel(model.getId());

        // 2. 预算预扣费（必须在落库之前，避免超限时留下脏数据）
        Long tenantId = TenantContextHolder.getTenantId();
        AiBudgetChecker.PreDeductResult preDeductResult = budgetChecker.preDeduct(
                tenantId, userId, estimateCost(model, systemMessage, generateReqVO.getPrompt()));
        try {
            // 3. 插入思维导图信息
            AiMindMapDO mindMapDO = BeanUtils.toBean(generateReqVO, AiMindMapDO.class, mindMap -> mindMap.setUserId(userId)
                    .setPlatform(platform.getPlatform()).setModelId(model.getId()).setModel(model.getModel()));
            mindMapMapper.insert(mindMapDO);

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
                    mindMapMapper.updateById(new AiMindMapDO().setId(mindMapDO.getId()).setGeneratedContent(contentBuffer.toString()));
                    // 记录调用日志 + 预算结算
                    createCallLog(model, userId, mindMapDO.getId(), AiBizTypeEnum.MIND_MAP.getType(),
                            requestTime, lastChunkRef.get(), AiCallStatusEnum.SUCCESS.getStatus(), null,
                            preDeductResult, contentBuffer.length() > 0);
                });
            }).doOnError(throwable -> {
                log.error("[generateMindMap][generateReqVO({}) 发生异常]", generateReqVO, throwable);
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> {
                    boolean budgetFinalized = false;
                    String errorMessage = throwable.getMessage();
                    try {
                        try {
                            mindMapMapper.updateById(new AiMindMapDO().setId(mindMapDO.getId()).setErrorMessage(errorMessage));
                        } catch (Exception updateEx) {
                            log.error("[generateMindMap][mindMapId({}) 更新失败信息失败]", mindMapDO.getId(), updateEx);
                            if (StrUtil.isNotBlank(updateEx.getMessage())) {
                                errorMessage = StrUtil.blankToDefault(errorMessage, "")
                                        + "; persist_error=" + updateEx.getMessage();
                            }
                        }
                        // 记录调用日志（失败）
                        createCallLog(model, userId, mindMapDO.getId(), AiBizTypeEnum.MIND_MAP.getType(),
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
                log.info("[generateMindMap][generateReqVO({}) 取消请求]", generateReqVO);
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> createCallLog(model, userId, mindMapDO.getId(),
                        AiBizTypeEnum.MIND_MAP.getType(), requestTime, lastChunkRef.get(),
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

    private Prompt buildPrompt(AiMindMapGenerateReqVO generateReqVO, AiModelDO model, String systemMessage) {
        // 1. 构建 message 列表
        List<Message> chatMessages = buildMessages(generateReqVO, systemMessage);
        // 2. 构建 options 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        return new Prompt(chatMessages, options);
    }

    private static List<Message> buildMessages(AiMindMapGenerateReqVO generateReqVO, String systemMessage) {
        List<Message> chatMessages = new ArrayList<>();
        // 1. 角色设定
        if (StrUtil.isNotBlank(systemMessage)) {
            chatMessages.add(new SystemMessage(systemMessage));
        }
        // 2. 用户输入
        chatMessages.add(new UserMessage(generateReqVO.getPrompt()));
        return chatMessages;
    }

    private AiModelDO getModel(AiChatRoleDO role) {
        AiModelDO model = null;
        if (role != null && role.getModelId() != null) {
            model = modalService.getModel(role.getModelId());
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

    @Override
    public void deleteMindMap(Long id) {
        // 校验存在
        validateMindMapExists(id);
        // 删除
        mindMapMapper.deleteById(id);
    }

    private void validateMindMapExists(Long id) {
        if (mindMapMapper.selectById(id) == null) {
            throw exception(MIND_MAP_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<AiMindMapDO> getMindMapPage(AiMindMapPageReqVO pageReqVO) {
        return mindMapMapper.selectPage(pageReqVO);
    }

}
