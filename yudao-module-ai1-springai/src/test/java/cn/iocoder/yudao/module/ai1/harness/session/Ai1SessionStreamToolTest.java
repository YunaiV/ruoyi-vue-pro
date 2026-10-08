package cn.iocoder.yudao.module.ai1.harness.session;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.harness.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionMessageService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.harness.mcp.Ai1McpToolFactory;
import cn.iocoder.yudao.module.ai1.harness.rag.Ai1RagTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.redisson.api.RStream;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.api.stream.StreamRangeArgs;
import org.redisson.api.stream.StreamReadArgs;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.redisson.client.codec.Codec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1SessionStreamTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1SessionStreamToolTest extends BaseMockitoUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final Long MESSAGE_ID = 1024L;
    private static final Long AGENT_ID = 1L;
    private static final Long SESSION_ID = 10L;
    private static final StreamMessageId RECORD_ID = new StreamMessageId(1, 0);

    @InjectMocks
    private Ai1SessionStreamTool sessionStreamTool;

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RStream<String, String> taskStream;
    @Mock
    private RStream<String, String> resultStream;
    @Mock
    private RLock taskLock;
    @Spy
    private YudaoAi1Properties ai1Properties = new YudaoAi1Properties();

    @Mock
    private Ai1SessionMessageService sessionMessageService;
    @Mock
    private Ai1SessionService sessionService;
    @Mock
    private Ai1AgentService agentService;
    @Mock
    private Ai1ModelService modelService;
    @Mock
    private Ai1KnowledgeBaseService knowledgeBaseService;
    @Mock
    private Ai1LlmChatTool llmChatTool;
    @Mock
    private Ai1McpToolFactory mcpToolFactory;
    @Mock
    private Ai1SkillToolFactory skillToolFactory;
    @Mock
    private Ai1RagTool ragTool;

    @BeforeEach
    public void before() {
        // mock redissonClient 的方法（部分用例不访问 Redis）
        lenient().doReturn(taskStream).when(redissonClient).getStream(eq("ai1:session:tasks"), any(Codec.class));
        lenient().doReturn(resultStream).when(redissonClient).getStream(eq("ai1:session:result:" + MESSAGE_ID), any(Codec.class));
        lenient().when(redissonClient.getLock("ai1:session:task-lock:" + MESSAGE_ID)).thenReturn(taskLock);
        lenient().when(taskLock.tryLock()).thenReturn(true);
        lenient().when(taskLock.isHeldByThread(anyLong())).thenReturn(true);
        lenient().when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO()
                .setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus()));
    }

    @Test
    public void testStreamResult_disconnectedMustNotComplete() throws Exception {
        SseEmitter emitter = mock(
                SseEmitter.class);
        doThrow(new IOException("client disconnected")).when(emitter).send(
                any(SseEmitter.SseEventBuilder.class));

        Boolean shouldComplete = ReflectionTestUtils.invokeMethod(
                sessionStreamTool, "streamResult", emitter, MESSAGE_ID, null, TENANT_ID);

        assertEquals(Boolean.FALSE, shouldComplete);
        verifyNoInteractions(resultStream);
        verify(emitter, never()).complete();
    }

    @Test
    public void testStreamResult_terminalMustComplete() {
        SseEmitter emitter = mock(
                SseEmitter.class);
        when(resultStream.read(any(StreamReadArgs.class)))
                .thenReturn(Map.of(RECORD_ID, Map.of("type", "done", "data", "")));

        Boolean shouldComplete = ReflectionTestUtils.invokeMethod(
                sessionStreamTool, "streamResult", emitter, MESSAGE_ID, null, TENANT_ID);

        assertEquals(Boolean.TRUE, shouldComplete);
    }

    @Test
    public void testSendErrorAndComplete_disconnectedMustNotComplete() throws Exception {
        SseEmitter emitter = mock(
                SseEmitter.class);
        doThrow(new IOException("client disconnected")).when(emitter).send(
                any(SseEmitter.SseEventBuilder.class));

        ReflectionTestUtils.invokeMethod(
                Ai1SessionStreamTool.class, "sendErrorAndComplete", emitter, "error");

        verify(emitter, never()).complete();
    }

    @Test
    public void testFailLostMessage_generating() {
        // mock 方法
        AtomicReference<Long> tenantRef = new AtomicReference<>();
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenAnswer(invocation -> {
            tenantRef.set(TenantContextHolder.getTenantId());
            return new Ai1SessionMessageDO().setId(MESSAGE_ID).setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        });

        // 调用
        sessionStreamTool.failLostMessage(MESSAGE_ID, TENANT_ID);
        // 断言
        assertEquals(TENANT_ID, tenantRef.get());
        verify(sessionMessageService).updateSessionMessage(argThat(message -> MESSAGE_ID.equals(message.getId())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
    }

    @Test
    public void testFailLostMessage_finished() {
        // mock sessionMessageService 的方法
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(
                new Ai1SessionMessageDO().setId(MESSAGE_ID).setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus()));

        // 调用
        sessionStreamTool.failLostMessage(MESSAGE_ID, TENANT_ID);
        // 断言
        verify(sessionMessageService, never()).updateSessionMessage(any());
    }

    @Test
    public void testHandleTask_withoutTenant() {
        // mock 方法
        AtomicReference<Long> tenantIdInGenerate = new AtomicReference<>(-1L);
        when(agentService.validateAgentExists(AGENT_ID)).thenAnswer(invocation -> {
            tenantIdInGenerate.set(TenantContextHolder.getTenantId());
            return new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L);
        });
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(eq(SESSION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
        when(llmChatTool.chat(any(), eq("你是 客服 的智能助手。"), anyList(), eq("你好"), anyList(), anyList(), eq(SESSION_ID), any(), any()))
                .thenReturn(new Ai1LlmChatTool.ChatText("您好", ""));
        // 准备参数
        Map<String, String> fields = buildTaskFields();
        fields.remove("tenantId");

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, fields);
        // 断言
        assertNull(tenantIdInGenerate.get());
        ArgumentCaptor<Ai1SessionMessageDO> messageCaptor = ArgumentCaptor.forClass(Ai1SessionMessageDO.class);
        verify(sessionMessageService).updateSessionMessage(messageCaptor.capture());
        assertEquals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), messageCaptor.getValue().getStatus());
        assertEquals(List.of("ping", "done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
    }

    @Test
    public void testHandleTask_generateInTaskTenant() {
        // mock 方法
        AtomicReference<Long> tenantIdInGenerate = new AtomicReference<>();
        when(agentService.validateAgentExists(AGENT_ID)).thenAnswer(invocation -> {
            tenantIdInGenerate.set(TenantContextHolder.getTenantId());
            return new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L);
        });
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(eq(SESSION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
        when(llmChatTool.chat(any(), anyString(), anyList(), eq("你好"), anyList(), anyList(), eq(SESSION_ID), any(), any()))
                .thenAnswer(invocation -> {
                    Consumer<String> onContent = invocation.getArgument(8);
                    onContent.accept("您好");
                    return new Ai1LlmChatTool.ChatText("您好", "");
                });

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言
        assertEquals(TENANT_ID, tenantIdInGenerate.get());
        assertNull(TenantContextHolder.getTenantId());
        ArgumentCaptor<Ai1SessionMessageDO> messageCaptor = ArgumentCaptor.forClass(Ai1SessionMessageDO.class);
        verify(sessionMessageService).updateSessionMessage(messageCaptor.capture());
        assertEquals(MESSAGE_ID, messageCaptor.getValue().getId());
        assertEquals("您好", messageCaptor.getValue().getContent());
        assertNull(messageCaptor.getValue().getReasoning());
        assertEquals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), messageCaptor.getValue().getStatus());
        verify(sessionService).touchSession(SESSION_ID);
        assertEquals(List.of("ping", "message", "done"), captureResultTypes());
    }

    @Test
    public void testHandleTask_generateFailed() {
        // mock agentService 的方法
        when(agentService.validateAgentExists(AGENT_ID)).thenThrow(new IllegalStateException("Agent 不存在"));

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言
        ArgumentCaptor<Ai1SessionMessageDO> messageCaptor = ArgumentCaptor.forClass(Ai1SessionMessageDO.class);
        verify(sessionMessageService).updateSessionMessage(messageCaptor.capture());
        assertEquals(Ai1SessionMessageStatusEnum.FAILED.getStatus(), messageCaptor.getValue().getStatus());
        assertEquals(List.of("ping", "error", "done"), captureResultTypes());
    }

    @Test
    public void testHandleTask_locked() {
        // mock taskLock 的方法
        when(taskLock.tryLock()).thenReturn(false);

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言
        verifyNoInteractions(sessionMessageService, llmChatTool, resultStream, taskStream);
        verify(taskLock, never()).unlock();
    }

    @Test
    public void testHandleTask_finished() {
        // mock sessionMessageService 的方法
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO()
                .setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus()));

        when(resultStream.rangeReversed(StreamRangeArgs.startId(StreamMessageId.MAX).endId(StreamMessageId.MIN).count(1)))
                .thenReturn(Map.of(RECORD_ID, Map.of("type", "done", "data", "")));

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言
        verifyNoInteractions(llmChatTool);
        verify(resultStream, never()).add(any());
        verify(sessionMessageService, never()).updateSessionMessage(any());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
        verify(taskLock).unlock();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testHandleTask_reclaimedKeepsPartialResult(boolean reclaimed) {
        // mock resultStream 的方法
        Map<StreamMessageId, Map<String, String>> results = new LinkedHashMap<>();
        results.put(new StreamMessageId(1, 0), Map.of("type", "thinking", "data", "思考"));
        results.put(new StreamMessageId(1, 1), Map.of("type", "message", "data", "第一段"));
        results.put(new StreamMessageId(1, 2), Map.of("type", "message", "data", "第二段"));
        when(resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))).thenReturn(results);
        lenient().when(resultStream.isExists()).thenReturn(true);

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), reclaimed);
        // 断言
        verifyNoInteractions(llmChatTool, agentService);
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "第一段第二段".equals(message.getContent())
                && "思考".equals(message.getReasoning())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        assertEquals(List.of("error", "done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
    }

    @Test
    public void testHandleTask_reclaimedWithoutResult() {
        // mock resultStream 的方法
        when(resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))).thenReturn(Map.of());

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言
        verifyNoInteractions(llmChatTool);
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "".equals(message.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        assertEquals(List.of("error", "done"), captureResultTypes());
    }

    @Test
    public void testHandleTask_resultReadFailedKeepsPending() {
        // mock resultStream 的方法
        when(resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))).thenThrow(new IllegalStateException("Redis 不可用"));

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言
        verifyNoInteractions(llmChatTool);
        verify(sessionMessageService, never()).updateSessionMessage(any());
        verify(taskStream, never()).ack(anyString(), any(StreamMessageId.class));
        verify(resultStream, never()).add(any());
        verify(taskLock).unlock();
    }

    @Test
    public void testHandleTask_lockLostDuringGenerate() {
        // mock 方法
        when(agentService.validateAgentExists(AGENT_ID)).thenReturn(
                new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L));
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(eq(SESSION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
        when(llmChatTool.chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any()))
                .thenAnswer(invocation -> {
                    when(taskLock.isHeldByThread(anyLong())).thenReturn(false);
                    Consumer<String> onContent = invocation.getArgument(8);
                    onContent.accept("旧 worker 输出");
                    return new Ai1LlmChatTool.ChatText("旧 worker 输出", "");
                });

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言
        assertEquals(List.of("ping"), captureResultTypes());
        verify(sessionMessageService, never()).updateSessionMessage(any());
        verify(taskStream, never()).ack(anyString(), any(StreamMessageId.class));
        verify(taskLock, never()).unlock();
    }

    @Test
    public void testHandleTask_saveFailedKeepsPending() {
        // mock 方法
        when(resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))).thenReturn(Map.of());
        doThrow(new IllegalStateException("数据库不可用")).when(sessionMessageService).updateSessionMessage(any());

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言
        verifyNoInteractions(llmChatTool);
        verify(taskStream, never()).ack(anyString(), any(StreamMessageId.class));
        verify(resultStream, never()).add(any());
        verify(taskLock).unlock();
    }

    @Test
    public void testHandleTask_ackFailedDoesNotRegenerate() {
        // mock 方法
        Ai1SessionMessageDO message = new Ai1SessionMessageDO().setId(MESSAGE_ID)
                .setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(message);
        doAnswer(invocation -> {
            message.setStatus(invocation.<Ai1SessionMessageDO>getArgument(0).getStatus());
            return null;
        }).when(sessionMessageService).updateSessionMessage(any());
        when(agentService.validateAgentExists(AGENT_ID)).thenReturn(
                new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L));
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(eq(SESSION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
        when(llmChatTool.chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any()))
                .thenReturn(new Ai1LlmChatTool.ChatText("回复", ""));
        when(taskStream.ack("ai1-session-workers", RECORD_ID)).thenThrow(new IllegalStateException("ACK 失败"))
                .thenReturn(1L);
        when(resultStream.rangeReversed(StreamRangeArgs.startId(StreamMessageId.MAX).endId(StreamMessageId.MIN).count(1)))
                .thenReturn(Map.of(RECORD_ID, Map.of("type", "done", "data", "")));

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言
        verify(llmChatTool).chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any());
        verify(sessionMessageService).updateSessionMessage(any());
        assertEquals(List.of("ping", "done"), captureResultTypes());
        verify(taskStream, times(2)).ack("ai1-session-workers", RECORD_ID);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testHandleTask_finishedWithoutTerminal(Integer status) {
        // mock 方法
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO()
                .setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole()).setStatus(status));
        when(resultStream.rangeReversed(StreamRangeArgs.startId(StreamMessageId.MAX).endId(StreamMessageId.MIN).count(1))).thenReturn(Map.of());

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言
        verifyNoInteractions(llmChatTool);
        verify(sessionMessageService, never()).updateSessionMessage(any());
        assertEquals(status.equals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus())
                ? List.of("done") : List.of("error", "done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
    }

    // ========== 测试数据 ==========

    private static Map<String, String> buildTaskFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("tenantId", String.valueOf(TENANT_ID));
        fields.put("messageId", String.valueOf(MESSAGE_ID));
        fields.put("agentId", String.valueOf(AGENT_ID));
        fields.put("sessionId", String.valueOf(SESSION_ID));
        fields.put("content", "你好");
        return fields;
    }

    // ========== 断言工具 ==========

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> captureResultTypes() {
        ArgumentCaptor<StreamAddArgs> captor = ArgumentCaptor.forClass(StreamAddArgs.class);
        verify(resultStream, atLeastOnce()).add(captor.capture());
        List<String> types = new ArrayList<>();
        for (StreamAddArgs args : captor.getAllValues()) {
            // StreamAddArgs 没有读取方法，通过 entries 字段获取事件类型
            types.add(String.valueOf(readEntries(args).get("type")));
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> readEntries(StreamAddArgs<String, String> args) {
        try {
            java.lang.reflect.Field field = args.getClass().getDeclaredField("entries");
            field.setAccessible(true);
            return (Map<String, String>) field.get(args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

}
