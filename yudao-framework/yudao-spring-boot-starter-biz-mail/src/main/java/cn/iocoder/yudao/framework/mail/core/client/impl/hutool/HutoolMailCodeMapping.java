package cn.iocoder.yudao.framework.mail.core.client.impl.hutool;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.mail.core.client.MailCodeMapping;

/**
 * 阿里云的 SmsCodeMapping 实现类
 *
 * 参见 https://help.aliyun.com/document_detail/101346.htm 文档
 *
 * @author 芋道源码
 */
public class HutoolMailCodeMapping implements MailCodeMapping {

    @Override
    public ErrorCode apply(String apiCode) {
        return null;
    }

}
