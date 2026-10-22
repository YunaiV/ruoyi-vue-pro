package cn.iocoder.yudao.module.ai1.harness.session;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.harness.mcp.Ai1McpToolFactory;
import cn.iocoder.yudao.module.ai1.harness.memory.Ai1MemoryTool;
import cn.iocoder.yudao.module.ai1.harness.rag.Ai1RagTool;
import cn.iocoder.yudao.module.ai1.harness.skill.Ai1SkillShellTracker;
import cn.iocoder.yudao.module.ai1.harness.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionMessageService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RStream;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamCreateGroupArgs;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.api.stream.StreamRangeArgs;
import org.redisson.api.stream.StreamReadArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.springaicommunity.agent.common.exec.ExecBackend;
import org.springaicommunity.agent.common.exec.ExecHandle;
import org.springaicommunity.agent.common.exec.ExecSpec;
import org.springaicommunity.agent.exec.LocalExecBackend;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    @Mock
    private RBucket<String> stopBucket;
    @Mock
    private RTopic stopTopic;
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
    @Mock
    private Ai1MemoryTool memoryTool;
    @Mock
    private Ai1SkillShellTracker shellTracker;

    @BeforeEach
    public void before() {
        // 直接调用 handleTask 的用例按运行中处理，与 start 之后一致
        ReflectionTestUtils.setField(sessionStreamTool, "running", true);
        // mock redissonClient 的方法（部分用例不访问 Redis）
        lenient().doReturn(taskStream).when(redissonClient).getStream(eq("ai1:session:tasks"), any(Codec.class));
        lenient().doReturn(resultStream).when(redissonClient).getStream(eq("ai1:session:result:" + MESSAGE_ID), any(Codec.class));
        lenient().when(redissonClient.getLock("ai1:session:task-lock:" + MESSAGE_ID)).thenReturn(taskLock);
        lenient().when(taskLock.tryLock()).thenReturn(true);
        lenient().when(taskLock.isHeldByThread(anyLong())).thenReturn(true);
        lenient().doReturn(stopBucket).when(redissonClient).getBucket(eq("ai1:session:stop:" + MESSAGE_ID), any(Codec.class));
        lenient().when(redissonClient.getTopic(eq("ai1:session:stop"), any(Codec.class))).thenReturn(stopTopic);
        lenient().when(skillToolFactory.newShellTracker(AGENT_ID)).thenReturn(shellTracker);
        lenient().when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO()
                .setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus()));
    }

    @Test
    public void testStreamResult_disconnectedMustNotComplete() throws Exception {
        // 准备参数
        SseEmitter emitter = mock(SseEmitter.class);
        // mock 连接断开
        doThrow(new IOException("client disconnected")).when(emitter).send(
                any(SseEmitter.SseEventBuilder.class));

        // 调用
        Boolean shouldComplete = ReflectionTestUtils.invokeMethod(
                sessionStreamTool, "streamResult", emitter, MESSAGE_ID, null, TENANT_ID);

        // 断言
        assertEquals(Boolean.FALSE, shouldComplete);
        verifyNoInteractions(resultStream);
        verify(emitter, never()).complete();
    }

    @Test
    public void testStreamResult_terminalMustComplete() {
        // 准备参数
        SseEmitter emitter = mock(SseEmitter.class);
        // mock 结果流
        when(resultStream.read(any(StreamReadArgs.class)))
                .thenReturn(Map.of(RECORD_ID, Map.of("type", "done", "data", "")));

        // 调用
        Boolean shouldComplete = ReflectionTestUtils.invokeMethod(
                sessionStreamTool, "streamResult", emitter, MESSAGE_ID, null, TENANT_ID);

        // 断言
        assertEquals(Boolean.TRUE, shouldComplete);
    }

    @Test
    public void testSendErrorAndComplete_disconnectedMustNotComplete() throws Exception {
        // 准备参数
        SseEmitter emitter = mock(SseEmitter.class);
        // mock 连接断开
        doThrow(new IOException("client disconnected")).when(emitter).send(
                any(SseEmitter.SseEventBuilder.class));

        // 调用
        ReflectionTestUtils.invokeMethod(
                Ai1SessionStreamTool.class, "sendErrorAndComplete", emitter, "error");

        // 断言
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
        // 断言：正文追加固定失败提示，不拼接原始异常
        ArgumentCaptor<Ai1SessionMessageDO> messageCaptor = ArgumentCaptor.forClass(Ai1SessionMessageDO.class);
        verify(sessionMessageService).updateSessionMessage(messageCaptor.capture());
        assertEquals(Ai1SessionMessageStatusEnum.FAILED.getStatus(), messageCaptor.getValue().getStatus());
        assertEquals("\n\n> 本条回复生成失败", messageCaptor.getValue().getContent());
        assertEquals(List.of("ping", "error", "done"), captureResultTypes());
        assertEquals(List.of("", "本条回复生成失败", ""), captureResultData());
        verify(shellTracker).cancel();
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
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "第一段第二段\n\n> 生成任务已中断，请重新发送".equals(message.getContent())
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
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "\n\n> 生成任务已中断，请重新发送".equals(message.getContent())
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

    @Test
    public void testHandleTask_terminatedWithoutTerminal() {
        // mock 方法：人工终止已回填，done 尚未写入
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO()
                .setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.FAILED.getStatus()).setContent("已生成\n\n> 本条回复已被人工终止"));
        when(resultStream.rangeReversed(StreamRangeArgs.startId(StreamMessageId.MAX).endId(StreamMessageId.MIN).count(1))).thenReturn(Map.of());

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言：人工终止不补写 error
        verifyNoInteractions(llmChatTool);
        verify(sessionMessageService, never()).updateSessionMessage(any());
        assertEquals(List.of("done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
    }

    @Test
    public void testHandleTask_generateFailedWithoutTerminal() {
        // mock 方法：生成失败已回填，error 尚未写入
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO()
                .setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.FAILED.getStatus()).setContent("\n\n> 本条回复生成失败"));
        when(resultStream.rangeReversed(StreamRangeArgs.startId(StreamMessageId.MAX).endId(StreamMessageId.MIN).count(1))).thenReturn(Map.of());

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields(), true);
        // 断言：按已回填的固定提示补写，不改为中断
        assertEquals(List.of("error", "done"), captureResultTypes());
        assertEquals(List.of("本条回复生成失败", ""), captureResultData());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
    }

    @Test
    public void testHandleTask_failedBeforeStart() {
        // mock 方法：投递超时后补偿已标记失败，worker 才开始处理
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO()
                .setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.FAILED.getStatus()).setContent(""));
        when(resultStream.rangeReversed(StreamRangeArgs.startId(StreamMessageId.MAX).endId(StreamMessageId.MIN).count(1))).thenReturn(Map.of());

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言：不生成、不改写消息，补齐终态后确认
        verifyNoInteractions(agentService, llmChatTool, mcpToolFactory, skillToolFactory);
        verify(sessionMessageService, never()).updateSessionMessage(any());
        assertEquals(List.of("error", "done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
    }

    @Test
    public void testHandleTask_injectMemory() {
        // mock 方法
        mockGenerate(new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L).setMemoryEnabled(true));
        when(sessionService.getSession(SESSION_ID)).thenReturn(new Ai1SessionDO().setId(SESSION_ID).setUserId(100L));
        when(memoryTool.loadMemory(AGENT_ID, 100L)).thenReturn("## 用户画像\n- 程序员");
        when(memoryTool.mergePrompt("你是 客服 的智能助手。", "## 用户画像\n- 程序员")).thenReturn("合并后的系统指令");
        when(llmChatTool.chat(any(), eq("合并后的系统指令"), anyList(), eq("你好"), anyList(), anyList(), eq(SESSION_ID), any(), any()))
                .thenReturn(new Ai1LlmChatTool.ChatText("您好", ""));

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言
        verify(sessionMessageService).updateSessionMessage(argThat(message ->
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus().equals(message.getStatus())));
        verify(skillToolFactory).buildTools(any(), same(shellTracker));
    }

    @Test
    public void testHandleTask_injectMemoryFailed() {
        // mock 方法：记忆查询失败时降级为原系统指令
        mockGenerate(new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L).setMemoryEnabled(true));
        when(sessionService.getSession(SESSION_ID)).thenThrow(new IllegalStateException("数据库不可用"));
        when(llmChatTool.chat(any(), eq("你是 客服 的智能助手。"), anyList(), eq("你好"), anyList(), anyList(), eq(SESSION_ID), any(), any()))
                .thenReturn(new Ai1LlmChatTool.ChatText("您好", ""));

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言
        verifyNoInteractions(memoryTool);
        verify(sessionMessageService).updateSessionMessage(argThat(message ->
                Ai1SessionMessageStatusEnum.SUCCESS.getStatus().equals(message.getStatus())));
    }

    @Test
    public void testHandleTask_stoppedWhileQueued() {
        // mock 排队期间已写入停止意图
        when(stopBucket.isExists()).thenReturn(true);

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言：不调用模型，人工终止只写 done，并清除中断标志
        verifyNoInteractions(agentService, llmChatTool);
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "\n\n> 本条回复已被人工终止".equals(message.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        assertEquals(List.of("done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    public void testHandleTask_stoppedDuringPreparation() {
        // mock 方法：装配工具期间收到停止通知
        mockGenerate(new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L));
        when(mcpToolFactory.buildTools(any())).thenAnswer(invocation -> {
            ReflectionTestUtils.invokeMethod(sessionStreamTool, "cancelRunContext", MESSAGE_ID);
            return List.of();
        });

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言：不再调用模型
        verifyNoInteractions(llmChatTool);
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "\n\n> 本条回复已被人工终止".equals(message.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        assertEquals(List.of("ping", "done"), captureResultTypes());
        verify(shellTracker, atLeastOnce()).cancel();
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    public void testHandleTask_stoppedDuringGenerate() {
        // mock 方法：输出第一段后停止，之后的增量不再写入
        mockGenerate(new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L));
        when(llmChatTool.chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any()))
                .thenAnswer(invocation -> {
                    Consumer<String> onContent = invocation.getArgument(8);
                    onContent.accept("第一段");
                    ReflectionTestUtils.invokeMethod(sessionStreamTool, "cancelRunContext", MESSAGE_ID);
                    onContent.accept("第二段");
                    return new Ai1LlmChatTool.ChatText("第一段第二段", "");
                });

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言：保留已生成内容，只提交一个终态
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "第一段\n\n> 本条回复已被人工终止".equals(message.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        assertEquals(List.of("ping", "message", "done"), captureResultTypes());
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    public void testStop_interruptRunning() {
        // mock 方法：生成期间节点停机
        mockGenerate(new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L));
        when(llmChatTool.chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any()))
                .thenAnswer(invocation -> {
                    sessionStreamTool.stop();
                    return new Ai1LlmChatTool.ChatText("部分", "");
                });

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言：保留内容并标记中断，另写 error，确认后不会被重新生成
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "部分\n\n> 生成任务已中断，请重新发送".equals(message.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        assertEquals(List.of("ping", "error", "done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
    }

    @Test
    public void testHandleTask_stoppedBeforeRegister() {
        // mock 方法：查询消息后、登记上下文前节点停机
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenAnswer(invocation -> {
            sessionStreamTool.stop();
            return new Ai1SessionMessageDO().setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                    .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        });

        // 调用
        sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
        // 断言：不调用模型、不装配工具，按中断收尾并确认
        verifyNoInteractions(agentService, llmChatTool, mcpToolFactory);
        verify(stopBucket, never()).isExists();
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "\n\n> 生成任务已中断，请重新发送".equals(message.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        assertEquals(List.of("error", "done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
        assertTrue(getRunContexts().isEmpty());
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    public void testConsumeLoop_stoppedAfterRead() {
        // mock 方法：阻塞读取返回任务时，节点已停机
        when(taskStream.readGroup(anyString(), anyString(), any(StreamReadGroupArgs.class))).thenAnswer(invocation -> {
            sessionStreamTool.stop();
            return Map.of(RECORD_ID, buildTaskFields());
        });

        // 调用
        ReflectionTestUtils.invokeMethod(sessionStreamTool, "consumeLoop", "worker-test");
        // 断言：不调用模型，按中断收尾后退出循环
        verifyNoInteractions(agentService, llmChatTool);
        assertEquals(List.of("error", "done"), captureResultTypes());
        verify(taskStream).ack("ai1-session-workers", RECORD_ID);
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    public void testCancel_queued() {
        // mock 方法：会话中有一条生成中的助手消息
        when(sessionMessageService.getGeneratingAssistantMessageIdList(SESSION_ID)).thenReturn(List.of(MESSAGE_ID));
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(new Ai1SessionMessageDO().setId(MESSAGE_ID)
                .setSessionId(SESSION_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus()));
        when(resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))).thenReturn(
                Map.of(RECORD_ID, Map.of("type", "message", "data", "已生成")));
        when(taskLock.isHeldByCurrentThread()).thenReturn(true);

        // 调用
        sessionStreamTool.cancel(SESSION_ID);
        // 断言：写入停止意图并广播
        verify(stopBucket).set("1", Duration.ofSeconds(3600));
        verify(stopTopic).publish(String.valueOf(MESSAGE_ID));
        verify(redissonClient, times(1)).getBucket(anyString(), any(Codec.class));
        // 断言：没有 worker 执行，直接收尾
        verify(sessionMessageService).updateSessionMessage(argThat(message -> "已生成\n\n> 本条回复已被人工终止".equals(message.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(message.getStatus())));
        verify(sessionService).touchSession(SESSION_ID);
        assertEquals(List.of("done"), captureResultTypes());
        verify(taskLock).unlock();
        verify(sessionMessageService, never()).getSessionMessageListBySessionId(any());
    }

    @Test
    public void testCancel_running() {
        // mock 方法：任务正在其他 worker 执行
        when(sessionMessageService.getGeneratingAssistantMessageIdList(SESSION_ID)).thenReturn(List.of(MESSAGE_ID));
        when(taskLock.tryLock()).thenReturn(false);

        // 调用
        sessionStreamTool.cancel(SESSION_ID);
        // 断言：只写意图与通知，由执行节点收尾
        verify(stopBucket).set("1", Duration.ofSeconds(3600));
        verify(stopTopic).publish(String.valueOf(MESSAGE_ID));
        verify(sessionMessageService, never()).updateSessionMessage(any());
        verify(resultStream, never()).add(any());
    }

    @Test
    public void testCancel_noGenerating() {
        // mock 方法
        when(sessionMessageService.getGeneratingAssistantMessageIdList(SESSION_ID)).thenReturn(List.of());

        // 调用
        sessionStreamTool.cancel(SESSION_ID);
        // 断言：没有生成中的消息时无副作用
        verifyNoInteractions(stopBucket, stopTopic, taskLock);
    }

    @Test
    @Timeout(30)
    public void testCheckStopRequests() throws Exception {
        // 准备参数
        Ai1SessionStreamTool.RunContext context = new Ai1SessionStreamTool.RunContext(new Thread(() -> {}), shellTracker, getReapers());
        Map<Long, Ai1SessionStreamTool.RunContext> runContexts = getRunContexts();
        runContexts.put(MESSAGE_ID, context);
        // mock 订阅断开期间写入的停止意图；进程回收一直阻塞
        when(stopBucket.isExists()).thenReturn(true);
        CountDownLatch killing = new CountDownLatch(1);
        CountDownLatch releaseKill = new CountDownLatch(1);
        doAnswer(invocation -> {
            killing.countDown();
            releaseKill.await(10, TimeUnit.SECONDS);
            return null;
        }).when(shellTracker).cancel();

        try {
            // 调用：回收阻塞时检查线程仍立即返回
            ReflectionTestUtils.invokeMethod(sessionStreamTool, "checkStopRequests");
            // 断言：回收在独立线程执行
            assertTrue(killing.await(5, TimeUnit.SECONDS));
            assertEquals(Ai1SessionStreamTool.TERMINATED_NOTICE, context.finish());
        } finally {
            releaseKill.countDown();
            awaitReapers();
        }
    }

    @Test
    @Timeout(30)
    public void testStop_slowReapNotBlockOthers() throws Exception {
        // 准备参数：两个执行中的生成，第一个的进程回收一直阻塞
        Ai1SkillShellTracker slowTracker = mock(Ai1SkillShellTracker.class);
        Ai1SkillShellTracker fastTracker = mock(Ai1SkillShellTracker.class);
        CountDownLatch killing = new CountDownLatch(1);
        CountDownLatch releaseKill = new CountDownLatch(1);
        CountDownLatch fastReaped = new CountDownLatch(1);
        doAnswer(invocation -> {
            killing.countDown();
            releaseKill.await(10, TimeUnit.SECONDS);
            return null;
        }).when(slowTracker).cancel();
        doAnswer(invocation -> {
            fastReaped.countDown();
            return null;
        }).when(fastTracker).cancel();
        CountDownLatch releaseWorker = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(2);
        Thread worker1 = startWorker(releaseWorker, interrupted);
        Thread worker2 = startWorker(releaseWorker, interrupted);
        getRunContexts().put(1L, new Ai1SessionStreamTool.RunContext(worker1, slowTracker, getReapers()));
        getRunContexts().put(2L, new Ai1SessionStreamTool.RunContext(worker2, fastTracker, getReapers()));
        Thread stopping = new Thread(sessionStreamTool::stop);

        try {
            // 调用
            stopping.start();
            // 断言：慢回收进行中，两个生成都被中断，另一个回收不受影响；stop 等待回收结束
            assertTrue(killing.await(5, TimeUnit.SECONDS));
            assertTrue(interrupted.await(5, TimeUnit.SECONDS));
            assertTrue(fastReaped.await(5, TimeUnit.SECONDS));
            assertTrue(stopping.isAlive());
        } finally {
            releaseKill.countDown();
            releaseWorker.countDown();
            stopping.join(10_000);
            worker1.join(5000);
            worker2.join(5000);
        }
        assertFalse(stopping.isAlive());
    }

    @Test
    @Timeout(30)
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void testRunContext_cancelReapsWhileWorkerIgnoresInterrupt(@TempDir Path workDir) throws Exception {
        // 准备参数：worker 吞掉中断，模拟模型或工具库不响应中断；Shell 写入自身 PID 后 exec 为 sleep
        CountDownLatch releaseWorker = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            while (releaseWorker.getCount() > 0) {
                try {
                    releaseWorker.await();
                } catch (InterruptedException ignored) {
                    // 忽略中断，继续等待
                }
            }
        });
        worker.start();
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(LocalExecBackend.builder().workingDirectory(workDir).build());
        Ai1SessionStreamTool.RunContext context = new Ai1SessionStreamTool.RunContext(worker, tracker, getReapers());
        ExecHandle handle = tracker.start(ExecSpec.of("echo $$ > pid.tmp && mv pid.tmp pid && exec sleep 60"));
        ProcessHandle process = null;
        try {
            process = awaitDescendant(workDir.resolve("pid"));
            assertTrue(process.isAlive());

            // 调用
            context.cancel(Ai1SessionStreamTool.TERMINATED_NOTICE);
            // 断言：worker 仍未返回，在册的后台进程已被回收
            process.onExit().get(10, TimeUnit.SECONDS);
            assertFalse(process.isAlive());
            assertFalse(handle.isAlive());
            assertTrue(worker.isAlive());
        } finally {
            // 只清理本用例启动的线程、进程
            releaseWorker.countDown();
            worker.join(5000);
            awaitReapers();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (handle.isAlive()) {
                handle.kill();
            }
        }
    }

    @Test
    public void testRunContext_cancelThenFinish() throws Exception {
        // 准备参数
        Ai1SessionStreamTool.RunContext context = new Ai1SessionStreamTool.RunContext(Thread.currentThread(), shellTracker, getReapers());

        // 调用：重复停止只记录第一次
        context.cancel(Ai1SessionStreamTool.TERMINATED_NOTICE);
        context.cancel(Ai1SessionStreamTool.INTERRUPTED_MESSAGE);
        // 断言
        assertTrue(Thread.currentThread().isInterrupted());
        assertThrows(CancellationException.class, context::checkActive);
        assertEquals(Ai1SessionStreamTool.TERMINATED_NOTICE, context.finish());
        assertFalse(Thread.currentThread().isInterrupted());
        // 断言：只启动一次回收
        awaitReapers();
        verify(shellTracker, times(1)).cancel();
    }

    @Test
    public void testRunContext_lateCancel() {
        // 准备参数
        Ai1SessionStreamTool.RunContext context = new Ai1SessionStreamTool.RunContext(Thread.currentThread(), shellTracker, getReapers());
        assertNull(context.finish());

        // 调用：结束后迟到的停止
        context.cancel(Ai1SessionStreamTool.TERMINATED_NOTICE);
        // 断言：不中断 worker 处理下一个任务，也不启动回收
        assertFalse(Thread.currentThread().isInterrupted());
        assertTrue(getReapers().isEmpty());
        verifyNoInteractions(shellTracker);
    }

    @Test
    @Timeout(30)
    public void testStop_waitReaperAfterWorkerExit() throws Exception {
        // mock 方法：生成中启动后台进程，人工停止后进程回收一直阻塞
        ExecBackend backend = mock(ExecBackend.class);
        ExecHandle handle = mock(ExecHandle.class);
        ExecSpec spec = ExecSpec.of("sleep 60");
        when(backend.start(spec)).thenReturn(handle);
        CountDownLatch killing = new CountDownLatch(1);
        CountDownLatch releaseKill = new CountDownLatch(1);
        CountDownLatch killed = new CountDownLatch(1);
        doAnswer(invocation -> {
            killing.countDown();
            releaseKill.await(10, TimeUnit.SECONDS);
            killed.countDown();
            return null;
        }).when(handle).kill();
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(backend);
        when(skillToolFactory.newShellTracker(AGENT_ID)).thenReturn(tracker);
        mockGenerate(new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L));
        when(llmChatTool.chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any()))
                .thenAnswer(invocation -> {
                    tracker.start(spec);
                    ReflectionTestUtils.invokeMethod(sessionStreamTool, "cancelRunContext", MESSAGE_ID);
                    // 回收线程认领进程后再返回；worker 线程已被中断，用自旋等待
                    long deadline = System.currentTimeMillis() + 5000;
                    while (killing.getCount() > 0 && System.currentTimeMillis() < deadline) {
                        Thread.onSpinWait();
                    }
                    return new Ai1LlmChatTool.ChatText("部分", "");
                });
        Thread stopping = new Thread(sessionStreamTool::stop);

        try {
            // 调用：worker 结束并移除上下文，回收仍阻塞
            sessionStreamTool.handleTask(RECORD_ID, buildTaskFields());
            assertTrue(killing.await(5, TimeUnit.SECONDS));
            assertTrue(getRunContexts().isEmpty());
            assertEquals(1, killed.getCount());
            // 调用：节点停止
            stopping.start();
            // 断言：stop 仍等待已结束上下文的回收
            stopping.join(1000);
            assertTrue(stopping.isAlive());
        } finally {
            releaseKill.countDown();
            stopping.join(10_000);
            awaitReapers();
        }
        // 断言：回收完成后 stop 返回，worker 未重复回收
        assertFalse(stopping.isAlive());
        assertEquals(0, killed.getCount());
        verify(handle, times(1)).kill();
        assertTrue(getReapers().isEmpty());
    }

    @Test
    public void testConsumeLoop_noGroup() {
        // mock 方法：任务队列被删除后消费组丢失，重建后退出循环
        ReflectionTestUtils.setField(sessionStreamTool, "running", true);
        when(taskStream.readGroup(anyString(), anyString(), any(StreamReadGroupArgs.class)))
                .thenThrow(new RedisException("NOGROUP No such key 'ai1:session:tasks' or consumer group"));
        doAnswer(invocation -> {
            ReflectionTestUtils.setField(sessionStreamTool, "running", false);
            return null;
        }).when(taskStream).createGroup(any(StreamCreateGroupArgs.class));

        // 调用
        ReflectionTestUtils.invokeMethod(sessionStreamTool, "consumeLoop", "worker-test");
        // 断言
        verify(taskStream).createGroup(any(StreamCreateGroupArgs.class));
    }

    @Test
    public void testFailStaleMessage_notStale() {
        // 调用，并断言：未超时时不加锁，避免 worker 误判为在途
        assertFalse(sessionStreamTool.failStaleMessage(buildGeneratingMessage(LocalDateTime.now())));
        verifyNoInteractions(redissonClient, sessionMessageService);
    }

    @Test
    public void testFailStaleMessage_running() {
        // mock 方法：仍有 worker 持有执行锁
        when(taskLock.tryLock()).thenReturn(false);

        // 调用，并断言
        assertFalse(sessionStreamTool.failStaleMessage(buildGeneratingMessage(LocalDateTime.now().minusMinutes(10))));
        verify(sessionMessageService, never()).getSessionMessage(any());
        verify(sessionMessageService, never()).updateSessionMessage(any());
        verify(resultStream, never()).add(any());
    }

    @Test
    public void testFailStaleMessage_success() {
        // mock 方法：任务记录已裁剪，没有 worker 执行
        Ai1SessionMessageDO message = buildGeneratingMessage(LocalDateTime.now().minusMinutes(10));
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(message);
        when(resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))).thenReturn(
                Map.of(RECORD_ID, Map.of("type", "message", "data", "已生成")));

        // 调用，并断言：与接管任务一致，保留部分结果并标记中断
        assertTrue(sessionStreamTool.failStaleMessage(message));
        verify(sessionMessageService).updateSessionMessage(argThat(updateObj -> "已生成\n\n> 生成任务已中断，请重新发送".equals(updateObj.getContent())
                && Ai1SessionMessageStatusEnum.FAILED.getStatus().equals(updateObj.getStatus())));
        verify(sessionService).touchSession(SESSION_ID);
        assertEquals(List.of("error", "done"), captureResultTypes());
        verify(taskLock).unlock();
        verifyNoInteractions(llmChatTool);
    }

    @Test
    public void testFailStaleMessage_completedConcurrently() {
        // mock 方法：加锁前 worker 已完成
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(buildGeneratingMessage(LocalDateTime.now().minusMinutes(10))
                .setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus()));

        // 调用，并断言：不再改写，归档可继续
        assertTrue(sessionStreamTool.failStaleMessage(buildGeneratingMessage(LocalDateTime.now().minusMinutes(10))));
        verify(sessionMessageService, never()).updateSessionMessage(any());
        verify(resultStream, never()).add(any());
        verify(taskLock).unlock();
    }

    @Test
    public void testFailStaleMessage_lockLostDuringPartialRead() {
        // mock 方法：读取部分结果期间执行锁失效，已被新持有者接管
        Ai1SessionMessageDO message = buildGeneratingMessage(LocalDateTime.now().minusMinutes(10));
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenReturn(message);
        AtomicBoolean owned = new AtomicBoolean(true);
        when(taskLock.isHeldByThread(anyLong())).thenAnswer(invocation -> owned.get());
        when(resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX))).thenAnswer(invocation -> {
            owned.set(false);
            return Map.of(RECORD_ID, Map.of("type", "message", "data", "已生成"));
        });

        // 调用，并断言：不改动消息、结果流，也不释放新持有者的锁
        assertThrows(IllegalStateException.class, () -> sessionStreamTool.failStaleMessage(message));
        verify(sessionMessageService, never()).updateSessionMessage(any());
        verify(sessionService, never()).touchSession(any());
        verify(resultStream, never()).add(any());
        verify(taskLock, never()).unlock();
    }

    // ========== mock 方法 ==========

    private void mockGenerate(Ai1AgentDO agent) {
        when(agentService.validateAgentExists(AGENT_ID)).thenReturn(agent);
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(eq(SESSION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
    }

    private static Thread startWorker(CountDownLatch release, CountDownLatch interrupted) {
        Thread worker = new Thread(() -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                interrupted.countDown();
            }
        });
        worker.start();
        return worker;
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

    private static Ai1SessionMessageDO buildGeneratingMessage(LocalDateTime createTime) {
        Ai1SessionMessageDO message = new Ai1SessionMessageDO().setId(MESSAGE_ID).setSessionId(SESSION_ID)
                .setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus());
        message.setCreateTime(createTime);
        return message;
    }

    /**
     * 等待 PID 文件写入，且该进程属于当前 JVM 的子孙进程
     */
    private static ProcessHandle awaitDescendant(Path pidFile) throws Exception {
        long deadline = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(pidFile)) {
                long pid = Long.parseLong(Files.readString(pidFile).trim());
                Optional<ProcessHandle> process = ProcessHandle.current().descendants()
                        .filter(descendant -> descendant.pid() == pid).findFirst();
                if (process.isPresent()) {
                    return process.get();
                }
            }
            Thread.sleep(50);
        }
        return fail("未找到本用例启动的 Shell 进程");
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> captureResultData() {
        ArgumentCaptor<StreamAddArgs> captor = ArgumentCaptor.forClass(StreamAddArgs.class);
        verify(resultStream, atLeastOnce()).add(captor.capture());
        List<String> data = new ArrayList<>();
        for (StreamAddArgs args : captor.getAllValues()) {
            data.add(String.valueOf(readEntries(args).get("data")));
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private Set<Thread> getReapers() {
        return (Set<Thread>) ReflectionTestUtils.getField(sessionStreamTool, "reapers");
    }

    /**
     * 等待本用例启动的回收线程结束
     */
    private void awaitReapers() throws InterruptedException {
        for (Thread reaper : getReapers()) {
            reaper.join(10_000);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Ai1SessionStreamTool.RunContext> getRunContexts() {
        return (Map<Long, Ai1SessionStreamTool.RunContext>) ReflectionTestUtils.getField(sessionStreamTool, "runContexts");
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
