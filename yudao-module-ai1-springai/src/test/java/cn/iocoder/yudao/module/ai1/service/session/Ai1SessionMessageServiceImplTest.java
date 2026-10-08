package cn.iocoder.yudao.module.ai1.service.session;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1SessionMessageSendReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.session.Ai1SessionMessageMapper;
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
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.SESSION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1SessionMessageServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1SessionMessageServiceImpl.class)
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

    @BeforeEach
    public void before() {
        // 设置租户上下文
        TenantContextHolder.setTenantId(TENANT_ID);
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
    public void testSendSessionMessageStream_submitTimeoutKeepsWorkerResult(int status) {
        // mock sessionService 和 sessionStreamTool 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(new Ai1SessionDO()
                .setId(10L).setAgentId(1L).setTitle(Ai1SessionDO.TITLE_DEFAULT));
        doAnswer(invocation -> {
            Long messageId = invocation.getArgument(0);
            sessionMessageMapper.updateById(new Ai1SessionMessageDO().setId(messageId)
                    .setStatus(status).setContent("worker 已回填").setReasoning("已有思考"));
            throw new RedisTimeoutException("投递响应丢失");
        }).when(sessionStreamTool).submit(anyLong(), eq(1L), eq(10L), eq("你好"));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        // 断言已有终态和内容不被补偿覆盖
        Ai1SessionMessageDO assistant = sessionMessageMapper.selectListBySessionId(10L).get(1);
        assertEquals(status, assistant.getStatus());
        assertEquals("worker 已回填", assistant.getContent());
        assertEquals("已有思考", assistant.getReasoning());
        verify(sessionStreamTool, never()).open(anyLong(), any());
    }

    @Test
    public void testSendSessionMessageStream_workerCanCompleteAfterSubmitTimeout() {
        // mock sessionService 和 sessionStreamTool 的方法
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(new Ai1SessionDO()
                .setId(10L).setAgentId(1L).setTitle(Ai1SessionDO.TITLE_DEFAULT));
        doThrow(new RedisTimeoutException("投递响应丢失")).when(sessionStreamTool)
                .submit(anyLong(), eq(1L), eq(10L), eq("你好"));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));
        Ai1SessionMessageDO assistant = sessionMessageMapper.selectListBySessionId(10L).get(1);
        assertEquals(Ai1SessionMessageStatusEnum.FAILED.getStatus(), assistant.getStatus());
        // 调用 worker 回填，模拟 Redis 实际写入后响应丢失
        sessionMessageService.updateSessionMessage(new Ai1SessionMessageDO().setId(assistant.getId())
                .setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus()).setContent("稍后生成成功"));
        // 断言后续完成结果仍可落库
        Ai1SessionMessageDO result = sessionMessageMapper.selectById(assistant.getId());
        assertEquals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), result.getStatus());
        assertEquals("稍后生成成功", result.getContent());
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

    // ========== 测试数据 ==========

    private Ai1SessionMessageDO insertSessionMessage(Long sessionId, String role) {
        Ai1SessionMessageDO message = new Ai1SessionMessageDO().setSessionId(sessionId).setRole(role).setContent("内容")
                .setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus());
        sessionMessageMapper.insert(message);
        return message;
    }

}
