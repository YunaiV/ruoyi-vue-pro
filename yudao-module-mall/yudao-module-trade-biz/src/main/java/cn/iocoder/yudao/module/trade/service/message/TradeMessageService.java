package cn.iocoder.yudao.module.trade.service.message;

import cn.iocoder.yudao.module.trade.service.message.dto.TradeOrderMessageWhenDeliveryOrderReqDTO;

/**
 * Trade 消息 service 接口
 *
 * @author HUIHUI
 */
public interface TradeMessageService {

    /**
     * 订单发货时发送消息
     *
     * @param reqDTO 发送消息
     */
    void sendMessageWhenDeliveryOrder(TradeOrderMessageWhenDeliveryOrderReqDTO reqDTO);

}
