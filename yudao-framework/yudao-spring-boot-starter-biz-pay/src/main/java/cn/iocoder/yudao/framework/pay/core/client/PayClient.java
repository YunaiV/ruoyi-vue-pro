package cn.iocoder.yudao.framework.pay.core.client;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;

/**
 * 支付客户端，用于对接各支付渠道的 SDK，实现发起支付、退款等功能
 *
 * @author 芋道源码
 */
public interface PayClient {

    // TODO 缺少注释
    CommonResult<Object> unifiedOrder(PayOrderUnifiedReqDTO reqDTO);

}
