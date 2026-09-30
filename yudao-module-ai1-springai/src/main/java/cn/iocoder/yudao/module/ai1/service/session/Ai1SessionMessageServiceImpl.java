package cn.iocoder.yudao.module.ai1.service.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1SessionMessageSendReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.session.Ai1SessionMessageMapper;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.harness.session.Ai1SessionStreamTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.SESSION_MESSAGE_NOT_EXISTS;

/**
 * AI1 会话消息 Service 实现类
 *
 * 流式生成与下发委托 {@link Ai1SessionStreamTool}；本类只负责会话校验与消息落库
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class Ai1SessionMessageServiceImpl implements Ai1SessionMessageService {

    @Resource
    private Ai1SessionMessageMapper sessionMessageMapper;

    @Resource
    private Ai1SessionService sessionService;
    @Resource
    private Ai1AgentService agentService;

    @Resource
    private Ai1SessionStreamTool sessionStreamTool;

    @Override
    public List<Ai1SessionMessageDO> getSessionMessageListBySessionId(Long sessionId) {
        return sessionMessageMapper.selectListBySessionId(sessionId);
    }

    @Override
    public List<Ai1SessionMessageDO> getSessionMessageListBySessionIdAndIdLessThan(Long sessionId, Long id, Integer limit) {
        return sessionMessageMapper.selectListBySessionIdAndIdLessThan(sessionId, id, limit);
    }

    @Override
    public SseEmitter sendSessionMessageStream(Long userId, Ai1SessionMessageSendReqVO sendReqVO) {
        try {
            // 1. 校验会话归属、Agent 存在且已开启
            Ai1SessionDO session = sessionService.validateSessionMy(userId, sendReqVO.getSessionId());
            agentService.validateAgentEnabled(session.getAgentId());

            // 2. 同一事务内落库用户消息与助手占位
            Long messageId = getSelf().createRoundMessages(session, sendReqVO.getContent());

            // 3. 投递生成任务，并打开 SSE 连接
            sessionStreamTool.submit(messageId, session.getAgentId(), session.getId(), sendReqVO.getContent());
            return sessionStreamTool.open(messageId, null);
        } catch (ServiceException e) {
            return sessionStreamTool.error(e.getMessage());
        } catch (Exception e) {
            log.warn("[sendSessionMessageStream][用户({}) 会话({}) 提交失败]", userId, sendReqVO.getSessionId(), e);
            return sessionStreamTool.error("会话提交失败，请稍后重试");
        }
    }

    @Override
    public SseEmitter resumeSessionMessageStream(Long userId, Long messageId, String lastEventId) {
        try {
            // 1. 校验消息存在、为助手消息，且所属会话归属当前用户
            Ai1SessionMessageDO message = sessionMessageMapper.selectById(messageId);
            if (message == null || !Ai1SessionMessageRoleEnum.isAssistant(message.getRole())) {
                throw exception(SESSION_MESSAGE_NOT_EXISTS);
            }
            sessionService.validateSessionMy(userId, message.getSessionId());

            // 2. 校验通过后，从 lastEventId 之后续传结果流，不重新生成
            return sessionStreamTool.open(messageId, lastEventId);
        } catch (ServiceException e) {
            return sessionStreamTool.error(e.getMessage());
        }
    }

    /**
     * 开启一轮会话：首条消息自动生成标题 → 落库用户消息（完成）与助手占位（生成中）→ 刷新会话活跃时间
     *
     * @param session 已校验归属的会话
     * @param content 提问内容
     * @return 助手消息编号，即结果流标识
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRoundMessages(Ai1SessionDO session, String content) {
        // 1. 首条消息时，使用提问内容作为标题（超长截断）
        if (Ai1SessionDO.TITLE_DEFAULT.equals(session.getTitle())
                && sessionMessageMapper.selectCountBySessionId(session.getId()) == 0) {
            sessionService.updateSessionTitle(session.getId(),
                    StrUtil.maxLength(content.trim(), Ai1SessionDO.TITLE_MAX_LENGTH - 3));
        }

        // 2. 用户消息 + 助手占位；刷新页面后，前端按生成中的助手消息编号续传
        sessionMessageMapper.insert(new Ai1SessionMessageDO().setSessionId(session.getId())
                .setRole(Ai1SessionMessageRoleEnum.USER.getRole()).setContent(content)
                .setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus()));
        Ai1SessionMessageDO assistantMessage = new Ai1SessionMessageDO().setSessionId(session.getId())
                .setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole()).setContent("")
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        sessionMessageMapper.insert(assistantMessage);

        // 3. 刷新会话活跃时间
        sessionService.touchSession(session.getId());
        return assistantMessage.getId();
    }

    @Override
    public void updateSessionMessage(Ai1SessionMessageDO updateObj) {
        sessionMessageMapper.updateById(updateObj);
    }

    @Override
    public void deleteSessionMessageListBySessionIds(Collection<Long> sessionIds) {
        if (CollUtil.isEmpty(sessionIds)) {
            return;
        }
        sessionMessageMapper.deleteBySessionIds(sessionIds);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     */
    private Ai1SessionMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
