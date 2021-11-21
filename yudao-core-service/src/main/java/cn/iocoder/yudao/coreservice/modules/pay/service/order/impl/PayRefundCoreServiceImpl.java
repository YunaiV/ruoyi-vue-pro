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
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayRefundMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundTypeEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayAppCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayChannelCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundChannelPostHandler;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundPostReqBO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundReqBO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundRespBO;
import cn.iocoder.yudao.coreservice.modules.pay.util.PaySeqUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedRespDTO;
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
    private PayRefundMapper payRefundMapper;

    @Resource
    private PayOrderExtensionCoreMapper payOrderExtensionCoreMapper;

    @Resource
    private PayAppCoreService payAppCoreService;

    @Resource
    private PayChannelCoreService payChannelCoreService;

    @Resource
    private PayClientFactory payClientFactory;

    /**
     * 处理渠道返回结果的后置处理器 集合
     */
    @Resource
    private List<PayRefundChannelPostHandler> handlerList;


    private final EnumMap<PayChannelRespEnum, PayRefundChannelPostHandler> mapHandler = new EnumMap<>(PayChannelRespEnum.class);



    @PostConstruct
    public void init(){

        if (Objects.nonNull(handlerList)) {
            handlerList.forEach(t->{
                for (PayChannelRespEnum item : t.supportHandleResp()) {
                    mapHandler.put(item, t);
                }
            });
        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayRefundRespBO refund(PayRefundReqBO reqBO) {
        // 获得 PayOrderDO
        PayOrderDO order = payOrderCoreMapper.selectById(reqBO.getPayOrderId());
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

        //校验退款的条件
        validatePayRefund(reqBO, order);

        //退款类型
        PayRefundTypeEnum refundType = PayRefundTypeEnum.SOME;
        if (Objects.equals(reqBO.getAmount(), order.getAmount())) {
            refundType = PayRefundTypeEnum.ALL;
        }

        //退款单入库 退款单状态：生成,  没有和渠道产生交互
        PayOrderExtensionDO orderExtensionDO = payOrderExtensionCoreMapper.selectById(order.getSuccessExtensionId());
        PayRefundDO refundDO = PayRefundDO.builder().channelOrderNo(order.getChannelOrderNo())
                .appId(order.getAppId())
                .channelOrderNo(order.getChannelOrderNo())
                .channelCode(order.getChannelCode())
                .channelId(order.getChannelId())
                .merchantId(order.getMerchantId())
                .orderId(order.getId())
                .merchantRefundNo(reqBO.getMerchantRefundNo())
                .notifyUrl(app.getRefundNotifyUrl())
                .payAmount(order.getAmount())
                .refundAmount(reqBO.getAmount())
                .userIp(reqBO.getUserIp())
                .merchantOrderId(order.getMerchantOrderId())
                .tradeNo(orderExtensionDO.getNo())
                .status(PayRefundStatusEnum.CREATE.getStatus())
                .reason(reqBO.getReason())
                .notifyStatus(PayOrderNotifyStatusEnum.NO.getStatus())
                .reqNo(PaySeqUtils.genRefundReqNo())
                .type(refundType.getStatus())
                .build();

         payRefundMapper.insert(refundDO);

         PayRefundUnifiedReqDTO unifiedReqDTO = PayRefundUnifiedReqDTO.builder()
                .userIp(reqBO.getUserIp())
                .channelOrderNo(refundDO.getChannelOrderNo())
                .payTradeNo(refundDO.getTradeNo())
                .refundReqNo(refundDO.getReqNo())
                .amount(reqBO.getAmount())
                .reason(refundDO.getReason())
                .build();

         //调用渠道进行退款
         PayRefundUnifiedRespDTO refundUnifiedRespDTO = client.unifiedRefund(unifiedReqDTO);

        //根据渠道返回，获取退款后置处理，由postHandler 进行处理
         PayRefundChannelPostHandler payRefundChannelPostHandler = mapHandler.get(refundUnifiedRespDTO.getRespEnum());

         if(Objects.isNull(payRefundChannelPostHandler)){
             throw exception(PAY_REFUND_POST_HANDLER_NOT_FOUND);
         }

         PayRefundPostReqBO bo = PayRefundCoreConvert.INSTANCE.convert(refundUnifiedRespDTO);
         bo.setRefundAmount(reqBO.getAmount())
            .setRefundedAmount(order.getRefundAmount())
            .setRefundedTimes(order.getRefundTimes())
            .setRefundId(refundDO.getId())
            .setOrderId(order.getId())
            .setRefundTypeEnum(refundType);

         //调用退款的后置处理
         payRefundChannelPostHandler.handleRefundChannelResp(bo);

         return PayRefundRespBO.builder().refundId(refundDO.getId()).build();
    }


    /**
     * 校验是否进行退款
     * @param reqBO 退款申请信息
     * @param order 原始支付订单信息
     */
    private void validatePayRefund(PayRefundReqBO reqBO, PayOrderDO order) {

        // 校验状态，必须是支付状态
        if (!PayOrderStatusEnum.SUCCESS.getStatus().equals(order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_SUCCESS);
        }
        //是否已经全额退款
        if (PayRefundTypeEnum.ALL.getStatus().equals(order.getRefundStatus())) {
            throw exception(PAY_REFUND_ALL_REFUNDED);
        }
        // 校验金额 退款金额不能大于 原定的金额
        if(reqBO.getAmount() + order.getRefundAmount() > order.getAmount()){
            throw exception(PAY_REFUND_AMOUNT_EXCEED);
        }
        //校验渠道订单号
        if (StrUtil.isEmpty(order.getChannelOrderNo())) {
            throw exception(PAY_REFUND_CHN_ORDER_NO_IS_NULL);
        }
        //TODO  退款的期限  退款次数的控制


    }
}
