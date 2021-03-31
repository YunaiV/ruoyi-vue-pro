package cn.iocoder.dashboard.framework.sms.core.client.dto;

import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
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
     * 短信发送状态 {@link SysSmsSendStatusEnum}
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
