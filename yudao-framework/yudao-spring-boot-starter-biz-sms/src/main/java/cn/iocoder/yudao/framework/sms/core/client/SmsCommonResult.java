package cn.iocoder.yudao.framework.sms.core.client;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;
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

    public static <T> SmsCommonResult<T> build(String apiCode, String apiMsg, String apiRequestId,
                                               T data, SmsCodeMapping codeMapping) {
        Assert.notNull(codeMapping, "参数 codeMapping 不能为空");
        SmsCommonResult<T> result = new SmsCommonResult<T>().setApiCode(apiCode).setApiMsg(apiMsg).setApiRequestId(apiRequestId);
        result.setData(data);
        // 翻译错误码
        if (codeMapping != null) {
            ErrorCode errorCode = codeMapping.apply(apiCode);
            if (errorCode == null) {
                errorCode = SmsFrameworkErrorCodeConstants.SMS_UNKNOWN;
            }
            result.setCode(errorCode.getCode()).setMsg(errorCode.getMsg());
        }
        return result;
    }

    public static <T> SmsCommonResult<T> error(Throwable ex) {
        SmsCommonResult<T> result = new SmsCommonResult<>();
        result.setCode(SmsFrameworkErrorCodeConstants.EXCEPTION.getCode());
        result.setMsg(ExceptionUtil.getRootCauseMessage(ex));
        return result;
    }

}
