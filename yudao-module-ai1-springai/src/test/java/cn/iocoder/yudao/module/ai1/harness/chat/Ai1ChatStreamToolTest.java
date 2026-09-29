package cn.iocoder.yudao.module.ai1.harness.chat;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatMessageDO;
import cn.iocoder.yudao.module.ai1.enums.chat.Ai1ChatMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.framework.ai.core.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatConversationService;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatMessageService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.harness.mcp.Ai1McpToolFactory;
import cn.iocoder.yudao.module.ai1.harness.rag.Ai1RagTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.client.codec.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1ChatStreamTool} 的单元测试：覆盖 worker 的租户恢复、无租户执行、失败回填
 *
 * @author 芋道源码
 */
public class Ai1ChatStreamToolTest extends BaseMockitoUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final Long MESSAGE_ID = 1024L;
    private static final Long AGENT_ID = 1L;
    private static final Long CONVERSATION_ID = 10L;
    private static final StreamMessageId RECORD_ID = new StreamMessageId(1, 0);

    @InjectMocks
    private Ai1ChatStreamTool chatStreamTool;

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RStream<String, String> taskStream;
    @Mock
    private RStream<String, String> resultStream;
    @Spy
    private YudaoAi1Properties ai1Properties = new YudaoAi1Properties();

    @Mock
    private Ai1ChatMessageService chatMessageService;
    @Mock
    private Ai1ChatConversationService chatConversationService;
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
    public void setUp() {
        doReturn(taskStream).when(redissonClient).getStream(eq("ai1:chat:tasks"), any(Codec.class));
        doReturn(resultStream).when(redissonClient).getStream(eq("ai1:chat:result:" + MESSAGE_ID), any(Codec.class));
    }

    @Test
    public void testHandleTask_withoutTenant() {
        // mock 方法：记录生成时的租户上下文，并返回生成结果
        AtomicReference<Long> tenantIdInGenerate = new AtomicReference<>(-1L);
        when(agentService.validateAgentExists(AGENT_ID)).thenAnswer(invocation -> {
            tenantIdInGenerate.set(TenantContextHolder.getTenantId());
            return new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L);
        });
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(chatMessageService.getChatMessageListByConversationIdAndIdLessThan(eq(CONVERSATION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
        when(llmChatTool.chat(any(), eq("你是 客服 的智能助手。"), anyList(), eq("你好"), anyList(), anyList(), eq(CONVERSATION_ID), any(), any()))
                .thenReturn(new Ai1LlmChatTool.ChatText("您好", ""));
        // 准备参数：关闭多租户时，任务不携带 tenantId
        Map<String, String> fields = buildTaskFields();
        fields.remove("tenantId");

        // 调用
        chatStreamTool.handleTask(RECORD_ID, fields);

        // 断言：无租户上下文直接生成，回填完成状态，写 done 终态并确认
        assertNull(tenantIdInGenerate.get());
        ArgumentCaptor<Ai1ChatMessageDO> messageCaptor = ArgumentCaptor.forClass(Ai1ChatMessageDO.class);
        verify(chatMessageService).updateChatMessage(messageCaptor.capture());
        assertEquals(Ai1ChatMessageStatusEnum.SUCCESS.getStatus(), messageCaptor.getValue().getStatus());
        assertEquals(List.of("done"), captureResultTypes());
        verify(taskStream).ack("ai1-chat-workers", RECORD_ID);
    }

    @Test
    public void testHandleTask_generateInTaskTenant() {
        // mock 方法：记录生成时的租户上下文
        AtomicReference<Long> tenantIdInGenerate = new AtomicReference<>();
        when(agentService.validateAgentExists(AGENT_ID)).thenAnswer(invocation -> {
            tenantIdInGenerate.set(TenantContextHolder.getTenantId());
            return new Ai1AgentDO().setId(AGENT_ID).setName("客服").setProviderId(1L).setModelId(2L);
        });
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO().setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(chatMessageService.getChatMessageListByConversationIdAndIdLessThan(eq(CONVERSATION_ID), eq(MESSAGE_ID), anyInt()))
                .thenReturn(new ArrayList<>());
        when(llmChatTool.chat(any(), anyString(), anyList(), eq("你好"), anyList(), anyList(), eq(CONVERSATION_ID), any(), any()))
                .thenAnswer(invocation -> {
                    Consumer<String> onContent = invocation.getArgument(8);
                    onContent.accept("您好");
                    return new Ai1LlmChatTool.ChatText("您好", "");
                });

        // 调用
        chatStreamTool.handleTask(RECORD_ID, buildTaskFields());

        // 断言：生成在任务租户下执行，结束后恢复；回填完成状态并刷新对话
        assertEquals(TENANT_ID, tenantIdInGenerate.get());
        assertNull(TenantContextHolder.getTenantId());
        ArgumentCaptor<Ai1ChatMessageDO> messageCaptor = ArgumentCaptor.forClass(Ai1ChatMessageDO.class);
        verify(chatMessageService).updateChatMessage(messageCaptor.capture());
        assertEquals(MESSAGE_ID, messageCaptor.getValue().getId());
        assertEquals("您好", messageCaptor.getValue().getContent());
        assertNull(messageCaptor.getValue().getReasoning());
        assertEquals(Ai1ChatMessageStatusEnum.SUCCESS.getStatus(), messageCaptor.getValue().getStatus());
        verify(chatConversationService).touchChatConversation(CONVERSATION_ID);
        assertEquals(List.of("message", "done"), captureResultTypes());
    }

    @Test
    public void testHandleTask_generateFailed() {
        // mock 方法：Agent 已被删除
        when(agentService.validateAgentExists(AGENT_ID)).thenThrow(new IllegalStateException("Agent 不存在"));

        // 调用
        chatStreamTool.handleTask(RECORD_ID, buildTaskFields());

        // 断言：回填失败状态，写 error + done 终态
        ArgumentCaptor<Ai1ChatMessageDO> messageCaptor = ArgumentCaptor.forClass(Ai1ChatMessageDO.class);
        verify(chatMessageService).updateChatMessage(messageCaptor.capture());
        assertEquals(Ai1ChatMessageStatusEnum.FAILED.getStatus(), messageCaptor.getValue().getStatus());
        assertEquals(List.of("error", "done"), captureResultTypes());
    }

    // ========== 随机对象 ==========

    private static Map<String, String> buildTaskFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("tenantId", String.valueOf(TENANT_ID));
        fields.put("messageId", String.valueOf(MESSAGE_ID));
        fields.put("agentId", String.valueOf(AGENT_ID));
        fields.put("conversationId", String.valueOf(CONVERSATION_ID));
        fields.put("content", "你好");
        return fields;
    }

    /**
     * 按顺序获取写入结果流的事件类型
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> captureResultTypes() {
        ArgumentCaptor<StreamAddArgs> captor = ArgumentCaptor.forClass(StreamAddArgs.class);
        verify(resultStream, atLeastOnce()).add(captor.capture());
        List<String> types = new ArrayList<>();
        for (StreamAddArgs args : captor.getAllValues()) {
            // StreamAddArgs 未提供读取方法，通过实现类 StreamAddParams 的 entries 字段断言事件类型
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
