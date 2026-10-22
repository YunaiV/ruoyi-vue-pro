package cn.iocoder.yudao.module.ai1.harness.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.dal.redis.memory.Ai1MemoryLockRedisDAO;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.harness.session.Ai1SessionStreamTool;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.memory.Ai1MemoryService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionMessageService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * AI1 长期记忆归档工具（harness 运行时）
 *
 * 【归档流程】由 Ai1MemoryArchiveJob 按租户调用 {@link #archive}
 * 1. 竞争：按租户 tryLock 归档锁，抢不到直接跳过
 * 2. 检测：遍历开启且开启记忆的 Agent，对其下每个用户，读取水位线 lastMessageId 之后的已完成消息
 * 3. 生成：「历史记忆 + 新消息」经模型生成最新记忆，写入并推进水位线
 *
 * 【关键约定】
 * 1. 新消息只取「安全前缀」：截止到水位线之后第一条生成中的消息，避免水位线越过尚未完成的回复
 * 2. 超时且无 worker 执行的孤儿生成先按中断收尾，避免永久阻塞安全前缀
 * 3. 单次最多归档 {@link #BATCH_SIZE} 条，剩余的由下次执行继续
 * 4. 写入前校验：记忆被人工修改、删除，或 Agent 已删除时放弃本次结果
 * 5. 锁顺序固定为「归档锁 → 消息执行锁（tryLock）」，快照锁只用于读取新消息，持有期间不收尾、不调用模型
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1MemoryArchiveTool {

    /**
     * 单个用户单次归档的最大消息数
     */
    static final int BATCH_SIZE = 100;

    @Resource
    private Ai1MemoryLockRedisDAO memoryLockRedisDAO;
    @Resource
    private YudaoAi1Properties ai1Properties;

    @Resource
    private Ai1AgentService agentService;
    @Resource
    private Ai1SessionService sessionService;
    @Resource
    private Ai1SessionMessageService sessionMessageService;
    @Resource
    private Ai1MemoryService memoryService;

    @Resource
    private Ai1MemoryTool memoryTool;
    @Resource
    private Ai1SessionStreamTool sessionStreamTool;

    /**
     * 归档当前租户下全部开启记忆的 Agent
     *
     * @return 归档成功的用户数
     */
    public int archive() {
        // 1. 按租户竞争归档锁，其他节点归档中时直接跳过
        Integer count = memoryLockRedisDAO.tryLockArchive(TenantContextHolder.getTenantId(), this::archiveAll);
        if (count == null) {
            log.info("[archive][租户({}) 归档进行中，跳过本次]", TenantContextHolder.getTenantId());
            return 0;
        }
        return count;
    }

    private int archiveAll() {
        // 2. 逐个 Agent、逐个用户归档；单个用户失败不影响其他用户
        int count = 0;
        int maxLength = ai1Properties.getMemory().getMaxLength();
        for (Ai1AgentDO agent : agentService.getAgentListByStatus(CommonStatusEnum.ENABLE.getStatus())) {
            if (!Boolean.TRUE.equals(agent.getMemoryEnabled())) {
                continue;
            }
            for (Long userId : sessionService.getUserIdListByAgentId(agent.getId())) {
                if (Thread.currentThread().isInterrupted()) {
                    return count;
                }
                try {
                    if (archiveOne(agent, userId, maxLength)) {
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("[archive][Agent({}) 用户({}) 归档失败]", agent.getId(), userId, e);
                }
            }
        }
        return count;
    }

    /**
     * 归档单个用户：读取新消息 → 生成记忆 → 校验后写入并推进水位线
     *
     * @return 是否写入成功
     */
    boolean archiveOne(Ai1AgentDO agent, Long userId, int maxLength) {
        // 1.1 读取水位线，并收尾水位线之后超时的孤儿生成
        Ai1MemoryDO memory = memoryService.getMemoryByAgentIdAndUserId(agent.getId(), userId);
        Long lastMessageId = memory != null ? ObjUtil.defaultIfNull(memory.getLastMessageId(), 0L) : 0L;
        failStaleMessages(agent.getId(), userId, lastMessageId);
        // 1.2 读取水位线之后的新消息，无则跳过
        List<Ai1SessionMessageDO> messages = getNewMessageList(agent.getId(), userId, lastMessageId);
        if (CollUtil.isEmpty(messages)) {
            return false;
        }

        // 2. 生成最新记忆
        String content = memoryTool.generate(agent, memory != null ? memory.getContent() : null, messages, maxLength);
        if (StrUtil.isBlank(content)) {
            return false;
        }

        // 3.1 首次归档（含删除后重新生成）：Agent 已删除，或记忆已存在时放弃，由下次执行基于新记忆继续
        Long newLastMessageId = CollUtil.getLast(messages).getId();
        if (memory == null) {
            if (!memoryService.createMemoryIfAgentExists(Ai1MemoryDO.builder().agentId(agent.getId()).userId(userId)
                    .content(content).lastMessageId(newLastMessageId).build())) {
                log.info("[archiveOne][Agent({}) 用户({}) Agent 已删除或记忆已存在，放弃本次结果]", agent.getId(), userId);
                return false;
            }
            return true;
        }
        // 3.2 再次归档：记忆被人工修改或删除时放弃
        if (!memoryService.updateMemoryIfUnchanged(memory, content, newLastMessageId)) {
            log.info("[archiveOne][Agent({}) 用户({}) 记忆已变更，放弃本次结果]", agent.getId(), userId);
            return false;
        }
        return true;
    }

    /**
     * 收尾水位线之后超时的孤儿生成，直到遇到仍在执行或未超时的生成
     *
     * 在快照锁外执行：收尾需抢消息执行锁并读写结果流，避免持有快照锁期间阻塞发送消息
     */
    private void failStaleMessages(Long agentId, Long userId, Long lastMessageId) {
        List<Ai1SessionDO> sessions = sessionService.getSessionListByAgentIdAndUserId(agentId, userId);
        if (CollUtil.isEmpty(sessions)) {
            return;
        }
        List<Long> sessionIds = convertList(sessions, Ai1SessionDO::getId);
        for (int i = 0; i < BATCH_SIZE; i++) {
            Ai1SessionMessageDO generatingMessage = sessionMessageService.getFirstSessionMessageBySessionIdsAndStatusAndIdGt(
                    sessionIds, Ai1SessionMessageStatusEnum.GENERATING.getStatus(), lastMessageId);
            if (generatingMessage == null || !sessionStreamTool.failStaleMessage(generatingMessage)) {
                return;
            }
        }
    }

    /**
     * 获得水位线之后的新消息：截止到第一条生成中的消息，按编号升序，最多 {@link #BATCH_SIZE} 条
     *
     * 持有与消息创建相同的快照锁读取：创建在锁内提交，读取时不存在编号更小、尚未提交的消息
     */
    private List<Ai1SessionMessageDO> getNewMessageList(Long agentId, Long userId, Long lastMessageId) {
        return memoryLockRedisDAO.lockSnapshot(agentId, userId, () -> {
            List<Ai1SessionDO> sessions = sessionService.getSessionListByAgentIdAndUserId(agentId, userId);
            if (CollUtil.isEmpty(sessions)) {
                return Collections.emptyList();
            }
            List<Long> sessionIds = convertList(sessions, Ai1SessionDO::getId);
            Ai1SessionMessageDO generatingMessage = sessionMessageService.getFirstSessionMessageBySessionIdsAndStatusAndIdGt(
                    sessionIds, Ai1SessionMessageStatusEnum.GENERATING.getStatus(), lastMessageId);
            return sessionMessageService.getSessionMessageListBySessionIdsAndStatusAndIdBetween(sessionIds,
                    Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), lastMessageId,
                    generatingMessage != null ? generatingMessage.getId() : null, BATCH_SIZE);
        });
    }

}
