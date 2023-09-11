package cn.iocoder.yudao.module.pay.api.order;

import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;

import javax.validation.Valid;

/**
 * 支付单 API 接口
 *
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
    Long createOrder(@Valid PayOrderCreateReqDTO reqDTO);

    /**
     * 获得支付单
     *
     * @param id 支付单编号
     * @return 支付单
     */
    PayOrderRespDTO getOrder(Long id);

    // TODO @puhui999：可以去掉 byId；然后 payOrderId 参数改成 id；
    /**
     * 更新支付订单价格
     *
     * @param payOrderId 支付单编号
     * @param payPrice   支付单价格
     */
    void updatePayOrderPriceById(Long payOrderId, Integer payPrice);

}
