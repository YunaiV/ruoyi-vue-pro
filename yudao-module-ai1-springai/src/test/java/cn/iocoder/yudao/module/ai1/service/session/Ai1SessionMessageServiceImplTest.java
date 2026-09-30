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
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @Resource
    private Ai1SessionMessageMapper sessionMessageMapper;

    @MockitoBean
    private Ai1SessionService sessionService;
    @MockitoBean
    private Ai1AgentService agentService;
    @MockitoBean
    private Ai1SessionStreamTool sessionStreamTool;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testSendSessionMessageStream_success() {
        // mock 方法：对话归属当前用户，首条消息
        Ai1SessionDO session = new Ai1SessionDO().setId(10L).setAgentId(1L).setUserId(USER_ID)
                .setTitle(Ai1SessionDO.TITLE_DEFAULT);
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenReturn(session);
        SseEmitter emitter = new SseEmitter();
        when(sessionStreamTool.open(anyLong(), isNull())).thenReturn(emitter);
        // 准备参数：提问内容超过标题长度
        String content = "三体舰队还有多久到达地球？请结合原著中的时间线详细说明，并对比面壁计划、执剑人与掩体计划等人类应对方案的技术发展差异";

        // 调用
        SseEmitter result = sessionMessageService.sendSessionMessageStream(USER_ID,
                new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent(content));

        // 断言：用户消息（完成）+ 助手占位（生成中）
        assertSame(emitter, result);
        List<Ai1SessionMessageDO> messages = sessionMessageMapper.selectListBySessionId(10L);
        assertEquals(2, messages.size());
        assertEquals(Ai1SessionMessageRoleEnum.USER.getRole(), messages.get(0).getRole());
        assertEquals(Ai1SessionMessageStatusEnum.SUCCESS.getStatus(), messages.get(0).getStatus());
        Ai1SessionMessageDO assistant = messages.get(1);
        assertEquals(Ai1SessionMessageRoleEnum.ASSISTANT.getRole(), assistant.getRole());
        assertEquals(Ai1SessionMessageStatusEnum.GENERATING.getStatus(), assistant.getStatus());
        // 断言：首条消息自动命名（截断到 50 字符），投递任务，并按助手消息编号打开连接
        verify(sessionService).updateSessionTitle(eq(10L),
                argThat(title -> title.length() == Ai1SessionDO.TITLE_MAX_LENGTH && title.endsWith("...")));
        verify(sessionStreamTool).submit(assistant.getId(), 1L, 10L, content);
        verify(sessionStreamTool).open(assistant.getId(), null);
    }

    @Test
    public void testSendSessionMessageStream_notOwner() {
        // mock 方法：对话不属于当前用户
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenThrow(exception(SESSION_NOT_EXISTS));

        // 调用
        sessionMessageService.sendSessionMessageStream(USER_ID, new Ai1SessionMessageSendReqVO().setSessionId(10L).setContent("你好"));

        // 断言：返回 error 事件，不落库、不投递
        verify(sessionStreamTool).error("对话不存在");
        verify(sessionStreamTool, never()).submit(anyLong(), anyLong(), anyLong(), anyString());
        assertTrue(sessionMessageMapper.selectListBySessionId(10L).isEmpty());
    }

    @Test
    public void testResumeSessionMessageStream_otherUser() {
        // mock 数据：他人对话中的助手消息
        Ai1SessionMessageDO message = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());
        when(sessionService.validateSessionMy(USER_ID, 10L)).thenThrow(exception(SESSION_NOT_EXISTS));

        // 调用
        sessionMessageService.resumeSessionMessageStream(USER_ID, message.getId(), null);

        // 断言：拒绝续传，不读取结果流
        verify(sessionStreamTool).error("对话不存在");
        verify(sessionStreamTool, never()).open(anyLong(), any());
    }

    @Test
    public void testResumeSessionMessageStream_userMessage() {
        // mock 数据：用户消息没有结果流
        Ai1SessionMessageDO message = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());

        // 调用
        sessionMessageService.resumeSessionMessageStream(USER_ID, message.getId(), null);

        // 断言
        verify(sessionStreamTool).error("消息不存在");
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

    @Test
    public void testGetSessionMessageListBySessionIdAndIdLessThan() {
        // mock 数据：同一对话 4 条消息
        Ai1SessionMessageDO message1 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        Ai1SessionMessageDO message2 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());
        Ai1SessionMessageDO message3 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.USER.getRole());
        Ai1SessionMessageDO message4 = insertSessionMessage(10L, Ai1SessionMessageRoleEnum.ASSISTANT.getRole());

        // 调用：取 message4 之前最近 2 条
        List<Ai1SessionMessageDO> list = sessionMessageService.getSessionMessageListBySessionIdAndIdLessThan(10L, message4.getId(), 2);

        // 断言：按编号倒序
        assertEquals(List.of(message3.getId(), message2.getId()), list.stream().map(Ai1SessionMessageDO::getId).toList());
        assertNotEquals(message1.getId(), list.get(1).getId());
    }

    // ========== 随机对象 ==========

    private Ai1SessionMessageDO insertSessionMessage(Long sessionId, String role) {
        Ai1SessionMessageDO message = new Ai1SessionMessageDO().setSessionId(sessionId).setRole(role).setContent("内容")
                .setStatus(Ai1SessionMessageStatusEnum.SUCCESS.getStatus());
        sessionMessageMapper.insert(message);
        return message;
    }

}
