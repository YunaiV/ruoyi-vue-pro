package cn.iocoder.yudao.framework.pay.core.client;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.pay.core.enums.PayFrameworkErrorCodeConstants;

import java.util.function.Function;

/**
 * 将 API 的错误码，转换为通用的错误码
 *
 * @see PayCommonResult
 * @see PayFrameworkErrorCodeConstants
 *
 * @author 芋道源码
 */
public interface PayCodeMapping extends Function<String, ErrorCode> {
}
