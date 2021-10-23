package cn.iocoder.yudao.coreservice.modules.pay.service.order;

import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitRespDTO;

import javax.validation.Valid;

/**
 * 支付订单 Core Service
 *
 * @author 芋道源码
 */
public interface PayOrderCoreService {

   /**
    * 创建支付单
    *
    * @param reqDTO 创建请求
    * @return 支付单编号
    */
   Long createPayOrder(@Valid PayOrderCreateReqDTO reqDTO);

   /**
    * 提交支付
    * 此时，会发起支付渠道的调用
    *
    * @param reqDTO 提交请求
    * @return 提交结果
    */
   PayOrderSubmitRespDTO submitPayOrder(@Valid PayOrderSubmitReqDTO reqDTO);

}
