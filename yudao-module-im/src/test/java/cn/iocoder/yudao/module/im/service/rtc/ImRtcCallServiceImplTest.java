package cn.iocoder.yudao.module.im.service.rtc;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import cn.iocoder.yudao.module.im.dal.mysql.rtc.ImRtcCallMapper;
import cn.iocoder.yudao.module.im.dal.mysql.rtc.ImRtcParticipantMapper;
import cn.iocoder.yudao.module.im.dal.redis.rtc.ImRtcCallLockRedisDAO;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallStatusEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantRoleEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantStatusEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.RTC_GROUP_INVITEE_REQUIRED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.RTC_GROUP_INVITEE_OVER_LIMIT;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.RTC_SELF_BUSY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ImRtcCallServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImRtcCallServiceImpl rtcCallService;

    @Mock
    private ImRtcParticipantMapper rtcParticipantMapper;
    @Mock
    private ImRtcCallMapper rtcCallMapper;
    @Mock
    private ImRtcCallLockRedisDAO rtcCallLockRedisDAO;
    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private ImWebSocketService webSocketService;
    @Mock
    private ImProperties imProperties;
    @Mock
    private ImGroupMemberService groupMemberService;

    // ========== timeoutInvitingParticipants（Job 入口）==========

    @Test
    public void testTimeoutInvitingParticipants_emptyCandidates_returnsZeroAndNoDownstream() {
        // 准备：无超时候选
        when(rtcParticipantMapper.selectListByStatusAndInviteTimeBefore(
                eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // 调用
        int result = rtcCallService.timeoutInvitingParticipants(1);

        // 断言：返回 0；无候选时不应触发 user 预查 / call 查询 / 推送
        assertEquals(0, result);
        verifyNoInteractions(adminUserApi, rtcCallMapper, webSocketService);
    }

    @Test
    public void testTimeoutInvitingParticipants_thresholdConvertedToCutoff() {
        // 准备：阈值 5 分钟；mock 空候选避免触发后续逻辑
        when(rtcParticipantMapper.selectListByStatusAndInviteTimeBefore(any(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // 调用
        LocalDateTime before = LocalDateTime.now();
        rtcCallService.timeoutInvitingParticipants(5);

        // 断言：cutoff = now - 5 分钟（允许 5 秒漂移）
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(rtcParticipantMapper).selectListByStatusAndInviteTimeBefore(
                eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), cutoffCaptor.capture());
        LocalDateTime cutoff = cutoffCaptor.getValue();
        LocalDateTime expected = before.minusMinutes(5);
        assertTrue(Duration.between(cutoff, expected).abs().getSeconds() < 5,
                "cutoff 应当约等于 now - 5 min；实际：" + cutoff);
    }

    @Test
    public void testTimeoutInvitingParticipants_casAllFails_noPushNoEndSession() {
        // 准备：候选非空但每个 CAS 都失败（并发已变状态）
        ImRtcParticipantDO p = buildParticipant(10L, "r1", 100L, ImRtcParticipantStatusEnum.INVITING);
        when(rtcParticipantMapper.selectListByStatusAndInviteTimeBefore(any(), any()))
                .thenReturn(List.of(p));
        when(adminUserApi.getUserMap(anySet())).thenReturn(Map.of(100L, buildUser(100L)));
        when(rtcParticipantMapper.updateByIdAndStatus(eq(10L), eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), any()))
                .thenReturn(0);

        // 调用
        int result = rtcCallService.timeoutInvitingParticipants(1);

        // 断言：CAS 全失败时返回 0；不查 call、不推送
        assertEquals(0, result);
        verify(rtcCallMapper, never()).selectByRoom(any());
        verifyNoInteractions(webSocketService);
    }

    @Test
    public void testTimeoutInvitingParticipants_groupCall_pushesNoAnswerSkipsEndSession() {
        // 准备：群通话单候选 CAS 成功；shouldCloseGroupRoom 通过 selectListByRoom 多 JOINED 让其返 false，跳过 endSession
        ImRtcParticipantDO p = buildParticipant(10L, "r1", 100L, ImRtcParticipantStatusEnum.INVITING);
        when(rtcParticipantMapper.selectListByStatusAndInviteTimeBefore(any(), any()))
                .thenReturn(List.of(p));
        when(adminUserApi.getUserMap(anySet())).thenReturn(Map.of(100L, buildUser(100L)));
        when(rtcParticipantMapper.updateByIdAndStatus(eq(10L), eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), any()))
                .thenReturn(1);
        ImRtcCallDO call = buildCall("r1", 200L, ImConversationTypeEnum.GROUP, 999L);
        when(rtcCallMapper.selectByRoom("r1")).thenReturn(call);
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(999L)).thenReturn(List.of(200L, 201L));
        // 房内 2 个 JOINED + 1 个 INVITING → shouldCloseGroupRoom 返 false
        when(rtcParticipantMapper.selectListByRoom("r1")).thenReturn(List.of(
                buildParticipant(20L, "r1", 200L, ImRtcParticipantStatusEnum.JOINED),
                buildParticipant(21L, "r1", 201L, ImRtcParticipantStatusEnum.JOINED),
                buildParticipant(22L, "r1", 202L, ImRtcParticipantStatusEnum.INVITING)
        ));

        // 调用
        int result = rtcCallService.timeoutInvitingParticipants(1);

        // 断言：成功 1 个；NO_ANSWER 信令推到主叫；不触发 endSession
        assertEquals(1, result);
        verify(webSocketService).sendPrivateMessageAsync(eq(200L), any(ImPrivateMessageDTO.class));
        verify(rtcCallMapper, never()).updateByIdAndStatusIn(any(), anyCollection(), any());
    }

    @Test
    public void testTimeoutInvitingParticipants_callMissing_silentSkip() {
        // 准备：CAS 成功后通话主表缺失（异常兜底场景）
        ImRtcParticipantDO p = buildParticipant(10L, "r1", 100L, ImRtcParticipantStatusEnum.INVITING);
        when(rtcParticipantMapper.selectListByStatusAndInviteTimeBefore(any(), any()))
                .thenReturn(List.of(p));
        when(adminUserApi.getUserMap(anySet())).thenReturn(Map.of(100L, buildUser(100L)));
        when(rtcParticipantMapper.updateByIdAndStatus(eq(10L), eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), any()))
                .thenReturn(1);
        when(rtcCallMapper.selectByRoom("r1")).thenReturn(null);

        // 调用
        int result = rtcCallService.timeoutInvitingParticipants(1);

        // 断言：CAS 已成功但 call 缺失视为部分失败返 0；不应推送
        assertEquals(0, result);
        verifyNoInteractions(webSocketService);
    }

    // ========== noAnswerCallCheck（前端 timer 入口）==========

    @Test
    public void testNoAnswerCallCheck_authFails_silentNoOp() {
        // 准备：selectByRoomAndUserId 返 null 覆盖三种鉴权失败场景（非参与者 / 非法 room / null room）
        when(rtcParticipantMapper.selectByRoomAndUserId(any(), eq(100L))).thenReturn(null);

        // 调用
        rtcCallService.noAnswerCallCheck(100L, "r1");
        rtcCallService.noAnswerCallCheck(100L, "");
        rtcCallService.noAnswerCallCheck(100L, null);

        // 断言：仅鉴权查询了 3 次；不应进入后续超时扫描 / 推送
        verify(rtcParticipantMapper, times(3)).selectByRoomAndUserId(any(), eq(100L));
        verify(rtcParticipantMapper, never()).selectListByRoomAndStatusAndInviteTimeBefore(any(), any(), any());
        verifyNoInteractions(adminUserApi, webSocketService);
    }

    @Test
    public void testNoAnswerCallCheck_usesBackendThreshold_notFrontend() {
        // 准备：鉴权通过 + 后端配置阈值 2 分钟 + 无候选（避免触发推送）
        when(rtcParticipantMapper.selectByRoomAndUserId("r1", 100L))
                .thenReturn(buildParticipant(10L, "r1", 100L, ImRtcParticipantStatusEnum.INVITING));
        ImProperties.Rtc rtcConfig = new ImProperties.Rtc();
        rtcConfig.setInviteTimeoutMinutes(2);
        when(imProperties.getRtc()).thenReturn(rtcConfig);
        when(rtcParticipantMapper.selectListByRoomAndStatusAndInviteTimeBefore(
                eq("r1"), eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // 调用
        LocalDateTime before = LocalDateTime.now();
        rtcCallService.noAnswerCallCheck(100L, "r1");

        // 断言：扫描时使用 cutoff = now - 2 分钟（后端配置），而非前端 60s
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(rtcParticipantMapper).selectListByRoomAndStatusAndInviteTimeBefore(
                eq("r1"), eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), cutoffCaptor.capture());
        LocalDateTime cutoff = cutoffCaptor.getValue();
        LocalDateTime expected = before.minusMinutes(2);
        assertTrue(Duration.between(cutoff, expected).abs().getSeconds() < 5,
                "cutoff 应当约等于 now - 2 min（后端配置）；实际：" + cutoff);
    }

    @Test
    public void testNoAnswerCallCheck_groupCall_pushesNoAnswer() {
        // 准备：鉴权通过 + 单候选 CAS 成功 + 群通话不关房
        when(rtcParticipantMapper.selectByRoomAndUserId("r1", 100L))
                .thenReturn(buildParticipant(10L, "r1", 100L, ImRtcParticipantStatusEnum.INVITING));
        ImProperties.Rtc rtcConfig = new ImProperties.Rtc();
        rtcConfig.setInviteTimeoutMinutes(1);
        when(imProperties.getRtc()).thenReturn(rtcConfig);
        ImRtcParticipantDO timeoutTarget = buildParticipant(11L, "r1", 101L, ImRtcParticipantStatusEnum.INVITING);
        when(rtcParticipantMapper.selectListByRoomAndStatusAndInviteTimeBefore(
                eq("r1"), eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), any()))
                .thenReturn(List.of(timeoutTarget));
        when(adminUserApi.getUserMap(anySet())).thenReturn(Map.of(101L, buildUser(101L)));
        when(rtcParticipantMapper.updateByIdAndStatus(eq(11L), eq(ImRtcParticipantStatusEnum.INVITING.getStatus()), any()))
                .thenReturn(1);
        ImRtcCallDO call = buildCall("r1", 200L, ImConversationTypeEnum.GROUP, 999L);
        when(rtcCallMapper.selectByRoom("r1")).thenReturn(call);
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(999L)).thenReturn(List.of(200L, 201L));
        // 让 shouldCloseGroupRoom 返 false
        when(rtcParticipantMapper.selectListByRoom("r1")).thenReturn(List.of(
                buildParticipant(20L, "r1", 200L, ImRtcParticipantStatusEnum.JOINED),
                buildParticipant(21L, "r1", 201L, ImRtcParticipantStatusEnum.JOINED)
        ));

        // 调用
        rtcCallService.noAnswerCallCheck(100L, "r1");

        // 断言：NO_ANSWER 信令推到主叫 200L；不触发 endSession
        verify(webSocketService).sendPrivateMessageAsync(eq(200L), any(ImPrivateMessageDTO.class));
        verify(rtcCallMapper, never()).updateByIdAndStatusIn(any(), anyCollection(), any());
    }

    // ========== createCall ==========

    @Test
    public void testCreateCall_groupOnlyInviteSelf_throwInviteeRequired() throws Exception {
        when(imProperties.getRtc()).thenReturn(new ImProperties.Rtc());
        when(rtcCallLockRedisDAO.lockGroup(eq(10L), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Callable<ImRtcCallDO> callable = invocation.getArgument(1);
            return callable.call();
        });
        ImRtcCallCreateReqVO reqVO = new ImRtcCallCreateReqVO();
        reqVO.setConversationType(ImConversationTypeEnum.GROUP.getType());
        reqVO.setGroupId(10L);
        reqVO.setInviteeIds(Set.of(100L));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> rtcCallService.createCall(100L, reqVO));

        assertEquals(RTC_GROUP_INVITEE_REQUIRED.getCode(), exception.getCode());
        verify(rtcParticipantMapper, never()).insertBatch(anyList());
    }

    // ========== acceptCall / joinCall 忙线校验 ==========

    @Test
    public void testAcceptCall_joinedOtherRoom_throwSelfBusy() {
        // 准备：当前通话仍在邀请中，但用户已加入另一个房间
        when(imProperties.getRtc()).thenReturn(new ImProperties.Rtc());
        ImRtcCallDO call = buildCall("r1", 200L, ImConversationTypeEnum.PRIVATE, null);
        when(rtcCallMapper.selectByRoom("r1")).thenReturn(call);
        when(rtcParticipantMapper.selectByRoomAndUserId("r1", 100L))
                .thenReturn(buildParticipant(10L, "r1", 100L, ImRtcParticipantStatusEnum.INVITING));
        when(rtcParticipantMapper.selectLastOneByUserIdAndStatusInAndRoomNot(eq(100L), anyCollection(), eq("r1")))
                .thenReturn(buildParticipant(11L, "r2", 100L, ImRtcParticipantStatusEnum.JOINED));

        // 调用 + 断言：拒绝接听，不覆盖其它房间状态
        ServiceException exception = assertThrows(ServiceException.class,
                () -> rtcCallService.acceptCall(100L, "r1"));
        assertEquals(RTC_SELF_BUSY.getCode(), exception.getCode());
        verify(rtcParticipantMapper, never()).updateByIdAndStatus(eq(10L), any(), any());
    }

    @Test
    public void testJoinCall_joinedOtherRoom_throwSelfBusy() {
        // 准备：群通话活跃，用户已加入另一个房间
        when(imProperties.getRtc()).thenReturn(new ImProperties.Rtc());
        ImRtcCallDO call = buildCall("r1", 200L, ImConversationTypeEnum.GROUP, 999L);
        when(rtcCallMapper.selectByRoom("r1")).thenReturn(call);
        when(groupMemberService.validateMemberInGroup(999L, 100L)).thenReturn(new ImGroupMemberDO());
        when(rtcParticipantMapper.selectLastOneByUserIdAndStatusInAndRoomNot(eq(100L), anyCollection(), eq("r1")))
                .thenReturn(buildParticipant(11L, "r2", 100L, ImRtcParticipantStatusEnum.JOINED));

        // 调用 + 断言：拒绝加入，不写参与者状态
        ServiceException exception = assertThrows(ServiceException.class,
                () -> rtcCallService.joinCall(100L, "r1"));
        assertEquals(RTC_SELF_BUSY.getCode(), exception.getCode());
        verify(rtcParticipantMapper, never()).insert(any(ImRtcParticipantDO.class));
        verify(rtcParticipantMapper, never()).updateById(any(ImRtcParticipantDO.class));
    }

    @Test
    public void testJoinCall_insertDuplicateKey_reuseExistingParticipant() {
        // 准备：群通话活跃，首次查询无参与者，插入时命中唯一键
        when(imProperties.getRtc()).thenReturn(new ImProperties.Rtc());
        ImRtcCallDO call = buildCall("r1", 200L, ImConversationTypeEnum.GROUP, 999L);
        when(rtcCallMapper.selectByRoom("r1")).thenReturn(call);
        when(groupMemberService.validateMemberInGroup(999L, 100L)).thenReturn(new ImGroupMemberDO());
        when(rtcParticipantMapper.selectLastOneByUserIdAndStatusInAndRoomNot(eq(100L), anyCollection(), eq("r1")))
                .thenReturn(null);
        when(rtcParticipantMapper.selectByRoomAndUserId("r1", 100L))
                .thenReturn(null, buildParticipant(10L, "r1", 100L, ImRtcParticipantStatusEnum.JOINED));
        when(rtcParticipantMapper.insert(any(ImRtcParticipantDO.class))).thenThrow(new DuplicateKeyException("dup"));

        // 调用
        ImRtcCallDO result = rtcCallService.joinCall(100L, "r1");

        // 断言：不向上抛数据库异常
        assertSame(call, result);
        verify(rtcParticipantMapper, never()).updateById(any(ImRtcParticipantDO.class));
    }

    @Test
    public void testInviteCall_overLimit_throws() throws Exception {
        ImProperties.Rtc rtcConfig = new ImProperties.Rtc();
        rtcConfig.setGroupMaxParticipants(3);
        when(imProperties.getRtc()).thenReturn(rtcConfig);
        ImRtcCallDO call = buildCall("r1", 200L, ImConversationTypeEnum.GROUP, 999L);
        when(rtcCallMapper.selectByRoom("r1")).thenReturn(call);
        when(rtcParticipantMapper.selectByRoomAndUserId("r1", 200L))
                .thenReturn(buildParticipant(10L, "r1", 200L, ImRtcParticipantStatusEnum.JOINED));
        when(rtcCallLockRedisDAO.lockGroup(eq(999L), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Callable<Void> callable = invocation.getArgument(1);
            return callable.call();
        });
        when(rtcParticipantMapper.selectListByRoom("r1")).thenReturn(List.of(
                buildParticipant(10L, "r1", 200L, ImRtcParticipantStatusEnum.JOINED),
                buildParticipant(11L, "r1", 201L, ImRtcParticipantStatusEnum.JOINED),
                buildParticipant(12L, "r1", 202L, ImRtcParticipantStatusEnum.INVITING)
        ));
        ImRtcCallInviteReqVO reqVO = new ImRtcCallInviteReqVO();
        reqVO.setRoom("r1");
        reqVO.setInviteeIds(Set.of(203L));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> rtcCallService.inviteCall(200L, reqVO));

        assertEquals(RTC_GROUP_INVITEE_OVER_LIMIT.getCode(), exception.getCode());
        verify(rtcParticipantMapper, never()).insertBatch(anyList());
    }

    // ========== 测试数据构造 ==========

    private ImRtcParticipantDO buildParticipant(Long id, String room, Long userId, ImRtcParticipantStatusEnum status) {
        return new ImRtcParticipantDO()
                .setId(id)
                .setRoom(room)
                .setUserId(userId)
                .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                .setStatus(status.getStatus())
                .setInviteTime(LocalDateTime.now());
    }

    private ImRtcCallDO buildCall(String room, Long inviterUserId, ImConversationTypeEnum conversationType, Long groupId) {
        return new ImRtcCallDO()
                .setRoom(room)
                .setConversationType(conversationType.getType())
                .setMediaType(1)
                .setInviterUserId(inviterUserId)
                .setGroupId(groupId)
                .setStatus(ImRtcCallStatusEnum.RUNNING.getStatus())
                .setStartTime(LocalDateTime.now());
    }

    private AdminUserRespDTO buildUser(Long id) {
        AdminUserRespDTO user = new AdminUserRespDTO();
        user.setId(id);
        user.setNickname("user-" + id);
        return user;
    }

}
