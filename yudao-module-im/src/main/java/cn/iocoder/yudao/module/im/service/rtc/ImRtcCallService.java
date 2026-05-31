package cn.iocoder.yudao.module.im.service.rtc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.rtc.vo.ImRtcCallManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;

import java.util.List;

/**
 * IM 实时通话 Service
 *
 * @author 芋道源码
 */
public interface ImRtcCallService {

    /**
     * 创建新通话；同好友对 / 同群已有进行中通话直接抛错（群场景应改走 {@link #inviteCall} 追加邀请，或 {@link #joinCall} 加入旁观）
     *
     * @param userId 发起人编号；通常是当前登录用户
     * @param reqVO  请求参数（scene / mediaType / peerUserId 或 groupId + inviteeIds）
     * @return 通话主表
     */
    ImRtcCallDO createCall(Long userId, ImRtcCallCreateReqVO reqVO);

    /**
     * 通话中追加邀请：仅群通话场景可用；本人必须是房内 JOINED 参与者；给新邀请人推 RTC_CALL(INVITE)
     *
     * @param userId 操作人编号；必须是当前会话参与者
     * @param reqVO  room + 新邀请的用户编号集合
     */
    void inviteCall(Long userId, ImRtcCallInviteReqVO reqVO);

    /**
     * 加入已有群通话：用于群胶囊条「加入」按钮；旁观者作为 JOINER 加入，邀请池内成员转 JOINED
     *
     * @param userId 加入者用户编号
     * @param room   业务通话编号；从胶囊条 activeCall 拿
     * @return 通话主表
     */
    ImRtcCallDO joinCall(Long userId, String room);

    /**
     * 接听通话：参与者 INVITING → JOINED；主表 CREATED → RUNNING（首次有非发起人接通时）
     *
     * @param userId 接听者用户编号
     * @param room   业务通话编号
     * @return 通话主表
     */
    ImRtcCallDO acceptCall(Long userId, String room);

    /**
     * 拒绝通话；仅 INVITING 状态可拒；群通话拒绝等同于不参与，房间仍存在
     *
     * @param userId 拒接者用户编号
     * @param room   业务通话编号
     */
    void rejectCall(Long userId, String room);

    /**
     * 取消邀请；主叫在 INVITING 状态主动取消
     *
     * @param userId 取消者用户编号（必须是主叫）
     * @param room   业务通话编号
     */
    void cancelCall(Long userId, String room);

    /**
     * 离开通话；RUNNING 状态下离开；私聊任一方离开 = 通话结束；群通话最后一人离开才结束
     *
     * @param userId 离开者用户编号
     * @param room   业务通话编号
     */
    void leaveCall(Long userId, String room);

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
     * 关键事件：participant_left（成员离开） / room_finished（房间结束）。
     * 前端正常 leave 时，也会触发同样的 LiveKit 事件；此处需做幂等处理，session 已被业务接口移除时直接忽略。
     *
     * @param event LiveKit Webhook 事件
     */
    void handleLiveKitEvent(cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitWebhookEventDTO event);

    /**
     * 【定时任务调用】清理僵尸通话
     *
     * @param thresholdMinutes 通话创建超过此分钟数才纳入扫描；调用方保证 > 0
     * @return 清理数量
     */
    int cleanupZombieCalls(int thresholdMinutes);

    /**
     * 【定时任务调用】超时未接通的 INVITING 参与者：单人粒度标 NO_ANSWER + 推 RTC_CALL(NO_ANSWER) 让前端 banner 收敛；
     * 若导致通话只剩主叫，由 endSessionIfTerminal 级联关房
     *
     * @param thresholdMinutes 邀请时间超过此分钟数才纳入扫描；调用方保证 > 0
     * @return 超时处理数量
     */
    int timeoutInvitingParticipants(int thresholdMinutes);

    /**
     * 前端 RUNNING 端 timer 兜底；立即扫描指定 room 内超时的 INVITING 参与者，等同 Job 但限定单 room；
     * 实际超时阈值由后端 {@link cn.iocoder.yudao.module.im.framework.config.ImProperties.Rtc#getInviteTimeoutMinutes()} 决定，
     * 避免前后端配置不一致；接口静默，所有边界（room 不存在 / 鉴权失败 / 无超时候选）都返回 false 不抛异常
     *
     * @param userId 调用者用户编号；必须是该 room 的参与者
     * @param room   业务通话编号
     */
    void noAnswerCallCheck(Long userId, String room);

    // ==================== 管理后台 ====================

    /**
     * 【管理后台】获得通话记录分页
     *
     * @param reqVO 分页查询条件
     * @return 通话记录分页
     */
    PageResult<ImRtcCallDO> getCallPage(ImRtcCallManagerPageReqVO reqVO);

    /**
     * 【管理后台】获得通话记录
     *
     * @param id 通话编号
     * @return 通话记录
     */
    ImRtcCallDO getCall(Long id);

    /**
     * 【管理后台】按通话编号查询参与者明细
     *
     * @param id 通话编号
     * @return 参与者明细列表；通话不存在时返回空集合
     */
    List<ImRtcParticipantDO> getCallParticipantListByCallId(Long id);

}
