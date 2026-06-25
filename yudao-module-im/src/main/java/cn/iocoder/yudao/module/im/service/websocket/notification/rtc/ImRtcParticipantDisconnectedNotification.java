package cn.iocoder.yudao.module.im.service.websocket.notification.rtc;

import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import lombok.Data;

/**
 * RTC_PARTICIPANT_DISCONNECTED 通话参与者离开通知
 * <p>
 * 不入库；LiveKit webhook participant_left 触发；推送范围同 {@link ImRtcParticipantConnectedNotification}
 * <p>
 * 前端 callStore 把 userId 从 joinedUserIds 移除；胶囊条人数 -1
 *
 * @author 芋道源码
 */
@Data
public class ImRtcParticipantDisconnectedNotification {

    /**
     * 业务通话编号
     */
    private String room;
    /**
     * 离开的参与者用户编号
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
     * 构造参与者离开通知；按 {@link ImRtcCallDO} 抽通话上下文，仅 userId 是变量
     *
     * @param call    通话主表
     * @param userId  离开的参与者用户编号
     * @return 通知载荷
     */
    public static ImRtcParticipantDisconnectedNotification of(ImRtcCallDO call, Long userId) {
        ImRtcParticipantDisconnectedNotification notification = new ImRtcParticipantDisconnectedNotification();
        notification.room = call.getRoom();
        notification.userId = userId;
        notification.conversationType = call.getConversationType();
        notification.groupId = call.getGroupId();
        return notification;
    }

}
