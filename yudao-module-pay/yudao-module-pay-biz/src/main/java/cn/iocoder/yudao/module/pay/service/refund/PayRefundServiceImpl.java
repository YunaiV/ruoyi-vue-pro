package cn.iocoder.yudao.module.pay.service.refund;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.config.PayProperties;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.convert.refund.PayRefundConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.notify.dto.PayNotifyTaskCreateReqDTO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.CHANNEL_NOT_FOUND;

/**
 * 退款订单 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
@Validated
public class PayRefundServiceImpl implements PayRefundService {

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayRefundMapper refundMapper;

    @Resource
    private PayOrderService orderService;
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
    public Long getRefundCountByAppId(Long appId) {
        return refundMapper.selectCountByAppId(appId);
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
    public Long createPayRefund(PayRefundCreateReqDTO reqDTO) {
        // 1.1 校验 App
        PayAppDO app = appService.validPayApp(reqDTO.getAppId());
        // 1.2 校验支付订单
        PayOrderDO order = validatePayOrderCanRefund(reqDTO);
        // 1.3 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(order.getChannelId());
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[refund][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 1.4 校验退款订单是否已经存在
        PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(
                app.getId(), reqDTO.getMerchantRefundId());
        if (refund != null) {
            throw exception(ErrorCodeConstants.REFUND_EXISTS);
        }

        // 2.1 插入退款单
        refund = PayRefundConvert.INSTANCE.convert(reqDTO)
                .setNo(generateRefundNo()).setOrderId(order.getId())
                .setChannelId(order.getChannelId()).setChannelCode(order.getChannelCode())
                // 商户相关的字段
                .setNotifyUrl(app.getRefundNotifyUrl())
                // 渠道相关字段
                .setChannelOrderNo(order.getChannelOrderNo())
                // 退款相关字段
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setPayPrice(order.getPrice()).setRefundPrice(reqDTO.getPrice());
        refundMapper.insert(refund);
        // 2.2 向渠道发起退款申请
        PayOrderExtensionDO orderExtension = orderService.getOrderExtension(order.getExtensionId());
        PayRefundUnifiedReqDTO unifiedReqDTO = new PayRefundUnifiedReqDTO()
                .setPayPrice(order.getPrice())
                .setRefundPrice(reqDTO.getPrice())
                .setOutTradeNo(orderExtension.getNo())
                .setOutRefundNo(refund.getNo())
                .setNotifyUrl(genChannelRefundNotifyUrl(channel))
                .setReason(reqDTO.getReason());
        PayRefundRespDTO refundRespDTO = client.unifiedRefund(unifiedReqDTO);
        // 2.3 处理退款返回
        notifyRefund(channel, refundRespDTO);

        // 成功在 退款回调中处理
        return refund.getId();
    }

    /**
     * 校验支付订单是否可以退款
     *
     * @param reqDTO 退款申请信息
     * @return 支付订单
     */
    private PayOrderDO validatePayOrderCanRefund(PayRefundCreateReqDTO reqDTO) {
        PayOrderDO order = orderService.getOrder(reqDTO.getAppId(), reqDTO.getMerchantOrderId());
        if (order == null) {
            throw exception(ErrorCodeConstants.ORDER_NOT_FOUND);
        }
        // 校验状态，必须是支付状态
        if (!PayOrderStatusEnum.SUCCESS.getStatus().equals(order.getStatus())) {
            throw exception(ErrorCodeConstants.ORDER_STATUS_IS_NOT_SUCCESS);
        }

        // 校验金额 退款金额不能大于原定的金额
        if (reqDTO.getPrice() + order.getRefundPrice() > order.getPrice()){
            throw exception(ErrorCodeConstants.REFUND_PRICE_EXCEED);
        }
        // 是否有退款中的订单
        if (refundMapper.selectCountByAppIdAndOrderId(reqDTO.getAppId(), order.getId(),
                PayRefundStatusEnum.WAITING.getStatus()) > 0) {
            throw exception(ErrorCodeConstants.REFUND_HAS_REFUNDING);
        }
        return order;
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelRefundNotifyUrl(PayChannelDO channel) {
        return payProperties.getRefundNotifyUrl() + "/" + channel.getId();
    }

    private String generateRefundNo() {
//    wx
//    2014
//    10
//    27
//    20
//    09
//    39
//    5522657
//    a690389285100
        // 目前的算法
        // 时间序列，年月日时分秒 14 位
        // 纯随机，6 位 TODO 芋艿：此处估计是会有问题的，后续在调整
        return DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + // 时间序列
                RandomUtil.randomInt(100000, 999999) // 随机。为什么是这个范围，因为偷懒
                ;
    }

    @Override
    public void notifyRefund(Long channelId, PayRefundRespDTO notify) {
        // 校验支付渠道是否有效
        channelService.validPayChannel(channelId);
        // 通知结果

        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 更新退款订单
        TenantUtils.execute(channel.getTenantId(), () -> notifyRefund(channel, notify));
    }

    // TODO 芋艿：事务问题
    private void notifyRefund(PayChannelDO channel, PayRefundRespDTO notify) {
        // 情况一：退款成功
        if (PayRefundStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyRefundSuccess(channel, notify);
            return;
        }
        // 情况二：退款失败
        if (PayRefundStatusRespEnum.isFailure(notify.getStatus())) {
            notifyRefundFailure(channel, notify);
        }
    }

    public void notifyRefundSuccess(PayChannelDO channel, PayRefundRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(ErrorCodeConstants.REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isSuccess(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款成功，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(ErrorCodeConstants.REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setSuccessTime(notify.getSuccessTime())
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setChannelNotifyData(toJsonString(notify));
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(ErrorCodeConstants.REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundSuccess][退款订单({}) 更新为退款成功]", refund.getId());

        // 2. 更新订单
        orderService.updateOrderRefundPrice(refund.getOrderId(), refund.getRefundPrice());

        // 3. 插入退款通知记录 TODO 芋艿：退款成功
        notifyService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
                .type(PayNotifyTypeEnum.REFUND.getType()).dataId(refund.getId()).build());
    }

    @Transactional(rollbackFor = Exception.class)
    public void notifyRefundFailure(PayChannelDO channel, PayRefundRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(ErrorCodeConstants.REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isFailure(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款关闭，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(ErrorCodeConstants.REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelNotifyData(toJsonString(notify))
                .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg());
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(ErrorCodeConstants.REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundFailure][退款订单({}) 更新为退款失败]", refund.getId());

        // 2. 插入退款通知记录 TODO 芋艿：退款失败
        notifyService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
                .type(PayNotifyTypeEnum.REFUND.getType()).dataId(refund.getId()).build());
    }

}
