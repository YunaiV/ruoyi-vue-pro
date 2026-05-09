package cn.iocoder.yudao.module.im.service.rtc;

import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteMoreReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallRespVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcGroupCallRespVO;

import java.util.List;

// TODO @AI：一个好友，只能一个通话；一个群，只能一个通话；（同一时间）；
/**
 * IM 实时通话 Service
 * <p>
 * 业务模型：一个好友对 / 一个群同时只能有一个进行中的通话；invite 兼具「创建」与「加入已有」两种语义。
 * 媒体协商完全交给 LiveKit；后端只做：会话状态机、Token 签发、来电信令推送、通话历史落消息流。
 *
 * @author 芋道源码
 */
public interface ImRtcCallService {

    // TODO @AI：是不是风格上，还是有 create 和 invite；这样两种逻辑上更好理解；
    /**
     * 发起通话；同一 roomName 已有进行中通话时返回该会话并标记 newCreated=false
     *
     * @param userId 操作人编号；通常是当前登录用户
     * @param reqVO  请求参数（scene / mediaType / peerUserId 或 groupId）
     * @return 包含 token / roomName / livekitUrl 的会话信息
     */
    ImRtcCallRespVO invite(Long userId, ImRtcCallInviteReqVO reqVO);

    /**
     * 通话中追加成员；仅群通话场景可用；给新邀请人推 RTC_INVITE
     *
     * @param userId 操作人编号；必须是当前会话参与者
     * @param reqVO  房间名 + 新邀请的用户编号集合
     */
    void inviteMore(Long userId, ImRtcCallInviteMoreReqVO reqVO);

    /**
     * 接听通话；私聊状态切换 INVITING → ONGOING；群通话直接加入已有 ONGOING 房间
     */
    ImRtcCallRespVO accept(Long userId, String roomName);

    /**
     * 拒绝通话；仅 INVITING 状态可拒；群通话拒绝等同于不参与，房间仍存在
     */
    void reject(Long userId, String roomName);

    /**
     * 取消邀请；主叫在 INVITING 状态主动取消
     */
    void cancel(Long userId, String roomName);

    /**
     * 离开通话；ONGOING 状态下离开；私聊任一方离开 = 通话结束；群通话最后一人离开才结束
     */
    void leave(Long userId, String roomName);

    /**
     * 重新签发 Token；用于客户端重连或 Token 过期续期
     */
    ImRtcCallRespVO refreshToken(Long userId, String roomName);

    /**
     * 列出用户当前的活跃通话；用于 App 冷启动 / 推送点开恢复
     */
    List<ImRtcCallRespVO> getActiveSessions(Long userId);

    /**
     * 查询群当前正在进行的通话；用于群聊顶部「N 人正在通话」胶囊条
     *
     * @param groupId 群编号
     * @return 群活跃通话；不存在返回 null；不含 token，仅展示用
     */
    ImRtcGroupCallRespVO getGroupActiveCall(Long groupId);

    /**
     * 处理 LiveKit Webhook 事件；用于关 tab / 强杀 / 网络断开等异常退出场景的兜底清理
     * <p>
     * 关键事件：participant_left（成员离开） / room_finished（房间结束）。前端正常 leave 时
     * 也会触发同样的 LiveKit 事件；此处需做幂等处理，session 已被业务接口移除时直接忽略。
     */
    void handleLiveKitEvent(cn.iocoder.yudao.module.im.service.rtc.dto.LiveKitWebhookEventDTO event);

}
