package cn.iocoder.yudao.framework.mail.core.client;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.mail.core.enums.MailFrameworkErrorCodeConstants;

import java.util.function.Function;

/**
 * 将 API 的错误码，转换为通用的错误码
 *
 * @see MailCommonResult
 * @see MailFrameworkErrorCodeConstants
 *
 * @author 芋道源码
 */
public interface MailCodeMapping extends Function<String, ErrorCode> {
}
