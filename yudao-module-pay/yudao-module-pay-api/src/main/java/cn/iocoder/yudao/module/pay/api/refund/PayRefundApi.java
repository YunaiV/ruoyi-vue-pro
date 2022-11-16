package cn.iocoder.yudao.module.pay.api.refund;

import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;

import javax.validation.Valid;

/**
 * 退款单 API 接口
 *
 * @author 芋道源码
 */
public interface PayRefundApi {

    /**
     * 创建退款单
     *
     * @param reqDTO 创建请求
     * @return 退款单编号
     */
    Long createPayRefund(@Valid PayRefundCreateReqDTO reqDTO);

}
