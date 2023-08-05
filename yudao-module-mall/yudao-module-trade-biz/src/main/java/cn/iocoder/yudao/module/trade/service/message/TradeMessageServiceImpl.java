package cn.iocoder.yudao.module.trade.service.message;

import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.trade.service.message.dto.TradeOrderMessageWhenDeliveryOrderReqDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Trade 消息 service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class TradeMessageServiceImpl implements TradeMessageService {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public void sendMessageWhenDeliveryOrder(TradeOrderMessageWhenDeliveryOrderReqDTO reqDTO) {
        // 1、构造消息
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("orderId", reqDTO.getOrderId());
        msgMap.put("msg", reqDTO.getMessage());
        // 2、发送站内信
        notifyMessageSendApi.sendSingleMessageToMember(
                new NotifySendSingleToUserReqDTO()
                        .setUserId(reqDTO.getUserId())
                        .setTemplateCode("order_delivery")
                        .setTemplateParams(msgMap));
    }

}
