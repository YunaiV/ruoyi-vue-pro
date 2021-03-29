package cn.iocoder.dashboard.framework.sms.core;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsSendFailureTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息内容实体类
 */
@Data
public class SmsResult implements Serializable {

    /**
     * 是否成功
     *
     * 注意，是调用 API 短信平台的请求是否成功
     */
    private Boolean success;
    /**
     * 发送失败的类型
     *
     * 枚举 {@link SmsSendFailureTypeEnum#getType()}
     */
    private Integer sendFailureType;
    /**
     * 发送失败的提示
     *
     * 一般情况下，使用 {@link SmsSendFailureTypeEnum#getMsg()}
     * 异常情况下，通过格式化 Exception 的提示存储
     */
    private String sendFailureMsg;

    /**
     * 短信 API 发送的错误码
     *
     * 由于第三方的错误码可能是字符串，所以使用 String 类型
     */
    private String apiSendCode;
    /**
     * 短信 API 发送的提示
     */
    private String apiSendMsg;
    /**
     * 短信 API 发送返回的唯一请求 ID
     *
     * 用于和短信 API 进行定位于排错
     */
    private String apiRequestId;
    /**
     * 短信 API 发送返回的序号
     *
     * 用于和短信 API 平台的发送记录关联
     */
    private String apiSerialNo;

    private SmsResult() {
    }

    public static SmsResult success(SmsSendFailureTypeEnum sendFailureType,
                                    String apiSendCode, String apiSendMsg, String apiRequestId, String apiSerialNo) {
        SmsResult result = new SmsResult().setSuccess(true).setApiSendCode(apiSendCode).setApiSendMsg(apiSendMsg)
                .setApiRequestId(apiRequestId).setApiSerialNo(apiSerialNo);
        if (sendFailureType != null) {
            result.setSendFailureType(sendFailureType.getType()).setSendFailureMsg(sendFailureType.getMsg());
        }
        return result;
    }

    public static SmsResult error(Throwable ex) {
        return new SmsResult().setSuccess(false)
                .setSendFailureType(SmsSendFailureTypeEnum.SMS_SEND_EXCEPTION.getType())
                .setSendFailureMsg(ExceptionUtil.getRootCauseMessage(ex));
    }

}
