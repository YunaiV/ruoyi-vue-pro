package cn.iocoder.yudao.module.im.service.websocket.notification.rtc;

import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantStatusEnum;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * RTC_CALL 通话信令通知（通话信令统一入口）
 * <p>
 * 不入库；走 imWebSocketService 仅推参与方
 * <p>
 * status 字段复用 {@link ImRtcParticipantStatusEnum}，表达「本次信令对应的参与者状态变迁」
 *
 * @author 芋道源码
 */
@Data
public class ImRtcCallNotification {

    /**
     * 信令对应的参与者状态
     *
     * 取值参见 {@link ImRtcParticipantStatusEnum}
     */
    private Integer status;

    /**
     * 业务通话编号
     */
    private String room;
    /**
     * 会话类型
     */
    private Integer conversationType;
    /**
     * 媒体类型
     */
    private Integer mediaType;
    /**
     * 群编号：群通话场景必填
     */
    private Long groupId;

    // ========== INVITE 专属字段 ==========

    /**
     * LiveKit Server WebSocket 地址；INVITE 专属
     */
    private String livekitUrl;
    /**
     * 该被叫专属的 LiveKit 接听 Token；接通后直接 connect 用；INVITE 专属
     */
    private String token;
    /**
     * 发起人用户编号；INVITE 专属
     */
    private Long inviterUserId;
    /**
     * 发起人昵称；INVITE 专属，前端来电界面展示
     */
    private String inviterNickname;
    /**
     * 发起人头像；INVITE 专属，前端来电界面展示
     */
    private String inviterAvatar;
    /**
     * 本次被邀请人列表；INVITE 专属，前端来电界面展示「邀请的其他人」
     *
     * 注意：包含收件人自身，前端按需过滤
     */
    private List<Long> inviteeIds;

    // ========== REJECT 专属字段 ==========

    /**
     * 操作者用户编号；REJECT 触发本次状态变迁的人
     */
    private Long operatorUserId;
    /**
     * 操作者昵称；前端按需展示（被某某拒接）；普通文案不依赖
     */
    private String operatorNickname;
    /**
     * 操作者头像；前端按需展示；普通文案不依赖
     */
    private String operatorAvatar;

    /**
     * 构造 INVITE 信令；推被邀请人，invitee 状态变为 INVITING
     *
     * @param call        通话主表
     * @param inviter     发起人；可空，缺失时 inviterNickname / inviterAvatar 留空
     * @param livekitUrl  LiveKit Server WebSocket 地址
     * @param token       被叫的接听 Token；按收件人单独签发
     * @param inviteeIds  本次被邀请人列表；前端来电界面展示「邀请的其他人」用
     * @return INVITE 信令
     */
    public static ImRtcCallNotification ofInvite(ImRtcCallDO call, AdminUserRespDTO inviter,
                                                 String livekitUrl, String token,
                                                 Collection<Long> inviteeIds) {
        ImRtcCallNotification notification = baseOf(call, ImRtcParticipantStatusEnum.INVITING.getStatus());
        notification.livekitUrl = livekitUrl;
        notification.token = token;
        notification.inviterUserId = call.getInviterUserId();
        if (inviter != null) {
            notification.inviterNickname = inviter.getNickname();
            notification.inviterAvatar = inviter.getAvatar();
        }
        notification.inviteeIds = inviteeIds != null ? new java.util.ArrayList<>(inviteeIds) : null;
        return notification;
    }

    /**
     * 构造 REJECT 信令；仅群通话场景；推主叫
     *
     * @param call            通话主表
     * @param operatorUserId  拒接者用户编号
     * @param operator        拒接者；可空，缺失时 operatorNickname / operatorAvatar 留空
     * @return REJECT 信令
     */
    public static ImRtcCallNotification ofReject(ImRtcCallDO call, Long operatorUserId, AdminUserRespDTO operator) {
        ImRtcCallNotification notification = baseOf(call, ImRtcParticipantStatusEnum.REJECTED.getStatus());
        notification.operatorUserId = operatorUserId;
        if (operator != null) {
            notification.operatorNickname = operator.getNickname();
            notification.operatorAvatar = operator.getAvatar();
        }
        return notification;
    }

    /**
     * 构造 NO_ANSWER 信令；仅群通话场景；推主叫；超时未接听语义独立于 REJECT
     *
     * @param call           通话主表
     * @param operatorUserId 未接听者用户编号
     * @param operator       未接听者；可空，缺失时 operatorNickname / operatorAvatar 留空
     * @return NO_ANSWER 信令
     */
    public static ImRtcCallNotification ofNoAnswer(ImRtcCallDO call, Long operatorUserId, AdminUserRespDTO operator) {
        ImRtcCallNotification notification = baseOf(call, ImRtcParticipantStatusEnum.NO_ANSWER.getStatus());
        notification.operatorUserId = operatorUserId;
        if (operator != null) {
            notification.operatorNickname = operator.getNickname();
            notification.operatorAvatar = operator.getAvatar();
        }
        return notification;
    }

    /**
     * 公共骨架；填充 call 上下文 + status
     */
    private static ImRtcCallNotification baseOf(ImRtcCallDO call, Integer status) {
        ImRtcCallNotification notification = new ImRtcCallNotification();
        notification.status = status;
        notification.room = call.getRoom();
        notification.conversationType = call.getConversationType();
        notification.mediaType = call.getMediaType();
        notification.groupId = call.getGroupId();
        return notification;
    }

}
