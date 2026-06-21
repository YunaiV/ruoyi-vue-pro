package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessagePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiToolDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.billing.AiBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiCallStatusEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiTokenSourceEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import cn.iocoder.yudao.module.ai.service.billing.AiBudgetChecker;
import cn.iocoder.yudao.module.ai.service.billing.AiModelCallLogService;
import cn.iocoder.yudao.module.ai.service.billing.AiModelPricingService;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeDocumentService;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeSegmentService;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchReqBO;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchRespBO;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.model.AiToolService;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import cn.iocoder.yudao.module.ai.util.FileTypeUtils;
import com.google.common.collect.Maps;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_MESSAGE_NOT_EXIST;

/**
 * AI 聊天消息 Service 实现类
 *
 * @author fansili
 */
@Service
@Slf4j
public class AiChatMessageServiceImpl implements AiChatMessageService {

    /**
     * 联网搜索的结束数
     */
    private static final Integer WEB_SEARCH_COUNT = 10;
    // TODO @芋艿：后续优化下对话的 Prompt 整体结构

    /**
     * 知识库转 {@link UserMessage} 的内容模版
     */
    private static final String KNOWLEDGE_USER_MESSAGE_TEMPLATE = "使用 <Reference></Reference> 标记中的内容作为本次对话的参考:\n\n" +
            "%s\n\n" + // 多个 <Reference></Reference> 的拼接
            "回答要求：\n- 避免提及你是从 <Reference></Reference> 获取的知识。";

    private static final String WEB_SEARCH_USER_MESSAGE_TEMPLATE = "使用 <WebSearch></WebSearch> 标记中的内容作为本次对话的参考:\n\n" +
            "%s\n\n" + // 多个 <WebSearch></WebSearch> 的拼接
            "回答要求：\n- 避免提及你是从 <WebSearch></WebSearch> 获取的知识。";

    /**
     * 附件转 ${@link UserMessage} 的内容模版
     */
    @SuppressWarnings("TextBlockMigration")
    private static final String Attachment_USER_MESSAGE_TEMPLATE = "使用 <Attachment></Attachment> 标记用户对话上传的附件内容:\n\n" +
            "%s\n\n" + // 多个 <Attachment></Attachment> 的拼接
            "回答要求：\n- 避免提及 <Attachment></Attachment> 附件的编码格式。";

    @Resource
    private AiChatMessageMapper chatMessageMapper;

    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiModelService modalService;
    @Resource
    private AiKnowledgeSegmentService knowledgeSegmentService;
    @Resource
    private AiKnowledgeDocumentService knowledgeDocumentService;
    @Resource
    private AiToolService toolService;

    @Resource
    private AiModelCallLogService callLogService;

    @Resource
    private AiBudgetChecker budgetChecker;

    @Resource
    private AiModelPricingService modelPricingService;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 yudao.ai.web-search.enable 配置项，可以关闭 AiWebSearchClient 的功能，所以这里只能不强制注入
    private AiWebSearchClient webSearchClient;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 yudao.ai.mcp.client.enable 配置项，可以关闭 McpSyncClient 的功能，所以这里只能不强制注入
    private List<McpSyncClient> mcpClients;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 yudao.ai.mcp.client.enable 配置项，可以关闭 McpSyncClient 的功能，所以这里只能不强制注入
    private McpClientCommonProperties mcpClientCommonProperties;

    @Resource
    private ToolCallbackResolver toolCallbackResolver;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageSendRespVO sendMessage(AiChatMessageSendReqVO sendReqVO, Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService
                .validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiModelDO model = modalService.validateModel(conversation.getModelId());
        ChatModel chatModel = modalService.getChatModel(model.getId());

        // 2.1 知识库召回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = recallKnowledgeSegment(
                sendReqVO.getContent(), conversation);

        // 2.2 联网搜索
        AiWebSearchResponse webSearchResponse = Boolean.TRUE.equals(sendReqVO.getUseSearch()) && webSearchClient != null ?
                webSearchClient.search(new AiWebSearchRequest().setQuery(sendReqVO.getContent())
                        .setSummary(true).setCount(WEB_SEARCH_COUNT)) : null;

        // 3. 构建 Prompt（用于模型调用 + 预算估算）
        Prompt prompt = buildPrompt(conversation, historyMessages, knowledgeSegments, webSearchResponse, model, sendReqVO);
        // 4. 预算预扣费（必须在落库之前，避免超限时留下脏数据）
        int estimatedPromptTokens = estimatePromptTokens(prompt);
        AiBudgetChecker.PreDeductResult preDeductResult = budgetChecker.preDeduct(
                TenantContextHolder.getTenantId(), userId, estimateCost(model, estimatedPromptTokens));
        boolean budgetFinalized = false;
        try {
            // 5.1 插入 user 发送消息
            AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                    userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext(),
                    null, sendReqVO.getAttachmentUrls(), null);

            // 5.2 插入 assistant 接收消息
            AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                    userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext(),
                    knowledgeSegments, null, webSearchResponse);

            // 5.3 调用模型，记录调用日志
            LocalDateTime requestTime = LocalDateTime.now();
            ChatResponse chatResponse;
            try {
                chatResponse = chatModel.call(prompt);
            } catch (Exception e) {
                // 释放预扣费
                budgetChecker.release(preDeductResult);
                budgetFinalized = true;
                // 失败时也记录调用日志
                createChatCallLog(model, userId, assistantMessage.getId(), conversation.getId(),
                        requestTime, null, AiCallStatusEnum.FAIL.getStatus(), e.getMessage(),
                        null, null, null, null, null, AiTokenSourceEnum.NONE.getSource(), null,
                        false);
                throw e;
            }
            // 提取 usage 并记录成功日志
            createChatCallLog(model, userId, assistantMessage.getId(), conversation.getId(),
                    requestTime, chatResponse, AiCallStatusEnum.SUCCESS.getStatus(), null,
                    null, null, null, null, null, null, preDeductResult, false);
            budgetFinalized = true;

            // 5.4 更新响应内容
            String newContent = AiUtils.getChatResponseContent(chatResponse);
            String newReasoningContent = AiUtils.getChatResponseReasoningContent(chatResponse);
            chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId())
                    .setContent(newContent).setReasoningContent(newReasoningContent));
            // 5.5 响应结果
            Map<Long, AiKnowledgeDocumentDO> documentMap = knowledgeDocumentService.getKnowledgeDocumentMap(
                    convertSet(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getDocumentId));
            List<AiChatMessageRespVO.KnowledgeSegment> segments = BeanUtils.toBean(knowledgeSegments,
                    AiChatMessageRespVO.KnowledgeSegment.class, segment -> {
                        AiKnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                        segment.setDocumentName(document != null ? document.getName() : null);
                    });
            return new AiChatMessageSendRespVO()
                    .setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                    .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class)
                            .setContent(newContent).setSegments(segments)
                            .setWebSearchPages(webSearchResponse != null ? webSearchResponse.getLists() : null));
        } catch (Exception e) {
            if (!budgetFinalized) {
                budgetChecker.release(preDeductResult);
            }
            throw e;
        }
    }

    @Override
    public Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(AiChatMessageSendReqVO sendReqVO,
            Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService
                .validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiModelDO model = modalService.validateModel(conversation.getModelId());
        StreamingChatModel chatModel = modalService.getChatModel(model.getId());

        // 2.1 知识库找回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = recallKnowledgeSegment(
                sendReqVO.getContent(), conversation);

        // 2.2 联网搜索
        AiWebSearchResponse webSearchResponse = Boolean.TRUE.equals(sendReqVO.getUseSearch()) && webSearchClient != null ?
                webSearchClient.search(new AiWebSearchRequest().setQuery(sendReqVO.getContent())
                        .setSummary(true).setCount(WEB_SEARCH_COUNT)) : null;

        // 3. 构建 Prompt（用于模型调用 + 预算估算）
        Prompt prompt = buildPrompt(conversation, historyMessages, knowledgeSegments, webSearchResponse, model, sendReqVO);
        // 4. 预算预扣费（必须在落库之前，避免超限时留下脏数据）
        Long tenantId = TenantContextHolder.getTenantId();
        int estimatedPromptTokens = estimatePromptTokens(prompt);
        AiBudgetChecker.PreDeductResult preDeductResult = budgetChecker.preDeduct(
                tenantId, userId, estimateCost(model, estimatedPromptTokens));
        try {
            // 5.1 插入 user 发送消息
            AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                    userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext(),
                    null, sendReqVO.getAttachmentUrls(), null);

            // 5.2 插入 assistant 接收消息
            AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                    userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext(),
                    knowledgeSegments, null, webSearchResponse);

            // 5.3 调用模型
            LocalDateTime requestTime = LocalDateTime.now();
            Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

            // 5.4 流式返回
            StringBuffer contentBuffer = new StringBuffer();
            StringBuffer reasoningContentBuffer = new StringBuffer();
            AtomicReference<ChatResponse> lastChunkRef = new AtomicReference<>();

            // 防止执行多次知识库和联网搜索
            AtomicBoolean firstExecuteFlag = new AtomicBoolean(true);
            AtomicReference<List<AiChatMessageRespVO.KnowledgeSegment>> cacheSegments = new AtomicReference<>();
            AtomicReference<List<AiWebSearchResponse.WebPage>> cacheWebSearchPages = new AtomicReference<>();
            return streamResponse.map(chunk -> {
                // 持续更新最后一个 chunk，用于 doOnComplete 时提取 usage
                lastChunkRef.set(chunk);
                // 仅首次：返回知识库、联网搜索
                if (StrUtil.isEmpty(contentBuffer)) {
                    if (firstExecuteFlag.compareAndSet(true, false)) { // CAS 操作，确保仅执行一次
                        Map<Long, AiKnowledgeDocumentDO> documentMap = TenantUtils.execute(tenantId, () -> knowledgeDocumentService.getKnowledgeDocumentMap(
                                convertSet(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getDocumentId)));
                        cacheSegments.set(BeanUtils.toBean(knowledgeSegments, AiChatMessageRespVO.KnowledgeSegment.class, segment -> {
                            AiKnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                            segment.setDocumentName(document != null ? document.getName() : null);
                        }));
                        if (webSearchResponse != null) {
                            cacheWebSearchPages.set(webSearchResponse.getLists());
                        }
                    }
                }
                // 响应结果
                String newContent = AiUtils.getChatResponseContent(chunk);
                String newReasoningContent = AiUtils.getChatResponseReasoningContent(chunk);
                if (StrUtil.isNotEmpty(newContent)) {
                    contentBuffer.append(newContent);
                }
                if (StrUtil.isNotEmpty(newReasoningContent)) {
                    reasoningContentBuffer.append(newReasoningContent);
                }
                return success(new AiChatMessageSendRespVO()
                        .setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                        .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class)
                                .setContent(StrUtil.nullToDefault(newContent, "")) // 避免 null 的 情况
                                .setReasoningContent(StrUtil.nullToDefault(newReasoningContent, "")) // 避免 null 的 情况
                                .setSegments(cacheSegments.get()).setWebSearchPages(cacheWebSearchPages.get()))); // 知识库 + 联网搜索
            }).doOnComplete(() -> {
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> {
                    chatMessageMapper.updateById(
                            new AiChatMessageDO().setId(assistantMessage.getId()).setContent(contentBuffer.toString())
                                    .setReasoningContent(reasoningContentBuffer.toString()));
                    // 记录调用日志
                    createChatCallLog(model, userId, assistantMessage.getId(), conversation.getId(),
                            requestTime, lastChunkRef.get(), AiCallStatusEnum.SUCCESS.getStatus(), null,
                            null, null, null, null, null, null, preDeductResult,
                            StrUtil.isNotEmpty(contentBuffer));
                });
            }).doOnError(throwable -> {
                log.error("[sendChatMessageStream][userId({}) sendReqVO({}) 发生异常]", userId, sendReqVO, throwable);
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> {
                    boolean budgetFinalized = false;
                    String errorMessage = throwable.getMessage();
                    try {
                        try {
                            // 如果有内容，则更新内容
                            if (StrUtil.isNotEmpty(contentBuffer)) {
                                chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId())
                                        .setContent(contentBuffer.toString()).setReasoningContent(reasoningContentBuffer.toString()));
                            } else {
                                // 否则，则进行删除
                                chatMessageMapper.deleteById(assistantMessage.getId());
                            }
                        } catch (Exception persistEx) {
                            log.error("[sendChatMessageStream][assistantMessageId({}) 持久化失败]", assistantMessage.getId(), persistEx);
                            if (StrUtil.isNotBlank(persistEx.getMessage())) {
                                errorMessage = StrUtil.blankToDefault(errorMessage, "")
                                        + "; persist_error=" + persistEx.getMessage();
                            }
                        }
                        // 记录调用日志（失败）
                        createChatCallLog(model, userId, assistantMessage.getId(), conversation.getId(),
                                requestTime, lastChunkRef.get(), AiCallStatusEnum.FAIL.getStatus(), errorMessage,
                                null, null, null, null, null, null, preDeductResult,
                                StrUtil.isNotEmpty(contentBuffer));
                        budgetFinalized = true;
                    } finally {
                        if (!budgetFinalized) {
                            budgetChecker.release(preDeductResult);
                        }
                    }
                });
            }).doOnCancel(() -> {
                log.info("[sendChatMessageStream][userId({}) sendReqVO({}) 取消请求]", userId, sendReqVO);
                // 使用捕获的 tenantId，因为 Flux 异步无法透传租户
                TenantUtils.execute(tenantId, () -> {
                    boolean budgetFinalized = false;
                    String errorMessage = null;
                    try {
                        try {
                            // 如果有内容，则更新内容
                            if (StrUtil.isNotEmpty(contentBuffer)) {
                                chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId())
                                        .setContent(contentBuffer.toString()).setReasoningContent(reasoningContentBuffer.toString()));
                            } else {
                                // 否则，则进行删除
                                chatMessageMapper.deleteById(assistantMessage.getId());
                            }
                        } catch (Exception persistEx) {
                            log.error("[sendChatMessageStream][assistantMessageId({}) 取消时持久化失败]", assistantMessage.getId(), persistEx);
                            errorMessage = persistEx.getMessage();
                        }
                        // 记录调用日志（取消）
                        createChatCallLog(model, userId, assistantMessage.getId(), conversation.getId(),
                                requestTime, lastChunkRef.get(), AiCallStatusEnum.CANCEL.getStatus(), errorMessage,
                                null, null, null, null, null, null, preDeductResult,
                                StrUtil.isNotEmpty(contentBuffer));
                        budgetFinalized = true;
                    } finally {
                        if (!budgetFinalized) {
                            budgetChecker.release(preDeductResult);
                        }
                    }
                });
            }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.CHAT_STREAM_ERROR)));
        } catch (Exception e) {
            budgetChecker.release(preDeductResult);
            throw e;
        }
    }

    /**
     * 创建聊天调用日志
     *
     * @param model           模型
     * @param userId          用户编号
     * @param bizId           业务主键（assistant message id）
     * @param conversationId  会话编号
     * @param requestTime     请求时间
     * @param chatResponse    响应（成功时传入，失败时为 null）
     * @param status          状态
     * @param errorMessage    错误信息
     * @param promptTokens    输入 token（覆盖 usage 提取值，传 null 则从 response 提取）
     * @param completionTokens 输出 token
     * @param cachedTokens    缓存命中 token
     * @param reasoningTokens 推理 token
     * @param totalTokens     总 token
     * @param tokenSource     token 来源（传 null 则自动判断）
     * @param hasOutputContent 是否有输出内容（用于失败/取消时无 usage 的估算兜底）
     */
    private void createChatCallLog(AiModelDO model, Long userId, Long bizId, Long conversationId,
                                   LocalDateTime requestTime, ChatResponse chatResponse,
                                   String status, String errorMessage,
                                   Integer promptTokens, Integer completionTokens,
                                   Integer cachedTokens, Integer reasoningTokens,
                                   Integer totalTokens, String tokenSource,
                                   AiBudgetChecker.PreDeductResult preDeductResult,
                                   boolean hasOutputContent) {
        AiModelCallLogDO callLog = null;
        try {
            LocalDateTime responseTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(requestTime, responseTime).toMillis();
            Long costAmount = null;

            // 从 ChatResponse 提取 usage
            if (chatResponse != null && chatResponse.getMetadata() != null
                    && chatResponse.getMetadata().getUsage() != null) {
                Usage usage = chatResponse.getMetadata().getUsage();
                if (promptTokens == null) {
                    promptTokens = usage.getPromptTokens();
                }
                if (completionTokens == null) {
                    completionTokens = usage.getCompletionTokens();
                }
                if (totalTokens == null) {
                    totalTokens = usage.getTotalTokens();
                }
                if (tokenSource == null) {
                    tokenSource = AiTokenSourceEnum.PROVIDER.getSource();
                }
            }
            if (tokenSource == null) {
                tokenSource = AiTokenSourceEnum.NONE.getSource();
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

            // 截断错误信息
            if (errorMessage != null && errorMessage.length() > 1024) {
                errorMessage = errorMessage.substring(0, 1024);
            }

            callLog = AiModelCallLogDO.builder()
                    .userId(userId)
                    .platform(model.getPlatform())
                    .modelId(model.getId())
                    .model(model.getModel())
                    .apiKeyId(model.getKeyId())
                    .bizType(AiBizTypeEnum.CHAT_MESSAGE.getType())
                    .bizId(bizId)
                    .conversationId(conversationId)
                    .requestTime(requestTime)
                    .responseTime(responseTime)
                    .durationMs((int) durationMs)
                    .status(status)
                    .errorMessage(errorMessage)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .cachedTokens(cachedTokens)
                    .reasoningTokens(reasoningTokens)
                    .tokenSource(tokenSource)
                    .costAmount(costAmount)
                    .build();
            callLogService.createCallLog(callLog);
        } catch (Exception e) {
            // 调用日志记录失败不应影响主流程和预算结算
            log.error("[createChatCallLog][userId({}) bizId({}) 记录调用日志失败]", userId, bizId, e);
        } finally {
            // 预算结算：无论日志写入是否成功，都必须结算/释放预扣费，避免 Redis 预扣金额悬挂
            if (preDeductResult != null) {
                try {
                    long actualCost = callLog != null && callLog.getCostAmount() != null ? callLog.getCostAmount() : 0L;
                    budgetChecker.settle(preDeductResult, actualCost);
                } catch (Exception e) {
                    // settle 内部已兜底；此处不再 release，避免反向冲销导致预算负值
                    log.error("[createChatCallLog][userId({}) bizId({}) 预算结算失败]", userId, bizId, e);
                }
            }
        }
    }

    private int estimatePromptTokens(Prompt prompt) {
        if (prompt == null || CollUtil.isEmpty(prompt.getInstructions())) {
            return 0;
        }
        int tokens = 0;
        for (Message message : prompt.getInstructions()) {
            tokens += StrUtil.length(StrUtil.nullToEmpty(message.getText()));
        }
        return tokens;
    }

    /**
     * 估算一次调用的最大费用（微元），用于预扣费
     *
     * 策略：输入按估算 token（至少 maxTokens），输出按 maxTokens 上限
     * 无计费配置时返回 0（不做预算拦截）
     */
    private long estimateCost(AiModelDO model, int estimatedPromptTokens) {
        cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO pricing =
                modelPricingService.getLatestModelPricing(model.getId());
        if (pricing == null) {
            return 0L;
        }
        int maxTokens = model.getMaxTokens() != null ? model.getMaxTokens() : 4096;
        estimatedPromptTokens = Math.max(estimatedPromptTokens, maxTokens);
        long priceIn = pricing.getPriceInPer1m() != null ? pricing.getPriceInPer1m() : 0L;
        long priceOut = pricing.getPriceOutPer1m() != null ? pricing.getPriceOutPer1m() : 0L;
        return Math.round((double) estimatedPromptTokens * priceIn / 1_000_000
                + (double) maxTokens * priceOut / 1_000_000);
    }

    private List<AiKnowledgeSegmentSearchRespBO> recallKnowledgeSegment(String content,
            AiChatConversationDO conversation) {
        // 1. 查询聊天角色
        if (conversation == null || conversation.getRoleId() == null) {
            return Collections.emptyList();
        }
        AiChatRoleDO role = chatRoleService.getChatRole(conversation.getRoleId());
        if (role == null || CollUtil.isEmpty(role.getKnowledgeIds())) {
            return Collections.emptyList();
        }

        // 2. 遍历召回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = new ArrayList<>();
        for (Long knowledgeId : role.getKnowledgeIds()) {
            knowledgeSegments.addAll(knowledgeSegmentService.searchKnowledgeSegment(new AiKnowledgeSegmentSearchReqBO()
                    .setKnowledgeId(knowledgeId).setContent(content)));
        }
        return knowledgeSegments;
    }

    private Prompt buildPrompt(AiChatConversationDO conversation, List<AiChatMessageDO> messages,
                               List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments,
                               AiWebSearchResponse webSearchResponse,
                               AiModelDO model, AiChatMessageSendReqVO sendReqVO) {
        List<Message> chatMessages = new ArrayList<>();
        // 1.1 System Context 角色设定
        if (StrUtil.isNotBlank(conversation.getSystemMessage())) {
            chatMessages.add(new SystemMessage(conversation.getSystemMessage()));
        }

        // 1.2 历史 history message 历史消息
        List<AiChatMessageDO> contextMessages = filterContextMessages(messages, conversation, sendReqVO);
        contextMessages.forEach(message -> {
            chatMessages.add(AiUtils.buildMessage(message.getType(), message.getContent()));
            UserMessage attachmentUserMessage = buildAttachmentUserMessage(message.getAttachmentUrls());
            if (attachmentUserMessage != null) {
                chatMessages.add(attachmentUserMessage);
            }
            // TODO @芋艿：历史的知识库；历史的搜索，要不要拼接？
        });

        // 1.3 当前 user message 新发送消息
        chatMessages.add(new UserMessage(sendReqVO.getContent()));

        // 1.4 知识库，通过 UserMessage 实现
        if (CollUtil.isNotEmpty(knowledgeSegments)) {
            String reference = knowledgeSegments.stream()
                    .map(segment -> "<Reference>" + segment.getContent() + "</Reference>")
                    .collect(Collectors.joining("\n\n"));
            chatMessages.add(new UserMessage(String.format(KNOWLEDGE_USER_MESSAGE_TEMPLATE, reference)));
        }

        // 1.5 联网搜索，通过 UserMessage 实现
        if (webSearchResponse != null && CollUtil.isNotEmpty(webSearchResponse.getLists())) {
            String webSearch = webSearchResponse.getLists().stream()
                    .map(page -> {
                        String summary = StrUtil.isNotEmpty(page.getSummary()) ?
                                "\nSummary: " + page.getSummary() : "";
                        return "<WebSearch title=\"" + page.getTitle() + "\" url=\"" + page.getUrl() + "\">"
                                + StrUtil.blankToDefault(page.getSummary(), page.getSnippet()) + "</WebSearch>";
                    })
                    .collect(Collectors.joining("\n\n"));
            chatMessages.add(new UserMessage(String.format(WEB_SEARCH_USER_MESSAGE_TEMPLATE, webSearch)));
        }

        // 1.6 附件，通过 UserMessage 实现
        if (CollUtil.isNotEmpty(sendReqVO.getAttachmentUrls())) {
            UserMessage attachmentUserMessage = buildAttachmentUserMessage(sendReqVO.getAttachmentUrls());
            if (attachmentUserMessage != null) {
                chatMessages.add(attachmentUserMessage);
            }
        }

        // 2.1 查询 tool 工具
        List<ToolCallback> toolCallbacks = getToolCallbackListByRoleId(conversation.getRoleId());
        Map<String,Object> toolContext = CollUtil.isNotEmpty(toolCallbacks) ? AiUtils.buildCommonToolContext()
                : Map.of();
        // 2.2 构建 ChatOptions 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(),
                conversation.getTemperature(), conversation.getMaxTokens(),
                toolCallbacks, toolContext);
        return new Prompt(chatMessages, chatOptions);
    }

    private List<ToolCallback> getToolCallbackListByRoleId(Long roleId) {
        if (roleId == null) {
            return null;
        }
        AiChatRoleDO chatRole = chatRoleService.getChatRole(roleId);
        if (chatRole == null) {
            return null;
        }
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        // 1. 通过 toolIds
        if (CollUtil.isNotEmpty(chatRole.getToolIds())) {
            Set<String> toolNames = convertSet(toolService.getToolList(chatRole.getToolIds()), AiToolDO::getName);
            toolNames.forEach(toolName -> {
                ToolCallback toolCallback = toolCallbackResolver.resolve(toolName);
                if (toolCallback != null) {
                    toolCallbacks.add(toolCallback);
                }
            });
        }
        // 2. 通过 mcpClients
        if (CollUtil.isNotEmpty(mcpClients) && CollUtil.isNotEmpty(chatRole.getMcpClientNames())) {
            chatRole.getMcpClientNames().forEach(mcpClientName -> {
                // 2.1 标准化名字，参考 McpClientAutoConfiguration 的 connectedClientName 方法
                String finalMcpClientName = mcpClientCommonProperties.getName() + " - " + mcpClientName;
                // 2.2 匹配对应的 McpSyncClient
                mcpClients.forEach(mcpClient -> {
                    if (ObjUtil.notEqual(mcpClient.getClientInfo().name(), finalMcpClientName)) {
                        return;
                    }
                    ToolCallback[] mcpToolCallBacks = new SyncMcpToolCallbackProvider(mcpClient).getToolCallbacks();
                    CollUtil.addAll(toolCallbacks, mcpToolCallBacks);
                });
            });
        }
        return toolCallbacks;
    }

    /**
     * 从历史消息中，获得倒序的 n 组消息作为消息上下文
     * <p>
     * n 组：指的是 user + assistant 形成一组
     *
     * @param messages     消息列表
     * @param conversation 对话
     * @param sendReqVO    发送请求
     * @return 消息上下文
     */
    private List<AiChatMessageDO> filterContextMessages(List<AiChatMessageDO> messages,
            AiChatConversationDO conversation,
            AiChatMessageSendReqVO sendReqVO) {
        if (conversation.getMaxContexts() == null || ObjUtil.notEqual(sendReqVO.getUseContext(), Boolean.TRUE)) {
            return Collections.emptyList();
        }
        List<AiChatMessageDO> contextMessages = new ArrayList<>(conversation.getMaxContexts() * 2);
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiChatMessageDO assistantMessage = CollUtil.get(messages, i);
            if (assistantMessage == null || assistantMessage.getReplyId() == null) {
                continue;
            }
            AiChatMessageDO userMessage = CollUtil.get(messages, i - 1);
            if (userMessage == null
                    || ObjUtil.notEqual(assistantMessage.getReplyId(), userMessage.getId())
                    || StrUtil.isEmpty(assistantMessage.getContent())) {
                continue;
            }
            // 由于后续要 reverse 反转，所以先添加 assistantMessage
            contextMessages.add(assistantMessage);
            contextMessages.add(userMessage);
            // 超过最大上下文，结束
            if (contextMessages.size() >= conversation.getMaxContexts() * 2) {
                break;
            }
        }
        Collections.reverse(contextMessages);
        return contextMessages;
    }

    private UserMessage buildAttachmentUserMessage(List<String> attachmentUrls) {
        if (CollUtil.isEmpty(attachmentUrls)) {
            return null;
        }
        // 读取文件内容
        Map<String, String> attachmentContents = Maps.newLinkedHashMapWithExpectedSize(attachmentUrls.size());
        for (String attachmentUrl : attachmentUrls) {
            try {
                String name = FileNameUtil.getName(attachmentUrl);
                String mineType = FileTypeUtils.getMineType(name);
                String content;
                if (FileTypeUtils.isImage(mineType)) {
                    // 特殊：图片则转为 Base64
                    byte[] bytes = HttpUtil.downloadBytes(attachmentUrl);
                    content = Base64.encode(bytes);
                } else {
                    content = knowledgeDocumentService.readUrl(attachmentUrl);
                }
                if (StrUtil.isNotEmpty(content)) {
                    attachmentContents.put(name, content);
                }
            } catch (Exception e) {
                log.error("[buildAttachmentUserMessage][读取附件({}) 发生异常]", attachmentUrl, e);
            }
        }
        if (CollUtil.isEmpty(attachmentContents)) {
            return null;
        }

        // 拼接 UserMessage 消息
        String attachment = attachmentContents.entrySet().stream()
                .map(entry -> "<Attachment name=\"" + entry.getKey() + "\">" + entry.getValue() + "</Attachment>")
                .collect(Collectors.joining("\n\n"));
        return new UserMessage(String.format(Attachment_USER_MESSAGE_TEMPLATE, attachment));
    }

    private AiChatMessageDO createChatMessage(Long conversationId, Long replyId,
                                              AiModelDO model, Long userId, Long roleId,
                                              MessageType messageType, String content, Boolean useContext,
                                              List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments,
                                              List<String> attachmentUrls,
                                              AiWebSearchResponse webSearchResponse) {
        AiChatMessageDO message = new AiChatMessageDO().setConversationId(conversationId).setReplyId(replyId)
                .setModel(model.getModel()).setModelId(model.getId()).setUserId(userId).setRoleId(roleId)
                .setType(messageType.getValue()).setContent(content).setUseContext(useContext)
                .setSegmentIds(convertList(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getId))
                .setAttachmentUrls(attachmentUrls);
        if (webSearchResponse != null) {
            message.setWebSearchPages(webSearchResponse.getLists());
        }
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<AiChatMessageDO> getChatMessageListByConversationId(Long conversationId) {
        return chatMessageMapper.selectListByConversationId(conversationId);
    }

    @Override
    public void deleteChatMessage(Long id, Long userId) {
        // 1. 校验消息存在
        AiChatMessageDO message = chatMessageMapper.selectById(id);
        if (message == null || ObjUtil.notEqual(message.getUserId(), userId)) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteById(id);
    }

    @Override
    public void deleteChatMessageByConversationId(Long conversationId, Long userId) {
        // 1. 校验消息存在
        List<AiChatMessageDO> messages = chatMessageMapper.selectListByConversationId(conversationId);
        if (CollUtil.isEmpty(messages) || ObjUtil.notEqual(messages.get(0).getUserId(), userId)) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteByIds(convertList(messages, AiChatMessageDO::getId));
    }

    @Override
    public void deleteChatMessageByAdmin(Long id) {
        // 1. 校验消息存在
        AiChatMessageDO message = chatMessageMapper.selectById(id);
        if (message == null) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteById(id);
    }

    @Override
    public Map<Long, Integer> getChatMessageCountMap(Collection<Long> conversationIds) {
        return chatMessageMapper.selectCountMapByConversationId(conversationIds);
    }

    @Override
    public PageResult<AiChatMessageDO> getChatMessagePage(AiChatMessagePageReqVO pageReqVO) {
        return chatMessageMapper.selectPage(pageReqVO);
    }

}
