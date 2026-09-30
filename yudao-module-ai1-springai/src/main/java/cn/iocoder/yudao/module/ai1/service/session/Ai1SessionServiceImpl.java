package cn.iocoder.yudao.module.ai1.service.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.session.Ai1SessionMapper;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.SESSION_NOT_EXISTS;

/**
 * AI1 对话 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1SessionServiceImpl implements Ai1SessionService {

    @Resource
    private Ai1SessionMapper sessionMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1AgentService agentService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1SessionMessageService sessionMessageService;

    @Override
    public Long createSessionMy(Long userId, Ai1SessionCreateMyReqVO createReqVO) {
        // 1. 校验 Agent 存在且已开启
        agentService.validateAgentEnabled(createReqVO.getAgentId());

        // 2. 插入：首条消息发送后，标题自动替换为提问内容
        Ai1SessionDO session = new Ai1SessionDO().setAgentId(createReqVO.getAgentId())
                .setUserId(userId).setTitle(Ai1SessionDO.TITLE_DEFAULT);
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public void updateSessionMy(Long userId, Ai1SessionUpdateMyReqVO updateReqVO) {
        // 1. 校验归属
        validateSessionMy(userId, updateReqVO.getId());

        // 2. 更新标题
        updateSessionTitle(updateReqVO.getId(), updateReqVO.getTitle().trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSessionMy(Long userId, Long id) {
        // 1. 校验归属
        validateSessionMy(userId, id);

        // 2. 删除对话与消息
        sessionMapper.deleteById(id);
        sessionMessageService.deleteSessionMessageListBySessionIds(Collections.singletonList(id));
    }

    @Override
    public Ai1SessionDO validateSessionMy(Long userId, Long id) {
        Ai1SessionDO session = sessionMapper.selectById(id);
        if (session == null || ObjUtil.notEqual(session.getUserId(), userId)) {
            throw exception(SESSION_NOT_EXISTS);
        }
        return session;
    }

    @Override
    public List<Ai1SessionDO> getSessionListByAgentIdAndUserId(Long agentId, Long userId) {
        return sessionMapper.selectListByAgentIdAndUserId(agentId, userId);
    }

    @Override
    public PageResult<Ai1SessionDO> getSessionPage(Ai1SessionPageReqVO pageReqVO) {
        return sessionMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateSessionTitle(Long id, String title) {
        sessionMapper.updateById(new Ai1SessionDO().setId(id).setTitle(title));
    }

    @Override
    public void touchSession(Long id) {
        sessionMapper.updateById(new Ai1SessionDO().setId(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSessionListByAgentIds(Collection<Long> agentIds) {
        // 1. 查询 Agent 下的全部对话编号
        if (CollUtil.isEmpty(agentIds)) {
            return;
        }
        List<Long> sessionIds = convertList(sessionMapper.selectListByAgentIds(agentIds), Ai1SessionDO::getId);
        if (CollUtil.isEmpty(sessionIds)) {
            return;
        }

        // 2. 删除对话与消息
        sessionMapper.deleteByIds(sessionIds);
        sessionMessageService.deleteSessionMessageListBySessionIds(sessionIds);
    }

}
