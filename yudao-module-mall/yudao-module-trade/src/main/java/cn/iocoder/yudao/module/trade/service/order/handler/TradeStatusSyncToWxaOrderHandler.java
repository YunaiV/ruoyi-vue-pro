package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaOrderNotifyConfirmReceiveReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同步订单状态到微信小程序的 {@link TradeOrderHandler} 实现类
 *
 * 背景：电商类目的微信小程序需要上传发货信息，不然微信支付会被封 = =！
 * 注意：微信小程序开发环境下的订单不能用来发货。只有小程序正式版才会有发货，所以体验版无法调通，提示订单不存在。注意别踩坑。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "yudao.trade.order", value = "status-sync-to-wxa-enable")
public class TradeStatusSyncToWxaOrderHandler implements TradeOrderHandler {

    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private SocialClientApi socialClientApi;

    @Resource
    private DeliveryExpressService expressService;

    @Override
    public void afterDeliveryOrder(TradeOrderDO order) {
        // 注意：只有微信小程序支付的订单，才需要同步
        if (ObjUtil.notEqual(order.getPayChannelCode(), PayChannelEnum.WX_LITE.getCode())) {
            return;
        }
        PayOrderRespDTO payOrder = payOrderApi.getOrder(order.getPayOrderId());
        SocialWxaOrderUploadShippingInfoReqDTO reqDTO = new SocialWxaOrderUploadShippingInfoReqDTO()
                .setTransactionId(payOrder.getChannelOrderNo())
                .setOpenid(payOrder.getChannelUserId())
                .setItemDesc(payOrder.getSubject())
                .setReceiverContact(order.getReceiverMobile());
        if (DeliveryTypeEnum.EXPRESS.getType().equals(order.getDeliveryType()) && StrUtil.isNotEmpty(order.getLogisticsNo())) {
            reqDTO.setLogisticsType(SocialWxaOrderUploadShippingInfoReqDTO.LOGISTICS_TYPE_EXPRESS)
                    .setExpressCompany(expressService.getDeliveryExpress(order.getLogisticsId()).getCode())
                    .setLogisticsNo(order.getLogisticsNo());
        } else if (DeliveryTypeEnum.PICK_UP.getType().equals(order.getDeliveryType())) {
            reqDTO.setLogisticsType(SocialWxaOrderUploadShippingInfoReqDTO.LOGISTICS_TYPE_PICK_UP);
        } else {
            reqDTO.setLogisticsType(SocialWxaOrderUploadShippingInfoReqDTO.LOGISTICS_TYPE_VIRTUAL);
        }
        try {
            socialClientApi.uploadWxaOrderShippingInfo(UserTypeEnum.MEMBER.getValue(), reqDTO);
        } catch (Exception ex) {
            log.error("[afterDeliveryOrder][订单({}) 上传订单物流信息到微信小程序失败]", order, ex);
        }
    }

    @Override
    public void afterReceiveOrder(TradeOrderDO order) {
        // 注意：只有微信小程序支付的订单，才需要同步
        if (ObjUtil.notEqual(order.getPayChannelCode(), PayChannelEnum.WX_LITE.getCode())) {
            return;
        }
        PayOrderRespDTO payOrder = payOrderApi.getOrder(order.getPayOrderId());
        SocialWxaOrderNotifyConfirmReceiveReqDTO reqDTO = new SocialWxaOrderNotifyConfirmReceiveReqDTO()
                .setTransactionId(payOrder.getChannelOrderNo())
                .setReceivedTime(order.getReceiveTime());
        try {
            socialClientApi.notifyWxaOrderConfirmReceive(UserTypeEnum.MEMBER.getValue(), reqDTO);
        } catch (Exception ex) {
            log.error("[afterReceiveOrder][订单({}) 通知订单收货到微信小程序失败]", order, ex);
        }
    }

    // TODO @芋艿：【设置路径】 https://developers.weixin.qq.com/miniprogram/dev/platform-capabilities/business-capabilities/order-shipping/order-shipping.html#%E5%85%AD%E3%80%81%E6%B6%88%E6%81%AF%E8%B7%B3%E8%BD%AC%E8%B7%AF%E5%BE%84%E8%AE%BE%E7%BD%AE%E6%8E%A5%E5%8F%A3

}
