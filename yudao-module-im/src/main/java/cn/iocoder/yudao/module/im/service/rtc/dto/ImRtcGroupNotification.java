package cn.iocoder.yudao.module.im.service.rtc.dto;

import lombok.Data;

import java.util.Set;

// TODO @AI：注释风格；
/**
 * RTC 群通话广播载荷；让群里所有成员（不仅是被邀请人）能感知通话存在，并支持主动加入
 *
 * @author 芋道源码
 */
@Data
public class ImRtcGroupNotification {

    /** 业务通话编号 */
    private String callId;
    /** LiveKit 房间名 */
    private String roomName;
    /** 群编号 */
    private Long groupId;
    /** 媒体类型；1 语音；2 视频 */
    private Integer mediaType;
    /** 发起人编号 */
    private Long inviterId;
    /** 当前已加入房间的用户编号集合 */
    private Set<Long> joinedUserIds;
    /** 当前被邀请池；展开胶囊条时用来展示「待加入」灰色头像 */
    private Set<Long> inviteeIds;

}
