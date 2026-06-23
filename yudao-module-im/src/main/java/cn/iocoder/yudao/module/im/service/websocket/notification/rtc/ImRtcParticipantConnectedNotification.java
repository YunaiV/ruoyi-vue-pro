package cn.iocoder.yudao.module.im.service.websocket.notification.rtc;

import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import lombok.Data;

/**
 * RTC_PARTICIPANT_CONNECTED 通话参与者加入通知
 * <p>
 * 不入库；LiveKit webhook participant_joined 触发；私聊推 peer 多端 + inviter 多端，群聊全群广播
 * <p>
 * 前端 callStore 把 userId 追加进 joinedUserIds；胶囊条人数 +1；群聊场景首条 1602 携带通话元信息以便 首次填充胶囊条
 *
 * @author 芋道源码
 */
@Data
public class ImRtcParticipantConnectedNotification {

    /**
     * 业务通话编号
     */
    private String room;
    /**
     * 加入的参与者用户编号
     */
    private Long userId;
    /**
     * 会话类型
     */
    private Integer conversationType;
    /**
     * 群编号；群通话场景必填
     */
    private Long groupId;
    /**
     * 媒体类型；群聊场景的非邀请成员靠这个字段 首次填充胶囊条
     */
    private Integer mediaType;
    /**
     * 发起人用户编号；群聊场景的非邀请成员靠这个字段 首次填充胶囊条
     */
    private Long inviterUserId;

    /**
     * 构造参与者加入通知；按 {@link ImRtcCallDO} 抽通话上下文，仅 userId 是变量
     *
     * @param call    通话主表
     * @param userId  加入的参与者用户编号
     * @return 通知载荷
     */
    public static ImRtcParticipantConnectedNotification of(ImRtcCallDO call, Long userId) {
        ImRtcParticipantConnectedNotification notification = new ImRtcParticipantConnectedNotification();
        notification.room = call.getRoom();
        notification.userId = userId;
        notification.conversationType = call.getConversationType();
        notification.groupId = call.getGroupId();
        notification.mediaType = call.getMediaType();
        notification.inviterUserId = call.getInviterUserId();
        return notification;
    }

}
