package cn.iocoder.yudao.module.ai1.service.chat;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.message.Ai1ChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatMessageDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.chat.Ai1ChatMessageMapper;
import cn.iocoder.yudao.module.ai1.enums.chat.Ai1ChatMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.chat.Ai1ChatMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.tool.chat.Ai1ChatStreamTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1ChatMessageServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1ChatMessageServiceImpl.class)
public class Ai1ChatMessageServiceImplTest extends BaseDbUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 100L;

    @Resource
    private Ai1ChatMessageServiceImpl chatMessageService;

    @Resource
    private Ai1ChatMessageMapper chatMessageMapper;

    @MockitoBean
    private Ai1ChatConversationService chatConversationService;
    @MockitoBean
    private Ai1AgentService agentService;
    @MockitoBean
    private Ai1ChatStreamTool chatStreamTool;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testSendChatMessageStream_success() {
        // mock 方法：对话归属当前用户，首条消息
        Ai1ChatConversationDO conversation = new Ai1ChatConversationDO().setId(10L).setAgentId(1L).setUserId(USER_ID)
                .setTitle(Ai1ChatConversationDO.TITLE_DEFAULT);
        when(chatConversationService.validateChatConversationMy(USER_ID, 10L)).thenReturn(conversation);
        SseEmitter emitter = new SseEmitter();
        when(chatStreamTool.open(anyLong(), isNull())).thenReturn(emitter);
        // 准备参数：提问内容超过标题长度
        String content = "三体舰队还有多久到达地球？请结合原著中的时间线详细说明，并对比面壁计划、执剑人与掩体计划等人类应对方案的技术发展差异";

        // 调用
        SseEmitter result = chatMessageService.sendChatMessageStream(USER_ID,
                new Ai1ChatMessageSendReqVO().setConversationId(10L).setContent(content));

        // 断言：用户消息（完成）+ 助手占位（生成中）
        assertSame(emitter, result);
        List<Ai1ChatMessageDO> messages = chatMessageMapper.selectListByConversationId(10L);
        assertEquals(2, messages.size());
        assertEquals(Ai1ChatMessageRoleEnum.USER.getRole(), messages.get(0).getRole());
        assertEquals(Ai1ChatMessageStatusEnum.SUCCESS.getStatus(), messages.get(0).getStatus());
        Ai1ChatMessageDO assistant = messages.get(1);
        assertEquals(Ai1ChatMessageRoleEnum.ASSISTANT.getRole(), assistant.getRole());
        assertEquals(Ai1ChatMessageStatusEnum.GENERATING.getStatus(), assistant.getStatus());
        // 断言：首条消息自动命名（截断到 50 字符），任务携带租户，并按助手消息编号打开连接
        verify(chatConversationService).updateChatConversationTitle(eq(10L),
                argThat(title -> title.length() == 50 && title.endsWith("...")));
        verify(chatStreamTool).submit(TENANT_ID, assistant.getId(), 1L, 10L, content);
        verify(chatStreamTool).open(assistant.getId(), null);
    }

    @Test
    public void testSendChatMessageStream_notOwner() {
        // mock 方法：对话不属于当前用户
        when(chatConversationService.validateChatConversationMy(USER_ID, 10L)).thenThrow(exception(CHAT_CONVERSATION_NOT_EXISTS));

        // 调用
        chatMessageService.sendChatMessageStream(USER_ID, new Ai1ChatMessageSendReqVO().setConversationId(10L).setContent("你好"));

        // 断言：返回 error 事件，不落库、不投递
        verify(chatStreamTool).error("对话不存在");
        verify(chatStreamTool, never()).submit(anyLong(), anyLong(), anyLong(), anyLong(), anyString());
        assertTrue(chatMessageMapper.selectListByConversationId(10L).isEmpty());
    }

    @Test
    public void testResumeChatMessageStream_otherUser() {
        // mock 数据：他人对话中的助手消息
        Ai1ChatMessageDO message = insertMessage(10L, Ai1ChatMessageRoleEnum.ASSISTANT.getRole());
        when(chatConversationService.validateChatConversationMy(USER_ID, 10L)).thenThrow(exception(CHAT_CONVERSATION_NOT_EXISTS));

        // 调用
        chatMessageService.resumeChatMessageStream(USER_ID, message.getId(), null);

        // 断言：拒绝续传，不读取结果流
        verify(chatStreamTool).error("对话不存在");
        verify(chatStreamTool, never()).open(anyLong(), any());
    }

    @Test
    public void testResumeChatMessageStream_userMessage() {
        // mock 数据：用户消息没有结果流
        Ai1ChatMessageDO message = insertMessage(10L, Ai1ChatMessageRoleEnum.USER.getRole());

        // 调用
        chatMessageService.resumeChatMessageStream(USER_ID, message.getId(), null);

        // 断言
        verify(chatStreamTool).error("消息不存在");
        verify(chatStreamTool, never()).open(anyLong(), any());
    }

    @Test
    public void testResumeChatMessageStream_success() {
        // mock 数据
        Ai1ChatMessageDO message = insertMessage(10L, Ai1ChatMessageRoleEnum.ASSISTANT.getRole());

        // 调用
        chatMessageService.resumeChatMessageStream(USER_ID, message.getId(), "1-0");

        // 断言
        verify(chatConversationService).validateChatConversationMy(USER_ID, 10L);
        verify(chatStreamTool).open(message.getId(), "1-0");
    }

    @Test
    public void testGetChatMessageListByConversationIdAndIdLessThan() {
        // mock 数据：同一对话 4 条消息
        Ai1ChatMessageDO message1 = insertMessage(10L, Ai1ChatMessageRoleEnum.USER.getRole());
        Ai1ChatMessageDO message2 = insertMessage(10L, Ai1ChatMessageRoleEnum.ASSISTANT.getRole());
        Ai1ChatMessageDO message3 = insertMessage(10L, Ai1ChatMessageRoleEnum.USER.getRole());
        Ai1ChatMessageDO message4 = insertMessage(10L, Ai1ChatMessageRoleEnum.ASSISTANT.getRole());

        // 调用：取 message4 之前最近 2 条
        List<Ai1ChatMessageDO> list = chatMessageService.getChatMessageListByConversationIdAndIdLessThan(10L, message4.getId(), 2);

        // 断言：按编号倒序
        assertEquals(List.of(message3.getId(), message2.getId()), list.stream().map(Ai1ChatMessageDO::getId).toList());
        assertNotEquals(message1.getId(), list.get(1).getId());
    }

    // ========== 随机对象 ==========

    private Ai1ChatMessageDO insertMessage(Long conversationId, String role) {
        Ai1ChatMessageDO message = new Ai1ChatMessageDO().setConversationId(conversationId).setRole(role).setContent("内容")
                .setStatus(Ai1ChatMessageStatusEnum.SUCCESS.getStatus());
        chatMessageMapper.insert(message);
        return message;
    }

}
