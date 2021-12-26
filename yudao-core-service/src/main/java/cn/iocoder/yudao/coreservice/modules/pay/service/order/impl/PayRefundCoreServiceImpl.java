package cn.iocoder.yudao.coreservice.modules.pay.service.order.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.coreservice.modules.pay.convert.order.PayRefundCoreConvert;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderExtensionCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayRefundCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.notify.PayNotifyCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.notify.dto.PayNotifyTaskCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayRefundPostReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayRefundReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundNotifyDTO;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundTypeEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayAppCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayChannelCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundChannelPostHandler;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayRefundRespDTO;
import cn.iocoder.yudao.coreservice.modules.pay.util.PaySeqUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayNotifyRefundStatusEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Slf4j
public class PayRefundCoreServiceImpl implements PayRefundCoreService {

    @Resource
    private PayOrderCoreMapper payOrderCoreMapper;
    @Resource
    private PayRefundCoreMapper payRefundCoreMapper;
    @Resource
    private PayOrderExtensionCoreMapper payOrderExtensionCoreMapper;

    @Resource
    private PayAppCoreService payAppCoreService;
    @Resource
    private PayChannelCoreService payChannelCoreService;
    @Resource
    private PayNotifyCoreService payNotifyCoreService;

    @Resource
    private PayClientFactory payClientFactory;

    /**
     * 处理渠道返回结果的后置处理器 集合
     */
    @Resource
    private List<PayRefundChannelPostHandler> handlerList;

    private final EnumMap<PayChannelRespEnum, PayRefundChannelPostHandler> mapHandlers = new EnumMap<>(PayChannelRespEnum.class);

    @PostConstruct
    public void init(){
        if (Objects.nonNull(handlerList)) {
            handlerList.forEach(handler -> {
                for (PayChannelRespEnum item : handler.supportHandleResp()) {
                    mapHandlers.put(item, handler);
                }
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayRefundRespDTO submitRefundOrder(PayRefundReqDTO req) {
        // 获得 PayOrderDO
        PayOrderDO order = payOrderCoreMapper.selectById(req.getPayOrderId());
        // 校验订单是否存在
        if (Objects.isNull(order) ) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        // 校验 App
        PayAppDO app = payAppCoreService.validPayApp(order.getAppId());
        // 校验支付渠道是否有效
        PayChannelDO channel = payChannelCoreService.validPayChannel(order.getChannelId());
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[refund][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }

        // 校验退款的条件
        validatePayRefund(req, order);

        // 退款类型
        PayRefundTypeEnum refundType = PayRefundTypeEnum.SOME;
        if (Objects.equals(req.getAmount(), order.getAmount())) {
            refundType = PayRefundTypeEnum.ALL;
        }
        // 退款单入库 退款单状态：生成,  没有和渠道产生交互
        PayOrderExtensionDO orderExtensionDO = payOrderExtensionCoreMapper.selectById(order.getSuccessExtensionId());
        PayRefundDO refundDO = PayRefundDO.builder().channelOrderNo(order.getChannelOrderNo())
                .appId(order.getAppId())
                .channelOrderNo(order.getChannelOrderNo())
                .channelCode(order.getChannelCode())
                .channelId(order.getChannelId())
                .merchantId(order.getMerchantId())
                .orderId(order.getId())
                .merchantRefundNo(req.getMerchantRefundNo())
                .notifyUrl(app.getRefundNotifyUrl())
                .payAmount(order.getAmount())
                .refundAmount(req.getAmount())
                .userIp(req.getUserIp())
                .merchantOrderId(order.getMerchantOrderId())
                .tradeNo(orderExtensionDO.getNo())
                .status(PayRefundStatusEnum.CREATE.getStatus())
                .reason(req.getReason())
                .notifyStatus(PayOrderNotifyStatusEnum.NO.getStatus())
                .reqNo(PaySeqUtils.genRefundReqNo())
                .type(refundType.getStatus())
                .build();
         payRefundCoreMapper.insert(refundDO);

        // 调用渠道进行退款
         PayRefundUnifiedReqDTO unifiedReqDTO = PayRefundUnifiedReqDTO.builder()
                .userIp(req.getUserIp())
                .channelOrderNo(refundDO.getChannelOrderNo())
                .payTradeNo(refundDO.getTradeNo())
                .refundReqNo(refundDO.getReqNo())
                .amount(req.getAmount())
                .reason(refundDO.getReason())
                .build();
         PayRefundUnifiedRespDTO refundUnifiedRespDTO = client.unifiedRefund(unifiedReqDTO);
         // 根据渠道返回，获取退款后置处理，由postHandler 进行处理
         PayRefundChannelPostHandler payRefundChannelPostHandler = mapHandlers.get(refundUnifiedRespDTO.getRespEnum());
         if (Objects.isNull(payRefundChannelPostHandler)) {
             throw exception(PAY_REFUND_POST_HANDLER_NOT_FOUND);
         }
         PayRefundPostReqDTO dto = PayRefundCoreConvert.INSTANCE.convert(refundUnifiedRespDTO);
         dto.setRefundAmount(req.getAmount())
            .setRefundedAmount(order.getRefundAmount())
            .setRefundedTimes(order.getRefundTimes())
            .setRefundId(refundDO.getId())
            .setOrderId(order.getId())
            .setRefundTypeEnum(refundType);
         //调用退款的后置处理
         payRefundChannelPostHandler.handleRefundChannelResp(dto);

         return PayRefundRespDTO.builder().refundId(refundDO.getId()).build();
    }


    @Override
    public void notifyPayRefund(Long channelId, PayNotifyDataDTO notifyData) {
        log.info("[notifyPayRefund][channelId({}) 回调数据({})]", channelId, notifyData.getBody());
        // 校验支付渠道是否有效
        PayChannelDO channel = payChannelCoreService.validPayChannel(channelId);
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[notifyPayOrder][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }
        // 解析渠道退款通知数据， 统一处理
        PayRefundNotifyDTO refundNotify = client.parseRefundNotify(notifyData);

        // TODO @jason：抽一个 notifyPayRefundSuccess 方法
        if (Objects.equals(PayNotifyRefundStatusEnum.SUCCESS,refundNotify.getStatus())){
            // 退款成功。 支付宝只有退款成功才会发通知
            PayRefundDO refundDO = payRefundCoreMapper.selectByReqNo(refundNotify.getReqNo());
            if (refundDO == null) {
                log.error("不存在 seqNo 为{} 的支付退款单",refundNotify.getReqNo());
                throw exception(PAY_REFUND_NOT_FOUND);
            }
            Long refundAmount = refundDO.getRefundAmount();
            Integer type = refundDO.getType();
            PayOrderStatusEnum orderStatus = PayOrderStatusEnum.SUCCESS;
            if(PayRefundTypeEnum.ALL.getStatus().equals(type)){
                orderStatus = PayOrderStatusEnum.CLOSED;
            }
            // 更新支付订单
            PayOrderDO payOrderDO = payOrderCoreMapper.selectById(refundDO.getOrderId());
            // 需更新已退金额
            Long refundedAmount = payOrderDO.getRefundAmount();
            PayOrderDO updateOrderDO = new PayOrderDO();
            updateOrderDO.setId(refundDO.getOrderId())
                    .setRefundAmount(refundedAmount + refundAmount)
                    .setStatus(orderStatus.getStatus())
                    .setRefundStatus(type);
            payOrderCoreMapper.updateById(updateOrderDO);

            // 跟新退款订单
            PayRefundDO updateRefundDO = new PayRefundDO();
            updateRefundDO.setId(refundDO.getId())
                    .setSuccessTime(refundNotify.getRefundSuccessTime())
                    .setChannelRefundNo(refundNotify.getChannelOrderNo())
                    .setTradeNo(refundNotify.getTradeNo())
                    .setNotifyTime(new Date())
                    .setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
            payRefundCoreMapper.updateById(updateRefundDO);

            //插入退款通知记录
            // TODO 通知商户成功或者失败. 现在通知似乎没有实现， 只是回调
            payNotifyCoreService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
                    .type(PayNotifyTypeEnum.REFUND.getType()).dataId(refundDO.getId()).build());
        } else {
            //TODO 退款失败
        }
    }

    /**
     * 校验是否进行退款
     * @param req 退款申请信息
     * @param order 原始支付订单信息
     */
    private void validatePayRefund(PayRefundReqDTO req, PayOrderDO order) {
        // 校验状态，必须是支付状态
        if (!PayOrderStatusEnum.SUCCESS.getStatus().equals(order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_SUCCESS);
        }
        //是否已经全额退款
        if (PayRefundTypeEnum.ALL.getStatus().equals(order.getRefundStatus())) {
            throw exception(PAY_REFUND_ALL_REFUNDED);
        }
        // 校验金额 退款金额不能大于 原定的金额
        if(req.getAmount() + order.getRefundAmount() > order.getAmount()){
            throw exception(PAY_REFUND_AMOUNT_EXCEED);
        }
        // 校验渠道订单号
        if (StrUtil.isEmpty(order.getChannelOrderNo())) {
            throw exception(PAY_REFUND_CHN_ORDER_NO_IS_NULL);
        }
        //TODO  退款的期限  退款次数的控制
    }

}
