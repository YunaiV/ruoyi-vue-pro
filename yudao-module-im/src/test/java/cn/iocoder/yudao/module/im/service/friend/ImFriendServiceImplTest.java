package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImPrivateMessageSendDTO;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_NOT_FRIEND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ImFriendServiceImpl} 的单元测试
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
        verify(imWebSocketService).sendNotificationAsync(eq(1L), anyInt(), anyInt(), any());
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
    public void testUpdateFriend_disabledFriend() {
        // 准备
        ImFriendUpdateReqVO reqVO = new ImFriendUpdateReqVO();
        reqVO.setFriendUserId(2L);
        reqVO.setSilent(true);
        ImFriendDO friend = ImFriendDO.builder().id(100L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(friend);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendService.updateFriend(1L, reqVO));
        assertEquals(FRIEND_NOT_FRIEND.getCode(), exception.getCode());
        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
        verify(imWebSocketService, never()).sendNotificationAsync(any(Long.class), anyInt(), anyInt(), any());
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
        verify(imWebSocketService).sendNotificationAsync(eq(1L), anyInt(), anyInt(), any());
    }

    @Test
    public void testUpdateFriend_emptyRequest() {
        // 准备：silent / displayName / pinned 都不传 —— 进入方法立刻返回，不查 mapper 也不发推送
        ImFriendUpdateReqVO reqVO = new ImFriendUpdateReqVO();
        reqVO.setFriendUserId(2L);

        // 调用
        friendService.updateFriend(1L, reqVO);

        // 断言：没查记录、没触发 SQL 更新 / 没发 WebSocket 推送
        verify(imFriendMapper, never()).selectByUserIdAndFriendUserId(anyLong(), anyLong());
        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
        verify(imWebSocketService, never()).sendNotificationAsync(any(Long.class), anyInt(), anyInt(), any());
    }

    // ========== 建立好友 ==========

    @Test
    public void testAddFriend0_existingEnabledSkip() {
        // 准备：已存在且启用
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.ENABLE.getStatus()).silent(true).pinned(true).blocked(true).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        // 调用
        friendService.addFriend0(1L, 2L, null, null);

        // 断言：不插入也不更新
        verify(imFriendMapper, never()).insert(any(ImFriendDO.class));
        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
        verify(imFriendMapper, never()).updateReAddFields(anyLong(), anyInt(), any(LocalDateTime.class),
                any(LocalDateTime.class), anyBoolean(), anyBoolean(), anyBoolean(), any(), any());
    }

    @Test
    public void testAddFriend0_existingDisabledRecovers() {
        // 准备：已存在且 DISABLE，应当恢复状态
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        // 调用
        friendService.addFriend0(1L, 2L, null, null);

        // 断言：恢复 ENABLE，并清空 deleteTime
        verify(imFriendMapper).updateReAddFields(eq(10L), eq(CommonStatusEnum.ENABLE.getStatus()),
                any(LocalDateTime.class), any(LocalDateTime.class), eq(false), eq(false), eq(false), isNull(), isNull());
        verify(imFriendMapper, never()).insert(any(ImFriendDO.class));
    }

    @Test
    public void testAddFriend0_duplicateKeyPropagates() {
        // 准备：mapper 抛并发冲突；极端并发下让异常向外抛，由外层事务回滚
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(null);
        when(imFriendMapper.insert(any(ImFriendDO.class)))
                .thenThrow(new DuplicateKeyException("concurrent insert"));

        // 调用 + 断言：异常向外抛
        assertThrows(DuplicateKeyException.class, () -> friendService.addFriend0(1L, 2L, null, null));
        verify(imFriendMapper).insert(any(ImFriendDO.class));
    }

    // ========== deleteFriend ==========

    @Test
    public void testDeleteFriend0_alreadyDisabled() {
        // 准备：已经是 DISABLE，不再更新
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        boolean result = friendService.deleteFriend0(1L, 2L);

        assertFalse(result);
        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
    }

    @Test
    public void testDeleteFriend0_enabledGetsDisabled() {
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        boolean result = friendService.deleteFriend0(1L, 2L);

        assertTrue(result);
        ArgumentCaptor<ImFriendDO> captor = ArgumentCaptor.forClass(ImFriendDO.class);
        verify(imFriendMapper).updateById(captor.capture());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
        assertNotNull(captor.getValue().getDeleteTime());
    }

    @Test
    public void testDeleteFriend_alreadyDisabledSkipNotification() {
        // 准备：已经是 DISABLE，不再推本端删除通知
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        friendService.deleteFriend(1L, 2L, true);

        verify(imFriendMapper, never()).updateById(any(ImFriendDO.class));
        verify(privateMessageService, never()).sendPrivateMessage(anyLong(), any(ImPrivateMessageSendDTO.class));
    }

    @Test
    public void testDeleteFriend_enabledSendNotification() {
        // 准备
        ImFriendDO exists = ImFriendDO.builder().id(10L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectByUserIdAndFriendUserId(1L, 2L)).thenReturn(exists);

        friendService.deleteFriend(1L, 2L, true);

        verify(imFriendMapper).updateById(any(ImFriendDO.class));
        verify(privateMessageService).sendPrivateMessage(eq(1L), any(ImPrivateMessageSendDTO.class));
    }

    // ========== 其它读方法 ==========

    @Test
    public void testGetFriendList() {
        List<ImFriendDO> list = ListUtil.of(
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

    @Test
    public void testGetActiveFriendList_emptySkip() {
        List<ImFriendDO> result = friendService.getActiveFriendList(1L, Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(imFriendMapper, never()).selectListByUserIdAndFriendUserIdsAndStatus(anyLong(), anyCollection(), anyInt());
    }

    @Test
    public void testGetMutualEnableFriendList_filterSingleSideDeleted() {
        ImFriendDO friend2 = ImFriendDO.builder().id(1L).userId(1L).friendUserId(2L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        ImFriendDO friend3 = ImFriendDO.builder().id(2L).userId(1L).friendUserId(3L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(imFriendMapper.selectListByUserIdAndStatus(1L, CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of(friend2, friend3));
        when(imFriendMapper.selectListByUserIdsAndFriendUserIdAndStatus(anyCollection(), eq(1L),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(ListUtil.of(
                        ImFriendDO.builder().userId(2L).friendUserId(1L)
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()));

        List<ImFriendDO> result = friendService.getMutualEnableFriendList(1L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getFriendUserId());
    }

}
