package cn.iocoder.yudao.module.ai1.harness.session;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.harness.mcp.Ai1McpToolFactory;
import cn.iocoder.yudao.module.ai1.harness.rag.Ai1RagTool;
import cn.iocoder.yudao.module.ai1.harness.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionMessageService;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.Mock;
import org.redisson.Redisson;
import org.redisson.api.RScript;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamCreateGroupArgs;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.api.stream.StreamRangeArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.NameMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static cn.iocoder.yudao.module.ai1.dal.redis.Ai1RedisKeyConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 会话任务认领与执行锁的 Redis 集成测试
 *
 * 默认禁用，使用 -Dai1.integration.redis=true 启用；ai1.integration.redis.address 可指定独立 Redis
 * LLM 与数据库使用 mock
 *
 * @author 芋道源码
 */
@EnabledIfSystemProperty(named = "ai1.integration.redis", matches = "true")
@Timeout(30)
public class Ai1SessionTaskRecoveryIntegrationTest extends BaseMockitoUnitTest {

    private static final Long MESSAGE_ID = 1024L;
    private static final Long SESSION_ID = 10L;

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

    private RedissonClient firstClient;
    private RedissonClient secondClient;
    private Ai1SessionStreamTool firstWorker;
    private Ai1SessionStreamTool secondWorker;
    private RStream<String, String> taskStream;
    private RStream<String, String> resultStream;
    private ExecutorService executor;
    private final CountDownLatch generated = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private final AtomicReference<Ai1SessionMessageDO> message = new AtomicReference<>();
    private final String keyPrefix = "ai1:task-recovery-test:" + UUID.randomUUID() + ":";

    @BeforeEach
    public void before() {
        // 准备两个节点与消息
        firstClient = createClient();
        secondClient = createClient();
        firstWorker = createWorker(firstClient);
        secondWorker = createWorker(secondClient);
        taskStream = secondClient.getStream(SESSION_TASK_STREAM, StringCodec.INSTANCE);
        resultStream = secondClient.getStream(String.format(SESSION_RESULT_STREAM, MESSAGE_ID), StringCodec.INSTANCE);
        taskStream.createGroup(StreamCreateGroupArgs.name(SESSION_TASK_GROUP).makeStream());
        executor = Executors.newSingleThreadExecutor();
        message.set(new Ai1SessionMessageDO().setId(MESSAGE_ID).setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole())
                .setStatus(Ai1SessionMessageStatusEnum.GENERATING.getStatus()));

        // mock 业务方法
        when(sessionMessageService.getSessionMessage(MESSAGE_ID)).thenAnswer(invocation -> message.get());
        doAnswer(invocation -> {
            message.set(invocation.getArgument(0));
            return null;
        }).when(sessionMessageService).updateSessionMessage(any());
        when(agentService.validateAgentExists(1L)).thenReturn(new Ai1AgentDO().setId(1L).setName("客服")
                .setProviderId(1L).setModelId(2L));
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(eq(SESSION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
        when(llmChatTool.chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any()))
                .thenAnswer(invocation -> {
                    Consumer<String> onContent = invocation.getArgument(8);
                    onContent.accept("第一段");
                    generated.countDown();
                    assertTrue(release.await(15, TimeUnit.SECONDS));
                    onContent.accept("第二段");
                    return new Ai1LlmChatTool.ChatText("第一段第二段", "");
                });
    }

    @AfterEach
    public void after() throws InterruptedException {
        release.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        taskStream.delete();
        resultStream.delete();
        secondClient.getLock(String.format(SESSION_TASK_LOCK, MESSAGE_ID)).forceUnlock();
        firstClient.shutdown();
        secondClient.shutdown();
    }

    @Test
    public void testReclaim_activeWorkerIsNotRegenerated() throws Exception {
        // 准备任务，交给第一个节点生成
        StreamMessageId recordId = addTask();
        Map<String, String> fields = taskStream.readGroup(SESSION_TASK_GROUP, "first",
                StreamReadGroupArgs.neverDelivered().count(1)).get(recordId);
        Future<?> generation = executor.submit(() -> firstWorker.handleTask(recordId, fields));
        assertTrue(generated.await(5, TimeUnit.SECONDS));

        // 等待超过两次看门狗租期，再模拟 PEL 空闲超过认领阈值
        Thread.sleep(3200);
        assertTrue(secondClient.getLock(String.format(SESSION_TASK_LOCK, MESSAGE_ID)).isLocked());
        Map<StreamMessageId, Map<String, String>> reclaimed = reclaimTask(recordId);
        assertTrue(reclaimed.containsKey(recordId));
        secondWorker.handleTask(recordId, reclaimed.get(recordId), true);

        // 断言第二个节点没有生成或确认在途任务
        assertEquals(1, taskStream.getPendingInfo(SESSION_TASK_GROUP).getTotal());
        assertEquals(List.of("ping", "message"), resultTypes());
        verify(llmChatTool).chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any());

        // 调用第一个节点继续完成，并断言只有一份输出
        release.countDown();
        generation.get(5, TimeUnit.SECONDS);
        assertEquals("第一段第二段", message.get().getContent());
        assertEquals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), message.get().getStatus());
        assertEquals(List.of("ping", "message", "message", "done"), resultTypes());
        assertEquals(0, taskStream.getPendingInfo(SESSION_TASK_GROUP).getTotal());
    }

    @Test
    public void testReclaim_stoppedWorkerKeepsPartialResult() throws Exception {
        // 准备任务，交给第一个节点生成
        StreamMessageId recordId = addTask();
        Map<String, String> fields = taskStream.readGroup(SESSION_TASK_GROUP, "first",
                StreamReadGroupArgs.neverDelivered().count(1)).get(recordId);
        Future<?> generation = executor.submit(() -> firstWorker.handleTask(recordId, fields));
        assertTrue(generated.await(5, TimeUnit.SECONDS));

        // 停止节点 Redis 客户端，模拟节点退出导致看门狗停止续期
        firstClient.shutdown();
        Thread.sleep(2000);
        assertFalse(secondClient.getLock(String.format(SESSION_TASK_LOCK, MESSAGE_ID)).isLocked());
        Map<StreamMessageId, Map<String, String>> reclaimed = reclaimTask(recordId);
        secondWorker.handleTask(recordId, reclaimed.get(recordId), true);

        // 断言接管只保留部分内容并标记失败，不再次调用模型
        assertEquals("第一段", message.get().getContent());
        assertEquals(Ai1SessionMessageStatusEnum.FAILED.getStatus(), message.get().getStatus());
        assertEquals(List.of("ping", "message", "error", "done"), resultTypes());
        assertEquals(0, taskStream.getPendingInfo(SESSION_TASK_GROUP).getTotal());
        verify(llmChatTool).chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), anyLong(), any(), any());

        // 调用旧节点恢复，断言不追加旧增量或覆盖回填结果
        release.countDown();
        generation.get(5, TimeUnit.SECONDS);
        assertEquals("第一段", message.get().getContent());
        assertEquals(List.of("ping", "message", "error", "done"), resultTypes());
    }

    // ========== 测试数据 ==========

    private RedissonClient createClient() {
        Config config = new Config().setLockWatchdogTimeout(1500);
        // 两个节点共享本用例的独立 Key 前缀，测试及清理不会访问业务队列
        config.setNameMapper(new NameMapper() {
            @Override
            public String map(String name) {
                return name == null ? null : keyPrefix + name;
            }

            @Override
            public String unmap(String name) {
                return name != null && name.startsWith(keyPrefix) ? name.substring(keyPrefix.length()) : name;
            }
        });
        config.useSingleServer().setAddress(System.getProperty("ai1.integration.redis.address", "redis://127.0.0.1:6379"));
        return Redisson.create(config);
    }

    private Ai1SessionStreamTool createWorker(RedissonClient client) {
        Ai1SessionStreamTool worker = new Ai1SessionStreamTool();
        ReflectionTestUtils.setField(worker, "redissonClient", client);
        ReflectionTestUtils.setField(worker, "ai1Properties", new YudaoAi1Properties());
        ReflectionTestUtils.setField(worker, "sessionMessageService", sessionMessageService);
        ReflectionTestUtils.setField(worker, "sessionService", sessionService);
        ReflectionTestUtils.setField(worker, "agentService", agentService);
        ReflectionTestUtils.setField(worker, "modelService", modelService);
        ReflectionTestUtils.setField(worker, "knowledgeBaseService", knowledgeBaseService);
        ReflectionTestUtils.setField(worker, "llmChatTool", llmChatTool);
        ReflectionTestUtils.setField(worker, "mcpToolFactory", mcpToolFactory);
        ReflectionTestUtils.setField(worker, "skillToolFactory", skillToolFactory);
        ReflectionTestUtils.setField(worker, "ragTool", ragTool);
        return worker;
    }

    private StreamMessageId addTask() {
        return taskStream.add(StreamAddArgs.entries(Map.of("messageId", String.valueOf(MESSAGE_ID),
                "agentId", "1", "sessionId", String.valueOf(SESSION_ID), "content", "你好")));
    }

    private Map<StreamMessageId, Map<String, String>> reclaimTask(StreamMessageId recordId) {
        // 只调整本用例独立前缀下的任务，复用生产的查询与认领逻辑
        secondClient.getScript(StringCodec.INSTANCE).eval(RScript.Mode.READ_WRITE,
                "return #redis.call('XCLAIM', KEYS[1], ARGV[1], 'first', 0, ARGV[2], 'IDLE', 300000, 'JUSTID')",
                RScript.ReturnType.LONG, List.of(SESSION_TASK_STREAM), SESSION_TASK_GROUP, recordId.toString());
        return ReflectionTestUtils.invokeMethod(secondWorker, "reclaimTasks", "second");
    }

    private List<String> resultTypes() {
        return resultStream.range(StreamRangeArgs.startId(StreamMessageId.MIN).endId(StreamMessageId.MAX)).values().stream()
                .map(fields -> fields.get("type")).toList();
    }

}
