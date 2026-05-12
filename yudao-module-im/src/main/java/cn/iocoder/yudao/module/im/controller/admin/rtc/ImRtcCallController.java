package cn.iocoder.yudao.module.im.controller.admin.rtc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteMoreReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallRespVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcGroupCallRespVO;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - IM 实时通话")
@RestController
@RequestMapping("/im/rtc")
@Validated
public class ImRtcCallController {

    @Resource
    private ImRtcCallService rtcCallService;

    @PostMapping("/invite")
    @Operation(summary = "发起通话；私聊或群聊根据 scene 区分")
    public CommonResult<ImRtcCallRespVO> invite(@Valid @RequestBody ImRtcCallInviteReqVO reqVO) {
        return success(rtcCallService.invite(getLoginUserId(), reqVO));
    }

    @PostMapping("/invite-more")
    @Operation(summary = "通话中添加成员；仅群通话可用")
    public CommonResult<Boolean> inviteMore(@Valid @RequestBody ImRtcCallInviteMoreReqVO reqVO) {
        rtcCallService.inviteMore(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/accept")
    @Operation(summary = "接听通话")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<ImRtcCallRespVO> accept(@RequestParam("roomName") String roomName) {
        return success(rtcCallService.accept(getLoginUserId(), roomName));
    }

    @PostMapping("/reject")
    @Operation(summary = "拒绝通话")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<Boolean> reject(@RequestParam("roomName") String roomName) {
        rtcCallService.reject(getLoginUserId(), roomName);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消邀请；主叫接通前调用")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<Boolean> cancel(@RequestParam("roomName") String roomName) {
        rtcCallService.cancel(getLoginUserId(), roomName);
        return success(true);
    }

    @PostMapping("/leave")
    @Operation(summary = "离开通话；接通后调用")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<Boolean> leave(@RequestParam("roomName") String roomName) {
        rtcCallService.leave(getLoginUserId(), roomName);
        return success(true);
    }

    @GetMapping("/refresh-token")
    @Operation(summary = "重新签发 Token；客户端重连或 Token 过期续期")
    @Parameter(name = "roomName", description = "LiveKit 房间名", required = true, example = "call_friend_1_2")
    public CommonResult<ImRtcCallRespVO> refreshToken(@RequestParam("roomName") String roomName) {
        return success(rtcCallService.refreshToken(getLoginUserId(), roomName));
    }

    @GetMapping("/group-active-call")
    @Operation(summary = "查询群当前进行中的通话；用于群聊顶部「N 人正在通话」胶囊条")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "2048")
    public CommonResult<ImRtcGroupCallRespVO> getGroupActiveCall(@RequestParam("groupId") Long groupId) {
        return success(rtcCallService.getGroupActiveCall(getLoginUserId(), groupId));
    }

}
