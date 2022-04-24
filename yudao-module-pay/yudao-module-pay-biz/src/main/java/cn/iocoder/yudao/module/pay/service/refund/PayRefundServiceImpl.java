package cn.iocoder.yudao.module.pay.service.refund;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundNotifyDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayNotifyRefundStatusEnum;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundTypeEnum;
import cn.iocoder.yudao.module.pay.service.merchant.PayAppService;
import cn.iocoder.yudao.module.pay.service.merchant.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.notify.dto.PayNotifyTaskCreateReqDTO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderExtensionService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.order.dto.PayRefundReqDTO;
import cn.iocoder.yudao.module.pay.service.order.dto.PayRefundRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 退款订单 Service 实现类
 *
 * @author aquan
 */
@Service
@Slf4j
@Validated
public class PayRefundServiceImpl implements PayRefundService {

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayRefundMapper refundMapper;
    @Resource
    private PayOrderMapper orderMapper; // TODO @jason：需要改成不直接操作 db；

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayOrderExtensionService orderExtensionService;
    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;

    @Override
    public PayRefundDO getRefund(Long id) {
        return refundMapper.selectById(id);
    }

    @Override
    public PageResult<PayRefundDO> getRefundPage(PayRefundPageReqVO pageReqVO) {
        return refundMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayRefundDO> getRefundList(PayRefundExportReqVO exportReqVO) {
        return refundMapper.selectList(exportReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayRefundRespDTO submitRefundOrder(PayRefundReqDTO req) {
        // 获得 PayOrderDO
        PayOrderDO order = orderService.getOrder(req.getPayOrderId());
        // 校验订单是否存在
        if (Objects.isNull(order) ) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_NOT_FOUND);
        }
        // 校验 App
        PayAppDO app = appService.validPayApp(order.getAppId());
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(order.getChannelId());
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[refund][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_CHANNEL_CLIENT_NOT_FOUND);
        }

        // 校验退款的条件
        validatePayRefund(req, order);
        // 退款类型
        PayRefundTypeEnum refundType = PayRefundTypeEnum.SOME;
        if (Objects.equals(req.getAmount(), order.getAmount())) {
            refundType = PayRefundTypeEnum.ALL;
        }
        PayOrderExtensionDO orderExtensionDO = orderExtensionService.getOrderExtension(order.getSuccessExtensionId());
        PayRefundDO payRefundDO = refundMapper.selectByTradeNoAndMerchantRefundNo(orderExtensionDO.getNo(), req.getMerchantRefundId());
        if(Objects.nonNull(payRefundDO)){
            // 退款订单已经提交过。
            //TODO 校验相同退款单的金额
            // TODO @jason：咱要不封装一个 ObjectUtils.equalsAny
            if (Objects.equals(PayRefundStatusEnum.SUCCESS.getStatus(), payRefundDO.getStatus())
                    || Objects.equals(PayRefundStatusEnum.CLOSE.getStatus(), payRefundDO.getStatus())) {
                //已成功退款
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_REFUND_SUCCEED);
            }
            //可以重复提交，保证 退款请求号 一致，由渠道保证幂等
        } else {
            // 成功，插入退款单 状态为生成.没有和渠道交互
            // TODO @jason：搞到 convert 里。一些额外的自动，手动 set 下；
            payRefundDO = PayRefundDO.builder().channelOrderNo(order.getChannelOrderNo())
                    .appId(order.getAppId())
                    .channelOrderNo(order.getChannelOrderNo())
                    .channelCode(order.getChannelCode())
                    .channelId(order.getChannelId())
                    .merchantId(order.getMerchantId())
                    .orderId(order.getId())
                    .merchantRefundNo(req.getMerchantRefundId())
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
            refundMapper.insert(payRefundDO);
        }
        // TODO @jason：搞到 convert 里。一些额外的自动，手动 set 下；
        PayRefundUnifiedReqDTO unifiedReqDTO = new PayRefundUnifiedReqDTO();
        unifiedReqDTO.setUserIp(req.getUserIp())
                .setAmount(req.getAmount())
                .setChannelOrderNo(order.getChannelOrderNo())
                .setPayTradeNo(orderExtensionDO.getNo())
                .setMerchantRefundId(req.getMerchantRefundId())
                .setReason(req.getReason());
        // 向渠道发起退款申请
        PayCommonResult<PayRefundUnifiedRespDTO> refundUnifiedResult = client.unifiedRefund(unifiedReqDTO);
        // 检查是否失败，失败抛出业务异常。
        // TODO 渠道的异常记录。
        // TODO @jason：可以先打个 warn log 哈；
        refundUnifiedResult.checkError();
        // 成功在 退款回调中处理
        return PayRefundRespDTO.builder().refundId(payRefundDO.getId()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyPayRefund(Long channelId, PayNotifyDataDTO notifyData) {
        log.info("[notifyPayRefund][channelId({}) 回调数据({})]", channelId, notifyData.getBody());
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[notifyPayOrder][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_CHANNEL_CLIENT_NOT_FOUND);
        }
        // 解析渠道退款通知数据， 统一处理
        PayRefundNotifyDTO refundNotify = client.parseRefundNotify(notifyData);
        if (Objects.equals(PayNotifyRefundStatusEnum.SUCCESS,refundNotify.getStatus())){
            payRefundSuccess(refundNotify);
        } else {
            //TODO 支付异常， 支付宝似乎没有支付异常的通知。
            // TODO @jason：那这里可以考虑打个 error logger @芋艿 微信是否存在支付异常通知
        }
    }

    private void payRefundSuccess(PayRefundNotifyDTO refundNotify) {
        // 校验退款单存在
        PayRefundDO refundDO = refundMapper.selectByTradeNoAndMerchantRefundNo(refundNotify.getTradeNo(), refundNotify.getReqNo());
        if (refundDO == null) {
            log.error("[payRefundSuccess][不存在 seqNo 为{} 的支付退款单]", refundNotify.getReqNo());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_REFUND_NOT_FOUND);
        }

        // 得到已退金额
        PayOrderDO payOrderDO = orderService.getOrder(refundDO.getOrderId());
        Long refundedAmount = payOrderDO.getRefundAmount();

        PayOrderStatusEnum orderStatus = PayOrderStatusEnum.SUCCESS;
        if(Objects.equals(payOrderDO.getAmount(), refundedAmount+ refundDO.getRefundAmount())){
            //支付金额  = 已退金额 + 本次退款金额。
            orderStatus = PayOrderStatusEnum.CLOSED;
        }
        // 更新支付订单
        PayOrderDO updateOrderDO = new PayOrderDO();
        updateOrderDO.setId(refundDO.getOrderId())
                .setRefundAmount(refundedAmount + refundDO.getRefundAmount())
                .setStatus(orderStatus.getStatus())
                .setRefundTimes(payOrderDO.getRefundTimes() + 1)
                .setRefundStatus(refundDO.getType());
        orderMapper.updateById(updateOrderDO);

        // 更新退款订单
        PayRefundDO updateRefundDO = new PayRefundDO();
        updateRefundDO.setId(refundDO.getId())
                .setSuccessTime(refundNotify.getRefundSuccessTime())
                .setChannelRefundNo(refundNotify.getChannelOrderNo())
                .setTradeNo(refundNotify.getTradeNo())
                .setNotifyTime(new Date())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
        refundMapper.updateById(updateRefundDO);

        // 插入退款通知记录
        // TODO 通知商户成功或者失败. 现在通知似乎没有实现， 只是回调
        notifyService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
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
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_STATUS_IS_NOT_SUCCESS);
        }
        // 是否已经全额退款
        if (PayRefundTypeEnum.ALL.getStatus().equals(order.getRefundStatus())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_REFUND_ALL_REFUNDED);
        }
        // 校验金额 退款金额不能大于 原定的金额
        if (req.getAmount() + order.getRefundAmount() > order.getAmount()){
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_REFUND_AMOUNT_EXCEED);
        }
        // 校验渠道订单号
        if (StrUtil.isEmpty(order.getChannelOrderNo())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_REFUND_CHN_ORDER_NO_IS_NULL);
        }
        //TODO  退款的期限  退款次数的控制
    }

}
