package cn.iocoder.yudao.module.ai1.service.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryUpdateReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.memory.Ai1MemoryMapper;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MEMORY_NOT_EXISTS;

/**
 * AI1 长期记忆 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1MemoryServiceImpl implements Ai1MemoryService {

    @Resource
    private Ai1MemoryMapper memoryMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1AgentService agentService;

    @Override
    public void updateMemory(Ai1MemoryUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validateMemoryExists(updateReqVO.getId());

        // 2. 更新正文；水位线不变，之后的归档写入前校验到正文变化时放弃
        Ai1MemoryDO updateObj = BeanUtils.toBean(updateReqVO, Ai1MemoryDO.class);
        memoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteMemory(Long id) {
        deleteMemoryListByIds(Collections.singletonList(id));
    }

    @Override
    public void deleteMemoryListByIds(List<Long> ids) {
        // 1. 校验存在
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        ids.forEach(this::validateMemoryExists);

        // 2. 逻辑删除，保留历史记录；进行中的归档写入会因记录已删除而放弃
        memoryMapper.deleteByIds(ids);
    }

    @Override
    public void deleteMemoryListByAgentIds(Collection<Long> agentIds) {
        if (CollUtil.isEmpty(agentIds)) {
            return;
        }
        memoryMapper.deleteBatch(Ai1MemoryDO::getAgentId, agentIds);
    }

    private void validateMemoryExists(Long id) {
        if (memoryMapper.selectById(id) == null) {
            throw exception(MEMORY_NOT_EXISTS);
        }
    }

    @Override
    public Ai1MemoryDO getMemory(Long id) {
        return memoryMapper.selectById(id);
    }

    @Override
    public Ai1MemoryDO getMemoryByAgentIdAndUserId(Long agentId, Long userId) {
        return memoryMapper.selectByAgentIdAndUserId(agentId, userId);
    }

    @Override
    public boolean createMemoryIfAgentExists(Ai1MemoryDO memory) {
        // 1. 前置校验 Agent 存在，且该用户没有未删除的记忆；同租户归档由归档锁串行，极端并发下的重复记录由按用户查询容忍
        if (agentService.getAgent(memory.getAgentId()) == null
                || memoryMapper.selectByAgentIdAndUserId(memory.getAgentId(), memory.getUserId()) != null) {
            return false;
        }

        // 2. 插入
        memoryMapper.insert(memory);
        return true;
    }

    @Override
    public boolean updateMemoryIfUnchanged(Ai1MemoryDO old, String content, Long lastMessageId) {
        // 1. 校验记忆未删除，且水位线与正文都未变化
        Ai1MemoryDO memory = memoryMapper.selectById(old.getId());
        if (memory == null || ObjUtil.notEqual(memory.getLastMessageId(), old.getLastMessageId())
                || ObjUtil.notEqual(memory.getContent(), old.getContent())) {
            return false;
        }

        // 2. 更新正文并推进水位线；校验之后的人工修改不再拦截，已删除的记录不会被更新
        return memoryMapper.updateById(new Ai1MemoryDO().setId(old.getId())
                .setContent(content).setLastMessageId(lastMessageId)) > 0;
    }

    @Override
    public PageResult<Ai1MemoryDO> getMemoryPage(Ai1MemoryPageReqVO pageReqVO) {
        return memoryMapper.selectPage(pageReqVO);
    }

}
