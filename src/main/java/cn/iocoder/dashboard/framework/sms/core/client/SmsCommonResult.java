package cn.iocoder.dashboard.framework.sms.core.client;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsSendFailureTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 短信的 CommonResult 拓展类
 *
 * 考虑到不同的平台，返回的 code 和 msg 是不同的，所以统一额外返回 {@link #apiCode} 和 {@link #apiMsg} 字段
 *
 * 另外，一些短信平台（例如说阿里云、腾讯云）会返回一个请求编号，用于排查请求失败的问题，我们设置到 {@link #apiRequestId} 字段
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsCommonResult<T> extends CommonResult<T> {

    /**
     * API 返回错误码
     *
     * 由于第三方的错误码可能是字符串，所以使用 String 类型
     */
    private String apiCode;
    /**
     * API 返回提示
     */
    private String apiMsg;

    /**
     * API 请求编号
     */
    private String apiRequestId;

    private SmsCommonResult() {
    }

    public static SmsCommonResult success(SmsSendFailureTypeEnum sendFailureType,
                                          String apiSendCode, String apiSendMsg, String apiRequestId, String apiSerialNo) {
        SmsCommonResult result = new SmsCommonResult().setSuccess(true).setApiSendCode(apiSendCode).setApiSendMsg(apiSendMsg)
                .setApiRequestId(apiRequestId).setApiSerialNo(apiSerialNo);
        if (sendFailureType != null) {
            result.setSendFailureType(sendFailureType.getType()).setSendFailureMsg(sendFailureType.getMsg());
        }
        return result;
    }

    public static SmsCommonResult error(Throwable ex) {
        return new SmsCommonResult().setSuccess(false)
                .setSendFailureType(SmsSendFailureTypeEnum.SMS_SEND_EXCEPTION.getType())
                .setSendFailureMsg(ExceptionUtil.getRootCauseMessage(ex));
    }

}
