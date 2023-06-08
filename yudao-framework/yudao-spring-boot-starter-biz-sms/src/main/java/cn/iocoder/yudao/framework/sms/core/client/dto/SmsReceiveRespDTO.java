package cn.iocoder.yudao.framework.sms.core.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息接收 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class SmsReceiveRespDTO {

    /**
     * 是否接收成功
     */
    private Boolean success;
    /**
     * API 接收结果的编码
     */
    private String errorCode;
    /**
     * API 接收结果的说明
     */
    private String errorMsg;

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 用户接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 短信 API 发送返回的序号
     */
    private String serialNo;
    /**
     * 短信日志编号
     *
     * 对应 SysSmsLogDO 的编号
     */
    private Long logId;

}
