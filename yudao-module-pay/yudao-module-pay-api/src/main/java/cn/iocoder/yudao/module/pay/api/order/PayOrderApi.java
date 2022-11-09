package cn.iocoder.yudao.module.pay.api.order;

import javax.validation.Valid;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface PayOrderApi {


    /**
     * 创建支付单
     *
     * @param reqDTO 创建请求
     * @return 支付单编号
     */
    Long createPayOrder(@Valid PayOrderDataCreateReqDTO reqDTO);

}
