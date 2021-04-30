package cn.iocoder.dashboard.framework.sms.core.client.impl.debug;

import cn.iocoder.yudao.common.exception.ErrorCode;
import cn.iocoder.yudao.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCodeMapping;

import java.util.Objects;

import static cn.iocoder.dashboard.framework.sms.core.enums.SmsFrameworkErrorCodeConstants.SMS_UNKNOWN;

/**
 * 钉钉的 SmsCodeMapping 实现类
 *
 * @author 芋道源码
 */
public class DebugDingTalkCodeMapping implements SmsCodeMapping {

    @Override
    public ErrorCode apply(String apiCode) {
        return Objects.equals(apiCode, "0") ? GlobalErrorCodeConstants.SUCCESS : SMS_UNKNOWN;
    }

}
