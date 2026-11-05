package cn.iocoder.yudao.module.ai.service.mindmap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapGenerateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.mindmap.AiMindMapDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.mindmap.AiMindMapMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnum;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogService;
import cn.iocoder.yudao.module.ai.service.requestlog.dto.AiRequestLogCreateReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
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
    private AiRequestLogService requestLogService;

    @Resource
    private AiMindMapMapper mindMapMapper;

    @Override
    public Flux<CommonResult<AiMindMapGenerateRespVO>> generateMindMap(AiMindMapGenerateReqVO generateReqVO,
                                                                       Long userId) {
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

        // 2.1 插入思维导图信息
        String requestId = IdUtil.fastUUID();
        AiMindMapDO mindMapDO = BeanUtils.toBean(generateReqVO, AiMindMapDO.class, mindMap -> mindMap.setUserId(userId)
                .setPlatform(platform.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                .setRequestId(requestId));
        mindMapMapper.insert(mindMapDO);
        // 2.2 构建请求日志
        AiRequestLogCreateReqDTO requestLog = requestLogService.buildCreateReqDTO(requestId, userId,
                AiRequestLogBizTypeEnum.MIND_MAP, mindMapDO.getId(), model);
        AtomicBoolean finished = new AtomicBoolean(false);

        // 3.1 构建 Prompt，并进行调用
        Flux<ChatResponse> streamResponse;
        try {
            Prompt prompt = buildPrompt(generateReqVO, model, systemMessage);
            streamResponse = chatModel.stream(prompt);
        } catch (RuntimeException ex) {
            finishMindMap(requestLog, finished, mindMapDO.getId(), null, AiRequestLogStatusEnum.FAIL, ex);
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
                return success(new AiMindMapGenerateRespVO().setId(mindMapDO.getId()).setContent(newContent)
                        .setInputTokens(requestLog.getInputTokens()).setOutputTokens(requestLog.getOutputTokens()).setTotalTokens(requestLog.getTotalTokens()));
            }
        }).doOnComplete(() -> finishMindMap(requestLog, finished, mindMapDO.getId(), contentBuffer.toString(),
                AiRequestLogStatusEnum.SUCCESS, null)
        ).doOnError(throwable -> {
            log.error("[generateMindMap][generateReqVO({}) 发生异常]", generateReqVO, throwable);
            finishMindMap(requestLog, finished, mindMapDO.getId(), null, AiRequestLogStatusEnum.FAIL, throwable);
        }).doOnCancel(() -> {
            log.info("[generateMindMap][requestId({}) 取消请求]", requestId);
            finishMindMap(requestLog, finished, mindMapDO.getId(), null, AiRequestLogStatusEnum.CANCEL, null);
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.WRITE_STREAM_ERROR)));
    }

    /**
     * 结束思维导图生成：记录请求日志，并更新生成结果与用量
     *
     * 同一请求仅第一次调用生效；两次写入相互独立，失败只记录日志，不影响已返回的生成内容
     */
    private void finishMindMap(AiRequestLogCreateReqDTO requestLog, AtomicBoolean finished, Long mindMapId,
                               String generatedContent, AiRequestLogStatusEnum status, Throwable error) {
        // 1. 记录请求日志：与 chunk 的用量更新互斥，保证思维导图与日志的用量一致
        AiMindMapDO updateObj;
        synchronized (requestLog) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            requestLog.setStatus(status.getStatus()).setErrorMessage(error != null
                    ? StrUtil.blankToDefault(error.getMessage(), error.getClass().getName()) : null);
            try {
                requestLogService.createRequestLog(requestLog);
            } catch (Exception ex) {
                log.error("[finishMindMap][requestId({}) 记录请求日志失败]", requestLog.getRequestId(), ex);
            }
            updateObj = new AiMindMapDO().setId(mindMapId).setGeneratedContent(generatedContent)
                    .setErrorMessage(error != null ? error.getMessage() : null)
                    .setInputTokens(requestLog.getInputTokens()).setOutputTokens(requestLog.getOutputTokens()).setTotalTokens(requestLog.getTotalTokens());
            updateObj.setUpdater(StrUtil.toStringOrNull(requestLog.getUserId()));
        }

        // 2. 更新思维导图结果与用量：Flux 异步回调中没有租户上下文，按请求开始时的租户执行
        TenantUtils.execute(requestLog.getTenantId(), () -> {
            try {
                mindMapMapper.updateById(updateObj);
            } catch (Exception ex) {
                log.error("[finishMindMap][mindMapId({}) requestId({}) 更新思维导图结果失败]",
                        mindMapId, requestLog.getRequestId(), ex);
            }
        });
    }

    private Prompt buildPrompt(AiMindMapGenerateReqVO generateReqVO, AiModelDO model, String systemMessage) {
        // 1. 构建 message 列表
        List<Message> chatMessages = buildMessages(generateReqVO, systemMessage);
        // 2. 构建 options 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiChatUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
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
    public AiMindMapDO getMindMap(Long id) {
        return mindMapMapper.selectById(id);
    }

    @Override
    public PageResult<AiMindMapDO> getMindMapPage(AiMindMapPageReqVO pageReqVO) {
        return mindMapMapper.selectPage(pageReqVO);
    }

}
