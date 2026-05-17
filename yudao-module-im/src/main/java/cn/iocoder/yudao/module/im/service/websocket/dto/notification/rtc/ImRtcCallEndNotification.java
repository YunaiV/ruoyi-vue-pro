package cn.iocoder.yudao.module.im.service.websocket.dto.notification.rtc;

import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallEndReasonEnum;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.Data;

/**
 * RTC_CALL_END 通话结束通知
 * <p>
 * 入 im_private_message / im_group_message；接收方关闭通话窗 + 渲染聊天 tip
 * <p>
 * 文案分场景：
 * 群聊：「语音通话已经结束」；发起人信息走配对的 {@link ImRtcCallStartNotification}
 * 私聊：仿微信准气泡，按 endReason × selfSend 视角转换文案（HANGUP / CANCEL / REJECT / BUSY / ERROR）
 * <p>
 * 与 {@link ImRtcCallStartNotification} 两段式配对：START 在 invite 事务里 INSERT，END 在 cancel / leave 事务里 INSERT；
 * 两段位于不同请求 / 事务，自增 id 保证聊天流顺序
 *
 * @author 芋道源码
 */
@Data
public class ImRtcCallEndNotification {

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
     * 结束原因
     */
    private Integer endReason;
    /**
     * 通话时长（秒）：接通过为 endTime - acceptTime；未接通为 null
     */
    private Long durationSeconds;
    /**
     * 操作者用户编号：HANGUP / CANCEL / REJECT 是触发结束的人；HANGUP webhook 兜底为 null
     * <p>
     * 用于前端「被某某挂断」类文案；普通文案不依赖此字段
     */
    private Long operatorUserId;
    /**
     * 操作者昵称：前端按需展示（被某某挂断 / 头像 tip）；操作者为空时随之为空
     */
    private String operatorNickname;
    /**
     * 操作者头像：前端按需展示；操作者为空时随之为空
     */
    private String operatorAvatar;

    public static ImRtcCallEndNotification of(ImRtcCallDO call, ImRtcCallEndReasonEnum reason, Long durationSeconds,
                                              Long operatorId, AdminUserRespDTO operator) {
        ImRtcCallEndNotification notification = new ImRtcCallEndNotification();
        notification.room = call.getRoom();
        notification.conversationType = call.getConversationType();
        notification.mediaType = call.getMediaType();
        notification.endReason = reason.getReason();
        notification.durationSeconds = durationSeconds;
        notification.operatorUserId = operatorId;
        if (operator != null) {
            notification.operatorNickname = operator.getNickname();
            notification.operatorAvatar = operator.getAvatar();
        }
        return notification;
    }

}
