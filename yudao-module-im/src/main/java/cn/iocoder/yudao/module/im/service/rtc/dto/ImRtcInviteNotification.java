package cn.iocoder.yudao.module.im.service.rtc.dto;

import lombok.Data;

// TODO @AI：注释风格；
/**
 * RTC 来电邀请通知；推给被叫弹「来电」界面
 *
 * @author 芋道源码
 */
@Data
public class ImRtcInviteNotification {

    /** 业务通话编号 */
    private String callId;
    /** LiveKit 房间名 */
    private String roomName;
    /** LiveKit Server WebSocket 地址 */
    private String livekitUrl;
    /** 该被叫专属的 LiveKit 接听 Token；接通后直接 connect 用 */
    private String token;
    /** 会话场景；1 私聊；2 群聊 */
    private Integer scene;
    /** 媒体类型；1 语音；2 视频 */
    private Integer mediaType;
    /** 发起人编号 */
    private Long inviterId;
    /** 发起人昵称；前端来电界面展示 */
    private String inviterNickname;
    /** 发起人头像 */
    private String inviterAvatar;
    /** 群编号；群通话场景必填 */
    private Long groupId;

}
