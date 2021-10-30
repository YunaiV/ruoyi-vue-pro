package cn.iocoder.yudao.framework.pay.core.client.impl.wx;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.pay.core.client.AbstractPayCodeMapping;

import java.util.Objects;

import static cn.iocoder.yudao.framework.pay.core.enums.PayFrameworkErrorCodeConstants.*;

/**
 * 微信支付 PayCodeMapping 实现类
 *
 * @author 芋道源码
 */
public class WXCodeMapping extends AbstractPayCodeMapping {

    /**
     * 错误码 - 成功
     * 由于 weixin-java-pay 封装的 Result 未返回 code，所以自己定义下
     */
    public static final String CODE_SUCCESS = "SUCCESS";
    /**
     * 错误提示 - 成功
     */
    public static final String MESSAGE_SUCCESS = "成功";

    @Override
    protected ErrorCode apply0(String apiCode, String apiMsg) {
        if (Objects.equals(apiCode, CODE_SUCCESS)) {
            return GlobalErrorCodeConstants.SUCCESS;
        }
        if (Objects.equals(apiCode, "FAIL")) {
            if (Objects.equals(apiMsg, "AppID不存在，请检查后再试")) {
                return PAY_CONFIG_APP_ID_ERROR;
            }
            if (Objects.equals(apiMsg, "签名错误，请检查后再试")
                || Objects.equals(apiMsg, "签名错误")) {
                return PAY_CONFIG_SIGN_ERROR;
            }
        }
        if (Objects.equals(apiCode, "PARAM_ERROR")) {
            if (Objects.equals(apiMsg, "无效的openid")) {
                return PAY_OPENID_ERROR;
            }
        }
        if (Objects.equals(apiCode, "CustomErrorCode")) {
            if (StrUtil.contains(apiMsg, "必填字段")) {
                return PAY_PARAM_MISSING;
            }
        }
        return null;
    }

}
