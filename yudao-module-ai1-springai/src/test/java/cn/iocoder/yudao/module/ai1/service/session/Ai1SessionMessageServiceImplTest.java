package cn.iocoder.yudao.module.ai1.service.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1SessionMessagePageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1SessionMessageSendReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.session.Ai1SessionMessageMapper;
import cn.iocoder.yudao.module.ai1.dal.redis.memory.Ai1MemoryLockRedisDAO;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.harness.session.Ai1SessionStreamTool;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.SESSION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1SessionMessageServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import({Ai1SessionMessageServiceImpl.class, Ai1MemoryLockRedisDAO.class})
public class Ai1SessionMessageServiceImplTest extends BaseDbUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 100L;

    @Resource
    private Ai1SessionMessageServiceImpl sessionMessageService;

    @MockitoSpyBean
    private Ai1SessionMessageMapper sessionMessageMapper;

    @MockitoBean
    private Ai1SessionService sessionService;
    @MockitoBean
    private Ai1AgentService agentService;
    @MockitoBean
    private Ai1SessionStreamTool sessionStreamTool;
    @MockitoBean
    private RedissonClient redissonClient;

    private final RLock snapshotLock = mock(RLock.class);

    @BeforeEach
    public void before() {
        // 设置租户上下文
        TenantContextHolder.setTenantId(TENANT_ID);
        // mock 记忆快照锁
        when(redissonClient.getLock(anyString())).thenReturn(snapshotLock);
    }

    @AfterEach
    public void after() {
        // 清理租户上下文
        TenantContextHolder.clear();
    }

    @Test
    public void testSendSessionMessageStream_success() {
        // mock 方法
        Ai1SessionDO session = new Ai1SessionDO().setId(10L).setAgentId(1L).setUserId(USER_ID)
                .setTitle(Ai1SessionDO.TITLE_DEFAULT);
        // mock sessionService 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(session);
        SseEmitter emitter = new SseEmitter();
        when(sessionStreamTool.open(anyLong(), isNull())).thenReturn(emitter);
        // 准备参数
        String content = "三体舰队还有多久到达地球？请结合原著中的时间线详细说明，并对比面壁计划、执剑人与掩体计划等人类应对方案的技术发展差异";

        // 调用
        SseEmitter result = sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent(content));
        // 断言
        assertSame(emitter, result);
        List<Ai1SessionMessageDO> messages = sessionMessageMapper.selectListBySessionId(10L);
        assertEquals(2, messages.size());
        assertEquals(Ai1SessionMessageRoleEnum.USER.getRole(), messages.get(0).getRole());
        assertEquals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), messages.get(0).getStatus());
        Ai1SessionMessageDO assistant = messages.get(1);
        assertEquals(Ai1SessionMessageRoleEnum.ASSISTANT.getRole(), assistant.getRole());
        assertEquals(Ai1SessionMessageStatusEnum.GENERATING.getStatus(), assistant.getStatus());
        // 断言首条消息命名和任务投递
        verify(sessionService).updateSessionTitle(eq(10L),
                argThat(title -> title.length() == Ai1SessionDO.TITLE_MAX_LENGTH && title.endsWith("...")));
        verify(sessionStreamTool).submit(assistant.getId(), 1L, 10L, content);
        verify(sessionStreamTool).open(assistant.getId(), null);
    }

    @Test
    public void testSendSessionMessageStream_snapshotLockUntilCommitted() {
        // mock sessionService 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(new Ai1SessionDO()
                .setId(10L).setAgentId(1L).setUserId(USER_ID).setTitle(Ai1SessionDO.TITLE_DEFAULT));
        // mock 释放快照锁时，本轮消息已提交
        List<Integer> committedCounts = new ArrayList<>();
        doAnswer(invocation -> {
            committedCounts.add(sessionMessageMapper.selectListBySessionId(10L).size());
            return null;
        }).when(snapshotLock).unlock();

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        // 断言：按 Agent、用户加锁，落库后才释放，再投递任务
        verify(redissonClient).getLock("ai1:memory:snapshot-lock:1:" + USER_ID);
        assertEquals(List.of(2), committedCounts);
        InOrder inOrder = inOrder(snapshotLock, sessionStreamTool);
        inOrder.verify(snapshotLock).lock();
        inOrder.verify(snapshotLock).unlock();
        inOrder.verify(sessionStreamTool).submit(anyLong(), eq(1L), eq(10L), eq("你好"));
    }

    @Test
    public void testStopSessionMessageMy_success() {
        // 调用
        sessionMessageService.stopSessionMessageMy(USER_ID, 10L);
        // 断言
        InOrder inOrder = inOrder(sessionService, sessionStreamTool);
        inOrder.verify(sessionService).validateSessionMy(USER_ID, 10L);
        inOrder.verify(sessionStreamTool).cancel(10L);
    }

    @Test
    public void testStopSessionMessageMy_notOwner() {
        // mock sessionService 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenThrow(exception(SESSION_NOT_EXISTS));

        // 调用，并断言异常
        assertServiceException(() -> sessionMessageService.stopSessionMessageMy(USER_ID, 10L), SESSION_NOT_EXISTS);
        // 断言
        verify(sessionStreamTool, never()).cancel(anyLong());
    }

    @Test
    public void testGetSessionMessagePage() {
        // mock 数据
        Ai1SessionMessageDO message1 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        Ai1SessionMessageDO message2 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());
        Ai1SessionMessageDO message3 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        insertSessionMessage(11L, Ai1SessionMessageRoleEnum.USER.getRole());
        // 准备参数
        Ai1SessionMessagePageReqVO reqVO = new Ai1SessionMessagePageReqVO().setSessionId(10L);
        reqVO.setPageSize(2);

        // 调用
        PageResult<Ai1SessionMessageDO> pageResult = sessionMessageService.getSessionMessagePage(reqVO);
        // 断言：取最新一页，页内按时间正序
        assertEquals(3, pageResult.getTotal());
        assertEquals(List.of(message2.getId(), message3.getId()),
                pageResult.getList().stream().map(Ai1SessionMessageDO::getId).toList());

        // 调用：第二页
        reqVO.setPageNo(2);
        pageResult = sessionMessageService.getSessionMessagePage(reqVO);
        // 断言：更早的消息
        assertEquals(3, pageResult.getTotal());
        assertEquals(List.of(message1.getId()),
                pageResult.getList().stream().map(Ai1SessionMessageDO::getId).toList());

        // 调用：超出范围的空页
        reqVO.setPageNo(3);
        pageResult = sessionMessageService.getSessionMessagePage(reqVO);
        // 断言
        assertEquals(3, pageResult.getTotal());
        assertTrue(pageResult.getList().isEmpty());
    }

    @Test
    public void testSendSessionMessageStream_submitFailed() {
        // mock sessionService 和 sessionStreamTool 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(new Ai1SessionDO()
                .setId(10L).setAgentId(1L).setTitle(Ai1SessionDO.TITLE_DEFAULT));
        doThrow(new RedisConnectionException("Redis 不可用")).when(sessionStreamTool)
                .submit(anyLong(), eq(1L), eq(10L), eq("你好"));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        // 断言用户消息保留、助手不再生成中
        List<Ai1SessionMessageDO> messages = sessionMessageMapper.selectListBySessionId(10L);
        assertEquals(2, messages.size());
        assertEquals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), messages.get(0).getStatus());
        assertEquals(Ai1SessionMessageStatusEnum.FAILED.getStatus(), messages.get(1).getStatus());
        assertEquals("", messages.get(1).getContent());
        verify(sessionStreamTool, never()).open(anyLong(), any());
        verify(sessionStreamTool).error("会话提交失败，请稍后重试");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testSendSessionMessageStream_submitTimeoutAfterWorkerCompleted(int status) {
        // mock sessionService 和 sessionStreamTool 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(new Ai1SessionDO()
                .setId(10L).setAgentId(1L).setTitle(Ai1SessionDO.TITLE_DEFAULT));
        // mock 投递实际成功，worker 在投递超时前已回填终态
        doAnswer(invocation -> {
            Long messageId = invocation.getArgument(0);
            sessionMessageMapper.updateById(new Ai1SessionMessageDO().setId(messageId)
                    .setStatus(status).setContent("worker 已回填").setReasoning("已有思考"));
            throw new RedisTimeoutException("投递响应丢失");
        }).when(sessionStreamTool).submit(anyLong(), eq(1L), eq(10L), eq("你好"));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        // 断言：补偿只更新生成中的消息，已有终态和内容不被覆盖
        Ai1SessionMessageDO assistant = sessionMessageMapper.selectListBySessionId(10L).get(1);
        assertEquals(status, assistant.getStatus());
        assertEquals("worker 已回填", assistant.getContent());
        assertEquals("已有思考", assistant.getReasoning());
        verify(sessionStreamTool, never()).open(anyLong(), any());
    }

    @Test
    public void testSendSessionMessageStream_openFailedKeepsGenerating() {
        // mock sessionService 和 sessionStreamTool 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(new Ai1SessionDO()
                .setId(10L).setAgentId(1L).setTitle(Ai1SessionDO.TITLE_DEFAULT));
        when(sessionStreamTool.open(anyLong(), isNull())).thenThrow(new IllegalStateException("SSE 不可用"));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        // 断言已投递任务不因 SSE 异常而被标失败
        Ai1SessionMessageDO assistant = sessionMessageMapper.selectListBySessionId(10L).get(1);
        assertEquals(Ai1SessionMessageStatusEnum.GENERATING.getStatus(), assistant.getStatus());
        verify(sessionStreamTool).submit(assistant.getId(), 1L, 10L, "你好");
        verify(sessionStreamTool).error("会话提交失败，请稍后重试");
    }

    @Test
    public void testSendSessionMessageStream_compensationFailedKeepsOriginalError() {
        // mock sessionService、sessionStreamTool 和 sessionMessageMapper 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(new Ai1SessionDO()
                .setId(10L).setAgentId(1L).setTitle(Ai1SessionDO.TITLE_DEFAULT));
        doThrow(exception(SESSION_NOT_EXISTS)).when(sessionStreamTool)
                .submit(anyLong(), eq(1L), eq(10L), eq("你好"));
        doThrow(new IllegalStateException("数据库不可用")).when(sessionMessageMapper)
                .update(any(Ai1SessionMessageDO.class), any(Wrapper.class));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        // 断言补偿异常不替换原投递业务异常
        verify(sessionStreamTool).error("会话不存在");
        verify(sessionStreamTool, never()).open(anyLong(), any());
        assertEquals(Ai1SessionMessageStatusEnum.GENERATING.getStatus(),
                sessionMessageMapper.selectListBySessionId(10L).get(1).getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = {47, 48, 49, 50, 51, 100})
    public void testCreateRoundMessages_titleLength(int length) {
        // 准备参数
        Ai1SessionDO session = new Ai1SessionDO().setId(10L).setTitle(Ai1SessionDO.TITLE_DEFAULT);
        String title = "问".repeat(length);

        // 调用
        sessionMessageService.createRoundMessages(session, "  " + title + "  ");
        // 断言
        verify(sessionService).updateSessionTitle(10L, length > 50 ? "问".repeat(47) + "..." : title);
    }

    @Test
    public void testCreateRoundMessages_existingMessagesKeepTitle() {
        // mock 数据
        insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        // 准备参数
        Ai1SessionDO session = new Ai1SessionDO().setId(10L).setTitle(Ai1SessionDO.TITLE_DEFAULT);

        // 调用
        sessionMessageService.createRoundMessages(session, "已有消息不覆盖标题");
        // 断言
        verify(sessionService, never()).updateSessionTitle(anyLong(), anyString());
    }

    @Test
    public void testSendSessionMessageStream_notOwner() {
        // mock sessionService 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenThrow(exception(SESSION_NOT_EXISTS));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID, new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        // 断言
        verify(sessionStreamTool).error("会话不存在");
        verify(sessionStreamTool, never()).submit(anyLong(), anyLong(), anyLong(), anyString());
        assertTrue(sessionMessageMapper.selectListBySessionId(10L).isEmpty());
    }

    @Test
    public void testResumeSessionMessageStream_otherUser() {
        // mock 数据
        Ai1SessionMessageDO message = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());
        // mock sessionService 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenThrow(exception(SESSION_NOT_EXISTS));

        // 调用
        sessionMessageService.resumeSessionMessageStream(USER_ID, message.getId(), null);
        // 断言
        verify(sessionStreamTool).error("会话不存在");
        verify(sessionStreamTool, never()).open(anyLong(), any());
    }

    @Test
    public void testResumeSessionMessageStream_userMessage() {
        // mock 数据
        Ai1SessionMessageDO message = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());

        // 调用
        sessionMessageService.resumeSessionMessageStream(USER_ID, message.getId(), null);
        // 断言
        verify(sessionStreamTool).error("会话消息不存在");
        verify(sessionStreamTool, never()).open(anyLong(), any());
    }

    @Test
    public void testResumeSessionMessageStream_success() {
        // mock 数据
        Ai1SessionMessageDO message = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());

        // 调用
        sessionMessageService.resumeSessionMessageStream(USER_ID, message.getId(), "1-0");
        // 断言
        verify(sessionService).validateSessionMy(USER_ID, 10L);
        verify(sessionStreamTool).open(message.getId(), "1-0");
    }

    @ParameterizedTest
    @ValueSource(strings = {"database", "ownership", "open"})
    public void testResumeSessionMessageStream_unexpectedException(String stage) {
        // mock 数据
        Ai1SessionMessageDO message = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());
        sessionMessageMapper.updateById(new Ai1SessionMessageDO().setId(message.getId())
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus()));
        // mock 数据库、归属校验或 SSE 打开异常
        IllegalStateException failure = new IllegalStateException("内部异常详情");
        switch (stage) {
            case "database" -> doThrow(failure).when(sessionMessageMapper).selectById(message.getId());
            case "ownership" -> when(sessionService.validateSessionMy(USER_ID, 10L)).thenThrow(failure);
            case "open" -> when(sessionStreamTool.open(message.getId(), "1-0")).thenThrow(failure);
        }
        SseEmitter emitter = new SseEmitter();
        when(sessionStreamTool.error("会话续传失败，请稍后重试")).thenReturn(emitter);

        // 调用
        SseEmitter result = sessionMessageService.resumeSessionMessageStream(USER_ID, message.getId(), "1-0");
        // 断言通过 SSE 返回安全提示，不改变服务端生成状态或重新投递任务
        assertSame(emitter, result);
        verify(sessionStreamTool).error("会话续传失败，请稍后重试");
        assertEquals(Ai1SessionMessageStatusEnum.GENERATING.getStatus(),
                sessionMessageMapper.selectListBySessionId(10L).get(0).getStatus());
        verify(sessionStreamTool, never()).submit(anyLong(), anyLong(), anyLong(), anyString());
    }

    @Test
    public void testGetSessionMessageListBySessionIdAndIdLessThan() {
        // mock 数据
        Ai1SessionMessageDO message1 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        Ai1SessionMessageDO message2 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());
        Ai1SessionMessageDO message3 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        Ai1SessionMessageDO message4 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());

        // 调用
        List<Ai1SessionMessageDO> list = sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(10L, message4.getId(), 2);
        // 断言
        assertEquals(List.of(message3.getId(), message2.getId()), list.stream().map(Ai1SessionMessageDO::getId).toList());
        assertNotEquals(message1.getId(), list.get(1).getId());
    }

    @Test
    public void testGetSessionMessageListBySessionIdsAndStatusAndIdBetween_safePrefix() {
        // mock 数据：同一用户两个会话，第一个会话的助手消息仍在生成
        String user = Ai1SessionMessageRoleEnum.USER.getRole();
        String assistant = Ai1SessionMessageRoleEnum.ASSISTANT.getRole();
        Ai1SessionMessageDO message1 = insertSessionMessage(10L, user);
        Ai1SessionMessageDO message2 = insertSessionMessage(10L, assistant, Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        Ai1SessionMessageDO message3 = insertSessionMessage(11L, user);
        insertSessionMessage(12L, user);
        insertSessionMessage(11L, assistant, Ai1SessionMessageStatusEnum.FAILED.getStatus());
        List<Long> sessionIds = List.of(10L, 11L);

        // 调用：只读取第一条生成中消息之前的已完成消息
        Ai1SessionMessageDO generating = sessionMessageService.getFirstSessionMessageBySessionIdsAndStatusAndIdGt(sessionIds,
                Ai1SessionMessageStatusEnum.GENERATING.getStatus(), 0L);
        List<Ai1SessionMessageDO> first = sessionMessageService.getSessionMessageListBySessionIdsAndStatusAndIdBetween(sessionIds,
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), 0L, generating.getId(), 100);
        // 断言
        assertEquals(message2.getId(), generating.getId());
        assertEquals(List.of(message1.getId()), first.stream().map(Ai1SessionMessageDO::getId).toList());

        // mock 生成结束
        sessionMessageMapper.updateById(new Ai1SessionMessageDO().setId(message2.getId())
                .setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus()));
        // 调用：从水位线之后读取，并按条数截断
        assertNull(sessionMessageService.getFirstSessionMessageBySessionIdsAndStatusAndIdGt(sessionIds,
                Ai1SessionMessageStatusEnum.GENERATING.getStatus(), message1.getId()));
        List<Ai1SessionMessageDO> limited = sessionMessageService.getSessionMessageListBySessionIdsAndStatusAndIdBetween(sessionIds,
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), message1.getId(), null, 1);
        List<Ai1SessionMessageDO> all = sessionMessageService.getSessionMessageListBySessionIdsAndStatusAndIdBetween(sessionIds,
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), message1.getId(), null, 100);
        // 断言
        assertEquals(List.of(message2.getId()), limited.stream().map(Ai1SessionMessageDO::getId).toList());
        assertEquals(List.of(message2.getId(), message3.getId()), all.stream().map(Ai1SessionMessageDO::getId).toList());
    }

    @Test
    public void testGetGeneratingAssistantMessageIdList() {
        // mock 数据：只有本会话生成中的助手消息命中
        insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        Ai1SessionMessageDO message1 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole(),
                Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());
        insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole(), Ai1SessionMessageStatusEnum.FAILED.getStatus());
        insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole(), Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        Ai1SessionMessageDO message2 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole(),
                Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        insertSessionMessage(11L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole(), Ai1SessionMessageStatusEnum.GENERATING.getStatus());

        // 调用
        List<Long> ids = sessionMessageService.getGeneratingAssistantMessageIdList(10L);
        // 断言：只返回本会话生成中的助手消息编号，按编号升序
        assertEquals(List.of(message1.getId(), message2.getId()), ids);
        assertTrue(sessionMessageService.getGeneratingAssistantMessageIdList(12L).isEmpty());
    }

    // ========== 测试数据 ==========

    private Ai1SessionMessageDO insertSessionMessage(Long sessionId, String role) {
        return insertSessionMessage(sessionId, role, Ai1SessionMessageStatusEnum.SUCCESS.getStatus());
    }

    private Ai1SessionMessageDO insertSessionMessage(Long sessionId, String role, Integer status) {
        Ai1SessionMessageDO message = new Ai1SessionMessageDO().setSessionId(sessionId).setRole(role).setContent("内容")
                .setStatus(status);
        sessionMessageMapper.insert(message);
        return message;
    }

}
