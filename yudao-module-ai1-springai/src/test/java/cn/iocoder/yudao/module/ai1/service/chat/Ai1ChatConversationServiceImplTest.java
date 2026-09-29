package cn.iocoder.yudao.module.ai1.service.chat;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.chat.Ai1ChatConversationMapper;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link Ai1ChatConversationServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1ChatConversationServiceImpl.class)
public class Ai1ChatConversationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1ChatConversationServiceImpl chatConversationService;

    @Resource
    private Ai1ChatConversationMapper chatConversationMapper;

    @MockitoBean
    private Ai1AgentService agentService;
    @MockitoBean
    private Ai1ChatMessageService chatMessageService;

    @Test
    public void testCreateChatConversationMy_success() {
        // 调用
        Long id = chatConversationService.createChatConversationMy(100L, new Ai1ChatConversationCreateMyReqVO().setAgentId(1L));

        // 断言：归属当前用户，使用默认标题
        Ai1ChatConversationDO conversation = chatConversationMapper.selectById(id);
        assertEquals(100L, conversation.getUserId());
        assertEquals(Ai1ChatConversationDO.TITLE_DEFAULT, conversation.getTitle());
        verify(agentService).validateAgentEnabled(1L);
    }

    @Test
    public void testUpdateChatConversationMy_otherUser() {
        // mock 数据：他人的对话
        Long id = chatConversationService.createChatConversationMy(100L, new Ai1ChatConversationCreateMyReqVO().setAgentId(1L));

        // 调用，并断言异常：不能改他人对话，且与不存在同样处理
        assertServiceException(() -> chatConversationService.updateChatConversationMy(200L,
                new Ai1ChatConversationUpdateMyReqVO().setId(id).setTitle("改标题")), CHAT_CONVERSATION_NOT_EXISTS);
        assertEquals(Ai1ChatConversationDO.TITLE_DEFAULT, chatConversationMapper.selectById(id).getTitle());
    }

    @Test
    public void testDeleteChatConversationMy_otherUser() {
        // mock 数据：他人的对话
        Long id = chatConversationService.createChatConversationMy(100L, new Ai1ChatConversationCreateMyReqVO().setAgentId(1L));

        // 调用，并断言异常
        assertServiceException(() -> chatConversationService.deleteChatConversationMy(200L, id), CHAT_CONVERSATION_NOT_EXISTS);
        assertNotNull(chatConversationMapper.selectById(id));
        verifyNoInteractions(chatMessageService);
    }

    @Test
    public void testDeleteChatConversationMy_success() {
        // mock 数据
        Long id = chatConversationService.createChatConversationMy(100L, new Ai1ChatConversationCreateMyReqVO().setAgentId(1L));

        // 调用
        chatConversationService.deleteChatConversationMy(100L, id);

        // 断言：连带删除消息
        assertNull(chatConversationMapper.selectById(id));
        verify(chatMessageService).deleteChatMessageListByConversationIds(Collections.singletonList(id));
    }

    @Test
    public void testGetChatConversationListByAgentIdAndUserId() {
        // mock 数据：当前用户 2 个、他人 1 个、其他 Agent 1 个
        Long id1 = chatConversationService.createChatConversationMy(100L, new Ai1ChatConversationCreateMyReqVO().setAgentId(1L));
        Long id2 = chatConversationService.createChatConversationMy(100L, new Ai1ChatConversationCreateMyReqVO().setAgentId(1L));
        chatConversationService.createChatConversationMy(200L, new Ai1ChatConversationCreateMyReqVO().setAgentId(1L));
        chatConversationService.createChatConversationMy(100L, new Ai1ChatConversationCreateMyReqVO().setAgentId(2L));

        // 调用
        List<Ai1ChatConversationDO> list = chatConversationService.getChatConversationListByAgentIdAndUserId(1L, 100L);

        // 断言：只返回当前用户在该 Agent 下的对话，按编号倒序
        assertEquals(Arrays.asList(id2, id1), list.stream().map(Ai1ChatConversationDO::getId).toList());
    }

}
