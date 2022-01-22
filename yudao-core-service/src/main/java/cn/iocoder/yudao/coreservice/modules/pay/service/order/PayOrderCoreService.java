package cn.iocoder.yudao.coreservice.modules.pay.service.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;

import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.*;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;

import javax.validation.Valid;

/**
 * 支付订单 Core Service
 *
 * @author 芋道源码
 */
public interface PayOrderCoreService {

   /**
    * 获得支付单
    *
    * @param id 支付单编号
    * @return 支付单
    */
   PayOrderDO getPayOrder(Long id);

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

   /**
    * 通知支付单成功
    *
    * @param channelId 渠道编号
    * @param notifyData 通知数据
    */
   void notifyPayOrder(Long channelId,  PayNotifyDataDTO notifyData) throws Exception;

}
