package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImChannelMessageMapper;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMaterialService;
import cn.iocoder.yudao.module.im.service.conversation.ImConversationReadService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link ImChannelMessageServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class ImChannelMessageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImChannelMessageServiceImpl channelMessageService;

    @Mock
    private ImChannelMessageMapper channelMessageMapper;
    @Mock
    private ImChannelMaterialService channelMaterialService;
    @Mock
    private ImWebSocketService webSocketService;
    @Mock
    private ImConversationReadService conversationReadService;

    @Test
    public void testReadChannelMessages_messageNotExists() {
        // 准备：messageId 不存在（伪造 / 未来 id）
        when(channelMessageMapper.selectById(999L)).thenReturn(null);

        // 调用
        channelMessageService.readChannelMessages(1L, 10L, 999L);

        // 断言：不推进读位置
        verify(conversationReadService, never()).updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong());
    }

    @Test
    public void testReadChannelMessages_wrongChannel() {
        // 准备：消息属于别的频道
        ImChannelMessageDO message = ImChannelMessageDO.builder()
                .id(100L).channelId(20L).sendTime(LocalDateTime.now()).build();
        when(channelMessageMapper.selectById(100L)).thenReturn(message);

        // 调用：声称读的是频道 10
        channelMessageService.readChannelMessages(1L, 10L, 100L);

        // 断言：不推进读位置
        verify(conversationReadService, never()).updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong());
    }

    @Test
    public void testReadChannelMessages_notVisible() {
        // 准备：定向消息，接收人不含当前用户
        ImChannelMessageDO message = ImChannelMessageDO.builder()
                .id(100L).channelId(10L).receiverUserIds(List.of(2L, 3L)).sendTime(LocalDateTime.now()).build();
        when(channelMessageMapper.selectById(100L)).thenReturn(message);

        // 调用
        channelMessageService.readChannelMessages(1L, 10L, 100L);

        // 断言：不推进读位置
        verify(conversationReadService, never()).updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong());
    }

    @Test
    public void testReadChannelMessages_notAdvanced() {
        // 准备：消息真实可见，但读位置未前进（updateConversationReadPosition 返回 false）
        ImChannelMessageDO message = ImChannelMessageDO.builder()
                .id(100L).channelId(10L).sendTime(LocalDateTime.now()).build();
        when(channelMessageMapper.selectById(100L)).thenReturn(message);
        when(conversationReadService.updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong()))
                .thenReturn(false);

        // 调用
        channelMessageService.readChannelMessages(1L, 10L, 100L);

        // 断言：读位置未前进 → 不推 READ 事件
        verify(webSocketService, never()).sendNotificationAsync(anyLong(), anyInt(), anyInt(), any());
    }

}
