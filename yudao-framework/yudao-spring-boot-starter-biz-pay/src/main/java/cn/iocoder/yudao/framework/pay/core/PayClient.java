package cn.iocoder.yudao.framework.pay.core;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;

/**
 * 支付客户端，用于对接各支付渠道的 SDK，实现发起支付、退款等功能
 *
 * @author 芋道源码
 */
public interface PayClient {

    CommonResult<String> unifiedOrder();

}
