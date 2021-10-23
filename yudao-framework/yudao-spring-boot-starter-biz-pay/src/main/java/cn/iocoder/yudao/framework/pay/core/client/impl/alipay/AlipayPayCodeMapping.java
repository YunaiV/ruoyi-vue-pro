package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.pay.core.client.AbstractPayCodeMapping;

/**
 * 支付宝的 PayCodeMapping 实现类
 *
 * @author 芋道源码
 */
public class AlipayPayCodeMapping extends AbstractPayCodeMapping {

    @Override
    protected ErrorCode apply0(String apiCode, String apiMsg) {
        return null;
    }

}
