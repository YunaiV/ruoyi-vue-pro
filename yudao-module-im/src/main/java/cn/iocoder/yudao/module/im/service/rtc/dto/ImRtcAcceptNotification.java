package cn.iocoder.yudao.module.im.service.rtc.dto;

import lombok.Data;

// TODO @AI：一些 notification 应该挪到 message 那边，你在
// TODO @AI：注释风格；
/**
 * RTC 接通通知；推给主叫与已在房成员，提示某用户已加入；前端把"等待对方接受"切换成通话中 UI
 *
 * @author 芋道源码
 */
@Data
public class ImRtcAcceptNotification {

    /** 业务通话编号 */
    // TODO @AI：是不是后续使用 id Long？（后续在改）
    private String callId;
    /** LiveKit 房间名 */
    private String roomName;
    /** 接听者编号 */
    private Long acceptorId;

}
