package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.request.ImFriendRequestApplyReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendRequestMapper;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendRequestHandleResultEnum;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendStateEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * IM 好友申请 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImFriendRequestServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImFriendRequestServiceImpl friendRequestService;

    @Mock
    private ImFriendRequestMapper friendRequestMapper;
    @Mock
    private ImFriendService friendService;
    @Mock
    private ImWebSocketService websocketService;
    @Mock
    private ImProperties imProperties;
    @Mock
    private AdminUserApi adminUserApi;

    // ========== applyFriend ==========

    @Test
    public void testApplyFriend_addSelf() {
        // 准备：发起人 = 接收人
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(1L);

        // 调用 + 断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.applyFriend(1L, reqVO));
        assertEquals(FRIEND_ADD_SELF.getCode(), exception.getCode());
    }

    @Test
    public void testApplyFriend_alreadyFriend() {
        // 准备
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(2L);
        when(friendService.getFriendState(1L, 2L)).thenReturn(ImFriendStateEnum.FRIEND.getState());

        // 调用 + 断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.applyFriend(1L, reqVO));
        assertEquals(FRIEND_REQUEST_ALREADY_FRIEND.getCode(), exception.getCode());
    }

    @Test
    public void testApplyFriend_blockedByPeer() {
        // 准备
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(2L);
        when(friendService.getFriendState(1L, 2L)).thenReturn(ImFriendStateEnum.BLOCKED.getState());

        // 调用 + 断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.applyFriend(1L, reqVO));
        assertEquals(FRIEND_REQUEST_BLOCKED_BY_PEER.getCode(), exception.getCode());
    }

    @Test
    public void testApplyFriend_silentReAdd() {
        // 准备：单向好友 — 我已删除，但对方仍把我当好友
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(2L).setDisplayName("老张").setAddSource(1);
        when(friendService.getFriendState(1L, 2L)).thenReturn(ImFriendStateEnum.NONE.getState());
        ImFriendDO peerFriend = ImFriendDO.builder().userId(2L).friendUserId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(friendService.getFriend(2L, 1L)).thenReturn(peerFriend);

        // 调用
        ImFriendRequestDO result = friendRequestService.applyFriend(1L, reqVO);

        // 断言：走静默重新加好友，不落申请记录
        assertNull(result);
        verify(friendService).silentReAddFriend(eq(1L), eq(2L), eq("老张"), eq(1));
        verify(friendRequestMapper, never()).insert(any(ImFriendRequestDO.class));
        verify(websocketService, never()).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testApplyFriend_blockedByPeerWhenMyselfDeleted() {
        // 准备：我侧已删除（getFriendState=NONE），对方仍把我当好友但已拉黑
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(2L).setDisplayName("老张").setAddSource(1);
        when(friendService.getFriendState(1L, 2L)).thenReturn(ImFriendStateEnum.NONE.getState());
        ImFriendDO peerFriend = ImFriendDO.builder().userId(2L).friendUserId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).blocked(true).build();
        when(friendService.getFriend(2L, 1L)).thenReturn(peerFriend);

        // 调用 + 断言：必须拒掉，不能走 silentReAddFriend 绕过拉黑
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.applyFriend(1L, reqVO));
        assertEquals(FRIEND_REQUEST_BLOCKED_BY_PEER.getCode(), exception.getCode());
        verify(friendService, never()).silentReAddFriend(anyLong(), anyLong(), anyString(), anyInt());
        verify(friendRequestMapper, never()).insert(any(ImFriendRequestDO.class));
    }

    @Test
    public void testApplyFriend_insertNew() {
        // 准备：双方都无关系，且无历史申请
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(2L).setApplyContent("加个好友").setDisplayName("老张").setAddSource(1);
        when(friendService.getFriendState(1L, 2L)).thenReturn(ImFriendStateEnum.NONE.getState());
        when(friendService.getFriend(2L, 1L)).thenReturn(null);
        when(friendRequestMapper.selectByFromUserIdAndToUserId(1L, 2L)).thenReturn(null);
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setNickname("张三").setAvatar("a.png"));
        when(imProperties.getFriend()).thenReturn(new ImProperties.Friend());

        // 调用
        ImFriendRequestDO result = friendRequestService.applyFriend(1L, reqVO);

        // 断言：落新申请记录
        ArgumentCaptor<ImFriendRequestDO> captor = ArgumentCaptor.forClass(ImFriendRequestDO.class);
        verify(friendRequestMapper).insert(captor.capture());
        ImFriendRequestDO saved = captor.getValue();
        assertEquals(1L, saved.getFromUserId());
        assertEquals(2L, saved.getToUserId());
        assertEquals("加个好友", saved.getApplyContent());
        assertEquals(ImFriendRequestHandleResultEnum.UNHANDLED.getResult(), saved.getHandleResult());
        assertSame(saved, result);
        // 断言：推送给接收方
        verify(websocketService).sendPrivateMessageAsync(eq(2L), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testApplyFriend_reuseOldRequest() {
        // 准备：双方都无关系，但存在历史申请 — 走 reset 复用
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(2L).setApplyContent("再来一次").setDisplayName("老张").setAddSource(2);
        when(friendService.getFriendState(1L, 2L)).thenReturn(ImFriendStateEnum.NONE.getState());
        when(friendService.getFriend(2L, 1L)).thenReturn(null);
        ImFriendRequestDO old = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.REFUSED.getResult())
                .setHandleContent("旧拒绝").setApplyContent("旧内容");
        when(friendRequestMapper.selectByFromUserIdAndToUserId(1L, 2L)).thenReturn(old);
        when(adminUserApi.getUser(1L)).thenReturn(null);
        when(imProperties.getFriend()).thenReturn(new ImProperties.Friend());

        // 调用
        ImFriendRequestDO result = friendRequestService.applyFriend(1L, reqVO);

        // 断言：复用旧记录，未触发 insert
        verify(friendRequestMapper).updateByIdReset(eq(100L), eq("再来一次"), eq("老张"), eq(2),
                any(java.time.LocalDateTime.class));
        verify(friendRequestMapper, never()).insert(any(ImFriendRequestDO.class));
        assertEquals(100L, result.getId());
        assertEquals("再来一次", result.getApplyContent());
        assertEquals(ImFriendRequestHandleResultEnum.UNHANDLED.getResult(), result.getHandleResult());
        assertNull(result.getHandleContent());
        assertNull(result.getHandleTime());
    }

    @Test
    public void testApplyFriend_insertDuplicateKey_reuseOldRequest() {
        // 准备：首次查询不存在，插入时命中唯一键，回查到并发写入的旧申请
        ImFriendRequestApplyReqVO reqVO = new ImFriendRequestApplyReqVO();
        reqVO.setToUserId(2L).setApplyContent("并发申请").setDisplayName("老张").setAddSource(2);
        when(friendService.getFriendState(1L, 2L)).thenReturn(ImFriendStateEnum.NONE.getState());
        when(friendService.getFriend(2L, 1L)).thenReturn(null);
        ImFriendRequestDO old = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.REFUSED.getResult());
        when(friendRequestMapper.selectByFromUserIdAndToUserId(1L, 2L)).thenReturn(null, old);
        when(friendRequestMapper.insert(any(ImFriendRequestDO.class))).thenThrow(new DuplicateKeyException("dup"));
        when(adminUserApi.getUser(1L)).thenReturn(null);
        when(imProperties.getFriend()).thenReturn(new ImProperties.Friend());

        // 调用
        ImFriendRequestDO result = friendRequestService.applyFriend(1L, reqVO);

        // 断言：复用并重置旧申请，不向上抛数据库异常
        verify(friendRequestMapper).updateByIdReset(eq(100L), eq("并发申请"), eq("老张"), eq(2),
                any(LocalDateTime.class));
        assertEquals(100L, result.getId());
        assertEquals(ImFriendRequestHandleResultEnum.UNHANDLED.getResult(), result.getHandleResult());
        verify(websocketService).sendPrivateMessageAsync(eq(2L), any(ImPrivateMessageDTO.class));
    }

    // ========== agreeFriendRequest ==========

    @Test
    public void testAgreeFriendRequest_success() {
        // 准备
        ImFriendRequestDO request = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
        when(friendRequestMapper.selectById(100L)).thenReturn(request);
        when(friendRequestMapper.updateByIdAndHandleResult(eq(100L),
                eq(ImFriendRequestHandleResultEnum.UNHANDLED.getResult()), any(ImFriendRequestDO.class))).thenReturn(1);

        // 调用
        friendRequestService.agreeFriendRequest(2L, 100L);

        // 断言：双向建立好友 + 推 APPROVED 给发起方
        verify(adminUserApi).validateUserList(ListUtil.of(1L, 2L));
        verify(friendService).becomeFriends(request);
        verify(websocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testAgreeFriendRequest_notExists() {
        // 准备：申请不存在
        when(friendRequestMapper.selectById(100L)).thenReturn(null);

        // 调用 + 断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.agreeFriendRequest(2L, 100L));
        assertEquals(FRIEND_REQUEST_NOT_EXISTS.getCode(), exception.getCode());
    }

    @Test
    public void testAgreeFriendRequest_notToMe() {
        // 准备：操作人不是接收方
        ImFriendRequestDO request = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
        when(friendRequestMapper.selectById(100L)).thenReturn(request);

        // 调用 + 断言：3L 不是接收方 2L
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.agreeFriendRequest(3L, 100L));
        assertEquals(FRIEND_REQUEST_NOT_TO_ME.getCode(), exception.getCode());
    }

    @Test
    public void testAgreeFriendRequest_alreadyHandled() {
        // 准备：申请已被处理
        ImFriendRequestDO request = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.AGREED.getResult());
        when(friendRequestMapper.selectById(100L)).thenReturn(request);

        // 调用 + 断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.agreeFriendRequest(2L, 100L));
        assertEquals(FRIEND_REQUEST_HANDLED.getCode(), exception.getCode());
    }

    @Test
    public void testAgreeFriendRequest_concurrentCasFail() {
        // 准备：fail-fast 校验通过，但乐观锁 CAS 被并发抢先
        ImFriendRequestDO request = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
        when(friendRequestMapper.selectById(100L)).thenReturn(request);
        when(friendRequestMapper.updateByIdAndHandleResult(anyLong(), anyInt(), any(ImFriendRequestDO.class))).thenReturn(0);

        // 调用 + 断言：返回 FRIEND_REQUEST_HANDLED
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.agreeFriendRequest(2L, 100L));
        assertEquals(FRIEND_REQUEST_HANDLED.getCode(), exception.getCode());
        verify(friendService, never()).becomeFriends(any(ImFriendRequestDO.class));
    }

    // ========== refuseFriendRequest ==========

    @Test
    public void testRefuseFriendRequest_success() {
        // 准备
        ImFriendRequestDO request = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
        when(friendRequestMapper.selectById(100L)).thenReturn(request);
        when(friendRequestMapper.updateByIdAndHandleResult(eq(100L),
                eq(ImFriendRequestHandleResultEnum.UNHANDLED.getResult()), any(ImFriendRequestDO.class))).thenReturn(1);

        // 调用
        friendRequestService.refuseFriendRequest(2L, 100L, "不认识");

        // 断言：handleResult / handleContent 写入；推 REJECTED 给发起方
        ArgumentCaptor<ImFriendRequestDO> captor = ArgumentCaptor.forClass(ImFriendRequestDO.class);
        verify(friendRequestMapper).updateByIdAndHandleResult(eq(100L),
                eq(ImFriendRequestHandleResultEnum.UNHANDLED.getResult()), captor.capture());
        assertEquals(ImFriendRequestHandleResultEnum.REFUSED.getResult(), captor.getValue().getHandleResult());
        assertEquals("不认识", captor.getValue().getHandleContent());
        verify(websocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testRefuseFriendRequest_concurrentCasFail() {
        // 准备：CAS 失败
        ImFriendRequestDO request = new ImFriendRequestDO().setId(100L).setFromUserId(1L).setToUserId(2L)
                .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
        when(friendRequestMapper.selectById(100L)).thenReturn(request);
        when(friendRequestMapper.updateByIdAndHandleResult(anyLong(), anyInt(), any(ImFriendRequestDO.class))).thenReturn(0);

        // 调用 + 断言：抛 HANDLED，且不推 REJECTED
        ServiceException exception = assertThrows(ServiceException.class,
                () -> friendRequestService.refuseFriendRequest(2L, 100L, "x"));
        assertEquals(FRIEND_REQUEST_HANDLED.getCode(), exception.getCode());
        verify(websocketService, never()).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    // ========== getMyFriendRequestList ==========

    @Test
    public void testGetMyFriendRequestList_delegate() {
        // 准备：mapper 返回 mock 数据
        LocalDateTime updateTime = LocalDateTime.now();
        ImFriendRequestDO cursor = new ImFriendRequestDO();
        cursor.setId(99L);
        cursor.setUpdateTime(updateTime);
        ImFriendRequestDO one = new ImFriendRequestDO();
        one.setId(1L);
        when(friendRequestMapper.selectById(99L)).thenReturn(cursor);
        when(friendRequestMapper.selectMyList(1L, updateTime, 99L, 20))
                .thenReturn(java.util.Collections.singletonList(one));

        // 调用 + 断言：cursor 由 Service 查询后传给 Mapper
        assertEquals(1, friendRequestService.getMyFriendRequestList(1L, 99L, 20).size());
        verify(friendRequestMapper).selectMyList(1L, updateTime, 99L, 20);
    }

    @Test
    public void testGetMyFriendRequestList_cursorNotExists() {
        // 准备：cursor 不存在
        when(friendRequestMapper.selectById(99L)).thenReturn(null);

        // 调用 + 断言：返回空列表
        assertTrue(friendRequestService.getMyFriendRequestList(1L, 99L, 20).isEmpty());
        verify(friendRequestMapper, never()).selectMyList(anyLong(), any(), anyLong(), anyInt());
    }

}
