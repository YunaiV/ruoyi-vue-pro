package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_ADD_SELF;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_NOT_FRIEND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * IM 好友关系 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImFriendServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImFriendServiceImpl friendService;

    @Mock
    private ImFriendMapper imFriendMapper;
    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private ImWebSocketService imWebSocketService;
    @Mock
    private ImPrivateMessageService privateMessageService;

    // ========== isFriend ==========

    @Test
    public void testIsFriend_enabled() {
        ImFriendDO friend = ImFriendDO.builder().userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(friend);

        assertTrue(friendService.isFriend(1L, 2L));
    }

    @Test
    public void testIsFriend_disabledOrAbsent() {
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(null);
        assertFalse(friendService.isFriend(1L, 2L));

        ImFriendDO disabled = ImFriendDO.builder().userId(1L).friendUserId(3L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 3L)).thenReturn(disabled);
        assertFalse(friendService.isFriend(1L, 3L));
    }

    // ========== updateFriend ==========

    @Test
    public void testUpdateFriend_success() {
        // 准备
        ImFriendUpdateReqVO reqVO = new ImFriendUpdateReqVO();
        reqVO.setFriendUserId(2L);
        reqVO.setSilent(true);
        ImFriendDO friend = ImFriendDO.builder().id(100L).userId(1L).friendUserId(2L)
                .silent(false).status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(friend);

        // 调用
        friendService.updateFriend(1L, reqVO);

        // 断言：更新了 silent 字段
        ArgumentCaptor<ImFriendDO> captor = ArgumentCaptor.forClass(ImFriendDO.class);
        verify(imFriendMapper).updateById(captor.capture());
        assertEquals(100L, captor.getValue().getId());
        assertTrue(captor.getValue().getSilent());
        // 断言：推送了好友更新通知
        verify(imWebSocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testUpdateFriend_notFriend() {
        // 准备
        ImFriendUpdateReqVO reqVO = new ImFriendUpdateReqVO();
        reqVO.setFriendUserId(2L);
        reqVO.setSilent(true);
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(null);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendService.updateFriend(1L, reqVO));
        assertEquals(FRIEND_NOT_FRIEND.getCode(), exception.getCode());
    }

    @Test
    public void testUpdateFriend_displayNameOnly() {
        // 准备：只传备注、不传 silent —— 走差量更新
        ImFriendUpdateReqVO reqVO = new ImFriendUpdateReqVO();
        reqVO.setFriendUserId(2L);
        reqVO.setDisplayName("老张");
        ImFriendDO friend = ImFriendDO.builder().id(100L).userId(1L).friendUserId(2L)
                .silent(false).status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(friend);

        // 调用
        friendService.updateFriend(1L, reqVO);

        // 断言：updateById 收到 displayName 但没有 silent（MyBatis-Plus 靠 NOT_NULL 跳过）
        ArgumentCaptor<ImFriendDO> captor = ArgumentCaptor.forClass(ImFriendDO.class);
        verify(imFriendMapper).updateById(captor.capture());
        assertEquals(100L, captor.getValue().getId());
        assertEquals("老张", captor.getValue().getDisplayName());
        assertNull(captor.getValue().getSilent());
        // 断言：推送好友更新通知
        verify(imWebSocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testUpdateFriend_emptyRequest() {
        // 准备：silent 和 displayName 都不传 —— 直接返回，不打 SQL 也不发推送
        ImFriendUpdateReqVO reqVO = new ImFriendUpdateReqVO();
        reqVO.setFriendUserId(2L);
        ImFriendDO friend = ImFriendDO.builder().id(100L).userId(1L).friendUserId(2L)
                .silent(false).status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(friend);

        // 调用
        friendService.updateFriend(1L, reqVO);

        // 断言：没触发 SQL 更新 / 没发 WebSocket 推送
        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
        verify(imWebSocketService, never()).sendPrivateMessageAsync(any(Long.class), any(ImPrivateMessageDTO.class));
    }

    // ========== addFriend ==========

    @Test
    public void testAddFriend_self() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendService.addFriend(1L, 1L));
        assertEquals(FRIEND_ADD_SELF.getCode(), exception.getCode());
    }

    @Test
    public void testAddFriend_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImFriendServiceImpl.class)))
                    .thenReturn(friendService);

            // 准备
            when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(null);
            when(imFriendMapper.selectByUserIdAndFriendUserId(2L, 1L)).thenReturn(null);

            // 调用
            friendService.addFriend(1L, 2L);

            // 断言：校验对方存在
            verify(adminUserApi).validateUser(2L);
            // 断言：双向插入 2 条好友关系
            verify(imFriendMapper, times(2)).insert(any(ImFriendDO.class));
            // 断言：发送提示消息 + 给双方各推送一次好友添加事件
            verify(privateMessageService).sendTipPrivateMessage(eq(1L), eq(2L), anyString());
            verify(imWebSocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
            verify(imWebSocketService).sendPrivateMessageAsync(eq(2L), any(ImPrivateMessageDTO.class));
        }
    }

    @Test
    public void testAddFriend0_existingEnabledSkipsUpdate() {
        // 准备：已存在且启用，第二步不应触发更新
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        // 调用
        friendService.addFriend0(1L, 2L);

        // 断言：不插入，也不更新
        verify(imFriendMapper, never()).insert(any(ImFriendDO.class));
        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
    }

    @Test
    public void testAddFriend0_existingDisabledRecovers() {
        // 准备：已存在且 DISABLE，应当恢复状态
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        // 调用
        friendService.addFriend0(1L, 2L);

        // 断言：更新 status 为 ENABLE
        ArgumentCaptor<ImFriendDO> captor = ArgumentCaptor.forClass(ImFriendDO.class);
        verify(imFriendMapper).updateById(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), captor.getValue().getStatus());
        verify(imFriendMapper, never()).insert(any(ImFriendDO.class));
    }

    @Test
    public void testAddFriend0_duplicateKeySwallowed() {
        // 准备：mapper 抛并发冲突
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(null);
        when(imFriendMapper.insert(any(ImFriendDO.class)))
                .thenThrow(new DuplicateKeyException("concurrent insert"));

        // 调用：应被吞掉，不抛异常
        friendService.addFriend0(1L, 2L);

        verify(imFriendMapper).insert(any(ImFriendDO.class));
    }

    // ========== deleteFriend ==========

    @Test
    public void testDeleteFriend_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImFriendServiceImpl.class)))
                    .thenReturn(friendService);

            ImFriendDO friend12 = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            ImFriendDO friend21 = ImFriendDO.builder().id(11L).userId(2L).friendUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(friend12);
            when(imFriendMapper.selectByUserIdAndFriendUserId(2L, 1L)).thenReturn(friend21);

            friendService.deleteFriend(1L, 2L);

            // 断言：双向更新为 DISABLE
            verify(imFriendMapper, times(2)).updateById(any(ImFriendDO.class));
            // 断言：推送提示消息 + 双方各推送一次好友删除事件
            verify(privateMessageService).sendTipPrivateMessage(eq(1L), eq(2L), anyString());
            verify(imWebSocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
            verify(imWebSocketService).sendPrivateMessageAsync(eq(2L), any(ImPrivateMessageDTO.class));
        }
    }

    @Test
    public void testDeleteFriend0_alreadyDisabled() {
        // 准备：已经是 DISABLE，不再更新
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        friendService.deleteFriend0(1L, 2L);

        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
    }

    @Test
    public void testDeleteFriend0_enabledGetsDisabled() {
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        friendService.deleteFriend0(1L, 2L);

        ArgumentCaptor<ImFriendDO> captor = ArgumentCaptor.forClass(ImFriendDO.class);
        verify(imFriendMapper).updateById(captor.capture());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
        assertNotNull(captor.getValue().getDeleteTime());
    }

    // ========== 其它读方法 ==========

    @Test
    public void testGetFriendList() {
        List<ImFriendDO> list = List.of(
                ImFriendDO.builder().id(1L).userId(1L).friendUserId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImFriendDO.builder().id(2L).userId(1L).friendUserId(3L)
                        .status(CommonStatusEnum.DISABLE.getStatus())
                        .deleteTime(LocalDateTime.now()).build()
        );
        when(imFriendMapper.selectListByUserId(1L)).thenReturn(list);

        List<ImFriendDO> result = friendService.getFriendList(1L);
        assertEquals(2, result.size());
    }

}
