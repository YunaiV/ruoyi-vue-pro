package cn.iocoder.yudao.module.im.controller.admin.rtc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteMoreReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallRespVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcGroupCallRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantStatusEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

// TODO @AI：需要讨论，到底使用 roomName 还是 callId 更好？前端传参；然后，DO 那种，是不是只存储 callId，roomName 二选 1，是不是就行了。或者统一成 room？uuid 完事；callid 还给 call.id 更好理解；
@Tag(name = "管理后台 - IM 实时通话")
@RestController
@RequestMapping("/im/rtc")
@Validated
public class ImRtcCallController {

    @Resource
    private ImRtcCallService rtcCallService;
    @Resource
    private ImProperties imProperties;

    @PostMapping("/invite")
    @Operation(summary = "发起通话；私聊或群聊根据 scene 区分")
    public CommonResult<ImRtcCallRespVO> invite(@Valid @RequestBody ImRtcCallInviteReqVO reqVO) {
        ImRtcCallDO call = rtcCallService.inviteCall(getLoginUserId(), reqVO);
        return success(buildCallResp(call, getLoginUserId()));
    }

    @PostMapping("/join")
    @Operation(summary = "加入已有群通话；用于胶囊条「加入」按钮")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "im_rtc_call_xxxx")
    public CommonResult<ImRtcCallRespVO> joinCall(@RequestParam("roomName") String roomName) {
        ImRtcCallDO call = rtcCallService.joinCall(getLoginUserId(), roomName);
        return success(buildCallResp(call, getLoginUserId()));
    }

    @PostMapping("/invite-more")
    @Operation(summary = "通话中添加成员；仅群通话可用")
    public CommonResult<Boolean> inviteMore(@Valid @RequestBody ImRtcCallInviteMoreReqVO reqVO) {
        rtcCallService.inviteMoreCall(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/accept")
    @Operation(summary = "接听通话")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<ImRtcCallRespVO> accept(@RequestParam("roomName") String roomName) {
        ImRtcCallDO call = rtcCallService.acceptCall(getLoginUserId(), roomName);
        return success(buildCallResp(call, getLoginUserId()));
    }

    @PostMapping("/reject")
    @Operation(summary = "拒绝通话")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<Boolean> reject(@RequestParam("roomName") String roomName) {
        rtcCallService.rejectCall(getLoginUserId(), roomName);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消邀请；主叫接通前调用")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<Boolean> cancel(@RequestParam("roomName") String roomName) {
        rtcCallService.cancelCall(getLoginUserId(), roomName);
        return success(true);
    }

    @PostMapping("/leave")
    @Operation(summary = "离开通话；接通后调用")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<Boolean> leave(@RequestParam("roomName") String roomName) {
        rtcCallService.leaveCall(getLoginUserId(), roomName);
        return success(true);
    }

    @GetMapping("/refresh-token")
    @Operation(summary = "重新签发 Token；客户端重连或 Token 过期续期")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<ImRtcCallRespVO> refreshToken(@RequestParam("roomName") String roomName) {
        ImRtcCallDO call = rtcCallService.refreshCallToken(getLoginUserId(), roomName);
        return success(buildCallResp(call, getLoginUserId()));
    }

    @GetMapping("/get-active-call")
    @Operation(summary = "查询当前进行中的通话；用于群聊顶部「N 人正在通话」胶囊条")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "2048")
    public CommonResult<ImRtcGroupCallRespVO> getActiveCall(@RequestParam("groupId") Long groupId) {
        ImRtcCallDO call = rtcCallService.getActiveCall(getLoginUserId(), groupId);
        return success(buildGroupActiveResp(call));
    }

    // ========== VO 拼装 ==========

    private ImRtcCallRespVO buildCallResp(ImRtcCallDO call, Long userId) {
        if (call == null) {
            return null;
        }
        List<ImRtcParticipantDO> participants = rtcCallService.getCallParticipants(call.getCallId());
        return new ImRtcCallRespVO()
                .setCallId(call.getCallId()).setRoomName(call.getRoomName())
                .setLivekitUrl(imProperties.getRtc().getLivekitUrl())
                .setToken(rtcCallService.signCallToken(userId, call.getRoomName()))
                .setScene(call.getConversationType()).setMediaType(call.getMediaType())
                .setStatus(call.getStatus()).setInviterId(call.getInviterUserId())
                .setGroupId(call.getGroupId())
                .setInviteeIds(filterUserIds(participants, ImRtcParticipantStatusEnum.INVITING))
                .setJoinedUserIds(filterUserIds(participants, ImRtcParticipantStatusEnum.JOINED));
    }

    private ImRtcGroupCallRespVO buildGroupActiveResp(ImRtcCallDO call) {
        if (call == null) {
            return null;
        }
        List<ImRtcParticipantDO> participants = rtcCallService.getCallParticipants(call.getCallId());
        return new ImRtcGroupCallRespVO()
                .setCallId(call.getCallId()).setRoomName(call.getRoomName())
                .setGroupId(call.getGroupId()).setMediaType(call.getMediaType())
                .setInviterId(call.getInviterUserId())
                .setJoinedUserIds(filterUserIds(participants, ImRtcParticipantStatusEnum.JOINED))
                .setInviteeIds(filterUserIds(participants, ImRtcParticipantStatusEnum.INVITING));
    }

    /**
     * 按状态过滤参与者 userId；用 LinkedHashSet 保留前端展示顺序
     */
    private static Set<Long> filterUserIds(List<ImRtcParticipantDO> participants,
                                           ImRtcParticipantStatusEnum status) {
        return CollectionUtils.convertLinkedSet(participants, ImRtcParticipantDO::getUserId,
                p -> Objects.equals(p.getStatus(), status.getStatus()));
    }

}
