package cn.iocoder.yudao.module.im.service.rtc.dto;

import lombok.Data;

// TODO @AI：注释风格；
/**
 * RTC 结束通知；拒绝 / 取消 / 挂断 / 超时 / 异常 统一走这一条；前端关闭通话界面
 *
 * @author 芋道源码
 */
@Data
public class ImRtcEndNotification {

    /** 业务通话编号 */
    private String callId;
    /** LiveKit 房间名 */
    private String roomName;
    /** 操作者编号；TIMEOUT 等系统触发场景为 null */
    private Long operatorId;
    /** 结束原因；取自 {@link cn.iocoder.yudao.module.im.enums.rtc.ImCallEndReasonEnum} 的 type */
    private Integer reason;
    /** 通话时长（秒）；接通过为 endTime - acceptTime；未接通为 null */
    private Long durationSeconds;

}
