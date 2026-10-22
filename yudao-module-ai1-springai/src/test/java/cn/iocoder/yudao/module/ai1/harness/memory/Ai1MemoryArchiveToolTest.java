package cn.iocoder.yudao.module.ai1.harness.memory;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1MemoryArchiveTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1MemoryArchiveToolTest extends BaseMockitoUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final Long AGENT_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final List<Long> SESSION_IDS = List.of(10L, 11L);

    @InjectMocks
    private Ai1MemoryArchiveTool memoryArchiveTool;

    @Spy
    private Ai1MemoryLockRedisDAO memoryLockRedisDAO;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock archiveLock;
    @Mock
    private RLock snapshotLock;
    @Spy
    private YudaoAi1Properties ai1Properties = new YudaoAi1Properties();

    @Mock
    private Ai1AgentService agentService;
    @Mock
    private Ai1SessionService sessionService;
    @Mock
    private Ai1SessionMessageService sessionMessageService;
    @Mock
    private Ai1MemoryService memoryService;
    @Mock
    private Ai1MemoryTool memoryTool;
    @Mock
    private Ai1SessionStreamTool sessionStreamTool;

    @BeforeEach
    public void before() {
        TenantContextHolder.setTenantId(TENANT_ID);
        // mock 归档锁、快照锁
        ReflectionTestUtils.setField(memoryLockRedisDAO, "redissonClient", redissonClient);
        lenient().when(redissonClient.getLock("ai1:memory:archive-lock:" + TENANT_ID)).thenReturn(archiveLock);
        lenient().when(archiveLock.tryLock()).thenReturn(true);
        lenient().when(archiveLock.isHeldByCurrentThread()).thenReturn(true);
        lenient().when(redissonClient.getLock("ai1:memory:snapshot-lock:" + AGENT_ID + ":" + USER_ID)).thenReturn(snapshotLock);
        lenient().when(sessionService.getSessionListByAgentIdAndUserId(AGENT_ID, USER_ID)).thenReturn(List.of(
                new Ai1SessionDO().setId(10L), new Ai1SessionDO().setId(11L)));
    }

    @AfterEach
    public void after() {
        TenantContextHolder.clear();
    }

    @Test
    public void testArchive_locked() {
        // mock 其他节点归档中
        when(archiveLock.tryLock()).thenReturn(false);

        // 调用，并断言
        assertEquals(0, memoryArchiveTool.archive());
        verifyNoInteractions(agentService, memoryTool);
        verify(archiveLock, never()).unlock();
    }

    @Test
    public void testArchive_lockNotHeld() {
        // mock 方法：归档结束时锁已不属于当前线程
        when(agentService.getAgentListByStatus(CommonStatusEnum.ENABLE.getStatus())).thenReturn(List.of());
        when(archiveLock.isHeldByCurrentThread()).thenReturn(false);

        // 调用，并断言：不释放其他线程持有的锁
        assertEquals(0, memoryArchiveTool.archive());
        verify(archiveLock, never()).unlock();
    }

    @Test
    public void testArchive_onlyMemoryEnabled() {
        // mock 方法：只有开启记忆的 Agent 归档
        Ai1AgentDO agent = buildAgent();
        when(agentService.getAgentListByStatus(CommonStatusEnum.ENABLE.getStatus())).thenReturn(List.of(
                agent, buildAgent().setId(2L).setMemoryEnabled(false), buildAgent().setId(3L).setMemoryEnabled(null)));
        when(sessionService.getUserIdListByAgentId(AGENT_ID)).thenReturn(List.of(USER_ID));
        mockNewMessages(0L, null, List.of(buildMessage(12L), buildMessage(13L)));
        when(memoryTool.generate(eq(agent), isNull(), anyList(), eq(2000))).thenReturn("## 用户画像\n- 程序员");
        when(memoryService.createMemoryIfAgentExists(any())).thenReturn(true);

        // 调用
        int count = memoryArchiveTool.archive();
        // 断言：首次归档插入，水位线为本批最后一条
        assertEquals(1, count);
        verify(sessionService, never()).getUserIdListByAgentId(2L);
        verify(sessionService, never()).getUserIdListByAgentId(3L);
        verify(memoryService).createMemoryIfAgentExists(argThat(memory -> AGENT_ID.equals(memory.getAgentId())
                && USER_ID.equals(memory.getUserId()) && "## 用户画像\n- 程序员".equals(memory.getContent())
                && Long.valueOf(13L).equals(memory.getLastMessageId())));
        verify(archiveLock).unlock();
    }

    @Test
    public void testArchive_userFailedNotAffectOthers() {
        // mock 方法：第一个用户生成失败，第二个用户无新消息
        when(agentService.getAgentListByStatus(CommonStatusEnum.ENABLE.getStatus())).thenReturn(List.of(buildAgent()));
        when(sessionService.getUserIdListByAgentId(AGENT_ID)).thenReturn(List.of(USER_ID, 200L));
        mockNewMessages(0L, null, List.of(buildMessage(12L)));
        when(memoryTool.generate(any(), any(), anyList(), anyInt())).thenThrow(new IllegalStateException("模型超时"));
        when(redissonClient.getLock("ai1:memory:snapshot-lock:" + AGENT_ID + ":200")).thenReturn(snapshotLock);

        // 调用
        int count = memoryArchiveTool.archive();
        // 断言：不推进水位线，下次重试
        assertEquals(0, count);
        verify(memoryService).getMemoryByAgentIdAndUserId(AGENT_ID, 200L);
        verify(memoryService, never()).createMemoryIfAgentExists(any());
        verify(memoryService, never()).updateMemoryIfUnchanged(any(), any(), any());
        verify(archiveLock).unlock();
    }

    @Test
    public void testArchiveOne_safePrefix() {
        // mock 方法：水位线之后有生成中的消息，只读取它之前的已完成消息
        Ai1MemoryDO memory = Ai1MemoryDO.builder().id(1L).agentId(AGENT_ID).userId(USER_ID)
                .content("旧记忆").lastMessageId(11L).build();
        when(memoryService.getMemoryByAgentIdAndUserId(AGENT_ID, USER_ID)).thenReturn(memory);
        mockNewMessages(11L, 20L, List.of(buildMessage(12L), buildMessage(13L)));
        when(memoryTool.generate(any(), eq("旧记忆"), anyList(), eq(2000))).thenReturn("新记忆");
        when(memoryService.updateMemoryIfUnchanged(memory, "新记忆", 13L)).thenReturn(true);

        // 调用，并断言
        assertTrue(memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        // 断言：孤儿收尾在快照锁外，持有快照锁读取，模型在锁外调用
        InOrder inOrder = inOrder(sessionStreamTool, snapshotLock, sessionMessageService, memoryTool);
        inOrder.verify(sessionStreamTool).failStaleMessage(argThat(message -> Long.valueOf(20L).equals(message.getId())));
        inOrder.verify(snapshotLock).lock();
        inOrder.verify(sessionMessageService).getSessionMessageListBySessionIdsAndStatusAndIdBetween(SESSION_IDS,
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), 11L, 20L, Ai1MemoryArchiveTool.BATCH_SIZE);
        inOrder.verify(snapshotLock).unlock();
        inOrder.verify(memoryTool).generate(any(), any(), anyList(), anyInt());
    }

    @Test
    public void testArchiveOne_snapshotReadFailed() {
        // mock 方法：持有快照锁读取新消息时失败
        when(sessionMessageService.getSessionMessageListBySessionIdsAndStatusAndIdBetween(SESSION_IDS,
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), 0L, null, Ai1MemoryArchiveTool.BATCH_SIZE))
                .thenThrow(new IllegalStateException("数据库超时"));

        // 调用，并断言：异常抛出，快照锁仍释放
        assertThrows(IllegalStateException.class, () -> memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        verify(snapshotLock).lock();
        verify(snapshotLock).unlock();
        verifyNoInteractions(memoryTool);
    }

    @Test
    public void testArchiveOne_noNewMessages() {
        // mock 方法
        mockNewMessages(0L, null, List.of());

        // 调用，并断言
        assertFalse(memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        verifyNoInteractions(memoryTool, sessionStreamTool);
    }

    @Test
    public void testArchiveOne_staleGeneratingRecovered() {
        // mock 数据：孤儿生成之后有已完成的消息
        Ai1MemoryDO memory = Ai1MemoryDO.builder().id(1L).content("旧记忆").lastMessageId(11L).build();
        when(memoryService.getMemoryByAgentIdAndUserId(AGENT_ID, USER_ID)).thenReturn(memory);
        // mock 方法：孤儿生成收尾后，不再有生成中的消息
        Ai1SessionMessageDO generatingMessage = buildMessage(20L);
        when(sessionMessageService.getFirstSessionMessageBySessionIdsAndStatusAndIdGt(SESSION_IDS,
                Ai1SessionMessageStatusEnum.GENERATING.getStatus(), 11L)).thenReturn(generatingMessage, (Ai1SessionMessageDO) null);
        when(sessionStreamTool.failStaleMessage(generatingMessage)).thenReturn(true);
        when(sessionMessageService.getSessionMessageListBySessionIdsAndStatusAndIdBetween(SESSION_IDS,
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), 11L, null, Ai1MemoryArchiveTool.BATCH_SIZE))
                .thenReturn(List.of(buildMessage(12L), buildMessage(21L)));
        when(memoryTool.generate(any(), eq("旧记忆"), anyList(), eq(2000))).thenReturn("新记忆");
        when(memoryService.updateMemoryIfUnchanged(memory, "新记忆", 21L)).thenReturn(true);

        // 调用，并断言：水位线越过已收尾的孤儿生成
        assertTrue(memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        InOrder inOrder = inOrder(sessionStreamTool, snapshotLock);
        inOrder.verify(sessionStreamTool).failStaleMessage(generatingMessage);
        inOrder.verify(snapshotLock).lock();
    }

    @Test
    public void testArchiveOne_activeGeneratingBlocks() {
        // mock 方法：生成仍在执行或未超时，安全前缀为空
        mockNewMessages(0L, 12L, List.of());

        // 调用，并断言：不收尾，也不调用模型
        assertFalse(memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        verify(sessionStreamTool).failStaleMessage(argThat(message -> Long.valueOf(12L).equals(message.getId())));
        verifyNoInteractions(memoryTool);
    }

    @Test
    public void testArchiveOne_blankMemory() {
        // mock 方法
        mockNewMessages(0L, null, List.of(buildMessage(12L)));
        when(memoryTool.generate(any(), any(), anyList(), anyInt())).thenReturn(" ");

        // 调用，并断言：模型无输出时不写入，水位线不前进
        assertFalse(memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        verify(memoryService, never()).createMemoryIfAgentExists(any());
    }

    @Test
    public void testArchiveOne_changedDuringArchive() {
        // mock 方法：归档期间记忆被人工修改或删除
        Ai1MemoryDO memory = Ai1MemoryDO.builder().id(1L).content("旧记忆").lastMessageId(11L).build();
        when(memoryService.getMemoryByAgentIdAndUserId(AGENT_ID, USER_ID)).thenReturn(memory);
        mockNewMessages(11L, null, List.of(buildMessage(12L)));
        when(memoryTool.generate(any(), any(), anyList(), anyInt())).thenReturn("新记忆");
        when(memoryService.updateMemoryIfUnchanged(memory, "新记忆", 12L)).thenReturn(false);

        // 调用，并断言：放弃本次结果，不重新插入
        assertFalse(memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        verify(memoryService, never()).createMemoryIfAgentExists(any());
    }

    @Test
    public void testArchiveOne_insertConflict() {
        // mock 方法：其他归档已插入，或 Agent 已删除
        mockNewMessages(0L, null, List.of(buildMessage(12L)));
        when(memoryTool.generate(any(), any(), anyList(), anyInt())).thenReturn("新记忆");
        when(memoryService.createMemoryIfAgentExists(any())).thenReturn(false);

        // 调用，并断言：放弃本次结果
        assertFalse(memoryArchiveTool.archiveOne(buildAgent(), USER_ID, 2000));
        verify(memoryService, never()).updateMemoryIfUnchanged(any(), any(), any());
    }

    // ========== mock 方法 ==========

    private void mockNewMessages(Long lastMessageId, Long generatingMessageId, List<Ai1SessionMessageDO> messages) {
        when(sessionMessageService.getFirstSessionMessageBySessionIdsAndStatusAndIdGt(SESSION_IDS,
                Ai1SessionMessageStatusEnum.GENERATING.getStatus(), lastMessageId))
                .thenReturn(generatingMessageId != null ? buildMessage(generatingMessageId) : null);
        when(sessionMessageService.getSessionMessageListBySessionIdsAndStatusAndIdBetween(SESSION_IDS,
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), lastMessageId, generatingMessageId, Ai1MemoryArchiveTool.BATCH_SIZE))
                .thenReturn(messages);
    }

    // ========== 测试数据 ==========

    private static Ai1AgentDO buildAgent() {
        return new Ai1AgentDO().setId(AGENT_ID).setProviderId(1L).setModelId(2L).setMemoryEnabled(true);
    }

    private static Ai1SessionMessageDO buildMessage(Long id) {
        return new Ai1SessionMessageDO().setId(id).setContent("消息" + id);
    }

}
