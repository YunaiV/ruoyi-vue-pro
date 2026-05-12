package cn.iocoder.yudao.module.im.service.rtc;

import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteMoreReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;

import java.util.List;

/**
 * IM 实时通话 Service
 * <p>
 * 业务模型：一个好友对 / 一个群同时只能有一个进行中的通话；inviteCall 仅负责「创建新通话」，加入已有群通话走 {@link #joinCall}
 *
 * @author 芋道源码
 */
public interface ImRtcCallService {

    /**
     * 发起新通话；同好友对 / 同群已有进行中通话直接抛错（群通话场景应改走 {@link #joinCall}）
     *
     * @param userId 操作人编号；通常是当前登录用户
     * @param reqVO  请求参数（scene / mediaType / peerUserId 或 groupId）
     * @return 通话主表
     */
    ImRtcCallDO inviteCall(Long userId, ImRtcCallInviteReqVO reqVO);

    /**
     * 加入已有群通话：用于群胶囊条「加入」按钮；旁观者作为 ACTIVE_JOIN 加入，邀请池内成员转 JOINED
     *
     * @param userId 加入者用户编号
     * @param room   业务通话编号；从胶囊条 activeCall 拿
     * @return 通话主表
     */
    ImRtcCallDO joinCall(Long userId, String room);

    /**
     * 通话中追加成员：仅群通话场景可用；给新邀请人推 RTC_CALL(INVITE)
     *
     * @param userId 操作人编号；必须是当前会话参与者
     * @param reqVO  room + 新邀请的用户编号集合
     */
    void inviteMoreCall(Long userId, ImRtcCallInviteMoreReqVO reqVO);

    /**
     * 接听通话：私聊状态切换 INVITING → RUNNING；群通话直接加入已有 RUNNING 房间
     *
     * @return 通话主表
     */
    ImRtcCallDO acceptCall(Long userId, String room);

    /**
     * 拒绝通话；仅 INVITING 状态可拒；群通话拒绝等同于不参与，房间仍存在
     */
    void rejectCall(Long userId, String room);

    /**
     * 取消邀请；主叫在 INVITING 状态主动取消
     */
    void cancelCall(Long userId, String room);

    /**
     * 离开通话；RUNNING 状态下离开；私聊任一方离开 = 通话结束；群通话最后一人离开才结束
     */
    void leaveCall(Long userId, String room);

    /**
     * 校验通话活跃且本人是参与者；用于客户端重连或 Token 过期续期前的合法性检查
     * <p>
     * 仅做校验；签发新 Token 由 Controller 调 {@link #signCallToken} 完成
     *
     * @return 通话主表
     */
    ImRtcCallDO validateCallParticipant(Long userId, String room);

    /**
     * 查询当前正在进行的通话；目前仅群聊场景（胶囊条），私聊未来扩展再补 peerUserId 参数
     * <p>
     * 鉴权：仅群活跃成员可查；防止任意用户探测群通话状态 / 拿到 inviter / inviteeIds 等敏感信息
     *
     * @param userId  操作人编号；通常是当前登录用户
     * @param groupId 群编号
     * @return 通话主表；不存在返回 null
     */
    ImRtcCallDO getActiveCall(Long userId, Long groupId);

    /**
     * 查询某通话的全部参与者明细；交给 Controller 拼装 inviteeIds / joinedUserIds
     *
     * @param room 业务通话编号
     * @return 参与者明细列表
     */
    List<ImRtcParticipantDO> getCallParticipantList(String room);

    /**
     * 签发指定用户进入该通话的 LiveKit Token；供 Controller 拼接到响应 VO
     *
     * @param userId 进房用户编号；token 内 displayName 取该用户昵称
     * @param room   业务通话编号
     * @return JWT 字符串
     */
    String signCallToken(Long userId, String room);

    /**
     * 处理 LiveKit Webhook 事件；用于关 tab / 强杀 / 网络断开等异常退出场景的兜底清理
     * <p>
     * 关键事件：participant_left（成员离开） / room_finished（房间结束）。前端正常 leave 时
     * 也会触发同样的 LiveKit 事件；此处需做幂等处理，session 已被业务接口移除时直接忽略。
     */
    void handleLiveKitEvent(cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitWebhookEventDTO event);

    /**
     * 【定时任务调用】清理僵尸通话
     *
     * @param thresholdMinutes 通话创建超过此分钟数才纳入扫描；调用方保证 > 0
     * @return 清理数量
     */
    int cleanupZombieCalls(int thresholdMinutes);

}
