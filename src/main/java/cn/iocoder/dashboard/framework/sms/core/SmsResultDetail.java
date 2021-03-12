package cn.iocoder.dashboard.framework.sms.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息内容实体类
 */
@Data
public class SmsResultDetail implements Serializable {

    /**
     * 唯一标识
     */
    private String apiId;

    /**
     * 短信发送状态 {@link cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum}
     */
    private Integer sendStatus;

    /**
     * 接收手机号
     */
    private String phone;

    /**
     * 提示
     */
    private String message;

    /**
     * 时间
     */
    private Date sendTime;

    /**
     * 接口返回值
     */
    private Object callbackResponseBody;
}
