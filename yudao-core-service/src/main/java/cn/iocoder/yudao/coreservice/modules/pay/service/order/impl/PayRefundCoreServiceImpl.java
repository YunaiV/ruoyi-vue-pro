package cn.iocoder.yudao.coreservice.modules.pay.service.order.impl;

import cn.hutool.core.util.StrUtil;
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
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayRefundReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundNotifyDTO;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundTypeEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayAppCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayChannelCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRefundRespEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayNotifyRefundStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        PayOrderExtensionDO orderExtensionDO = payOrderExtensionCoreMapper.selectById(order.getSuccessExtensionId());
        PayRefundDO payRefundDO = payRefundCoreMapper.selectByTradeNoAndMerchantRefundNo(orderExtensionDO.getNo(), req.getMerchantRefundNo());
        //构造渠道的统一的退款请求参数
        PayRefundUnifiedReqDTO unifiedReqDTO = new PayRefundUnifiedReqDTO();
        if(Objects.nonNull(payRefundDO)){
            //退款订单已经提交过。
            //TODO 校验相同退款单的金额
            if(Objects.equals(PayRefundStatusEnum.SUCCESS.getStatus(), payRefundDO.getStatus())
                || Objects.equals(PayRefundStatusEnum.CLOSE.getStatus(), payRefundDO.getStatus())){
                //已成功退款
               throw exception(PAY_REFUND_SUCCEED);
            }else{
                //保证商户退款单不变，重复向渠道发起退款。渠道保持幂等
                unifiedReqDTO.setUserIp(req.getUserIp())
                             .setAmount(payRefundDO.getRefundAmount())
                             .setChannelOrderNo(payRefundDO.getChannelOrderNo())
                             .setPayTradeNo(payRefundDO.getTradeNo())
                             .setRefundReqNo(payRefundDO.getMerchantRefundNo())
                             .setReason(payRefundDO.getReason());
            }
        }else{
            //新生成退款单。 退款单入库 退款单状态：生成
            payRefundDO = PayRefundDO.builder().channelOrderNo(order.getChannelOrderNo())
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
                    .type(refundType.getStatus())
                    .build();
            payRefundCoreMapper.insert(payRefundDO);
            unifiedReqDTO.setUserIp(req.getUserIp())
                    .setAmount(payRefundDO.getRefundAmount())
                    .setChannelOrderNo(payRefundDO.getChannelOrderNo())
                    .setPayTradeNo(payRefundDO.getTradeNo())
                    .setRefundReqNo(payRefundDO.getMerchantRefundNo())
                    .setReason(req.getReason());
        }
        //向渠道发起退款申请
        PayRefundUnifiedRespDTO refundUnifiedRespDTO = client.unifiedRefund(unifiedReqDTO);
        //构造退款申请返回对象
        PayRefundRespDTO respDTO = new PayRefundRespDTO();
        if(refundUnifiedRespDTO.getChannelResp() == PayChannelRefundRespEnum.SUCCESS
            ||refundUnifiedRespDTO.getChannelResp() == PayChannelRefundRespEnum.PROCESSING ){
            //成功处理， 在退款通知中处理, 这里不处理
            respDTO.setChannelReturnResult(PayChannelRefundRespEnum.SUCCESS.getStatus());
            respDTO.setRefundId(payRefundDO.getId());
        }else {
            //失败返回错误给前端，可以重新发起退款，保证退款请求号（这里是商户退款单号)， 避免重复退款。
            respDTO.setChannelReturnResult(PayChannelRefundRespEnum.FAILURE.getStatus());
            //更新退款单状态
            PayRefundDO updatePayRefund = new PayRefundDO();
            updatePayRefund.setId(payRefundDO.getId())
                    .setChannelErrorMsg(refundUnifiedRespDTO.getChannelMsg())
                    .setChannelErrorCode(refundUnifiedRespDTO.getChannelCode())
                    .setStatus(PayRefundStatusEnum.FAILURE.getStatus());
            payRefundCoreMapper.updateById(updatePayRefund);
        }
        respDTO.setChannelReturnCode(refundUnifiedRespDTO.getChannelCode())
                .setChannelReturnMsg(refundUnifiedRespDTO.getChannelMsg());
        return respDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
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
        if (Objects.equals(PayNotifyRefundStatusEnum.SUCCESS,refundNotify.getStatus())){
            payRefundSuccess(refundNotify);
        } else {
            //TODO 支付异常， 支付宝似乎没有支付异常的通知。
        }
    }

    private void payRefundSuccess(PayRefundNotifyDTO refundNotify) {
        PayRefundDO refundDO = payRefundCoreMapper.selectByTradeNoAndMerchantRefundNo(refundNotify.getTradeNo(), refundNotify.getReqNo());
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
                .setRefundTimes(payOrderDO.getRefundTimes()+1)
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
