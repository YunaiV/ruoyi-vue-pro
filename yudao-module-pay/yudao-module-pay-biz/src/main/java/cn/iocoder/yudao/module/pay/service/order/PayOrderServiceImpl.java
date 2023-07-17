package cn.iocoder.yudao.module.pay.service.order;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.pay.config.PayProperties;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.convert.order.PayOrderConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderExtensionMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.notify.dto.PayNotifyTaskCreateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 支付订单 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
@Slf4j
public class PayOrderServiceImpl implements PayOrderService {

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayOrderMapper orderMapper;
    @Resource
    private PayOrderExtensionMapper orderExtensionMapper;

    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;

    @Override
    public PayOrderDO getOrder(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public PayOrderDO getOrder(Long appId, String merchantOrderId) {
        return orderMapper.selectByAppIdAndMerchantOrderId(appId, merchantOrderId);
    }

    @Override
    public Long getOrderCountByAppId(Long appId) {
        return orderMapper.selectCountByAppId(appId);
    }

    @Override
    public PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO) {
        return orderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO) {
        return orderMapper.selectList(exportReqVO);
    }

    // TODO @艿艿：需要优化。不确定这个方法的作用
    @Override
    public List<PayOrderDO> getOrderSubjectList(Collection<Long> idList) {
        return orderMapper.findByIdListQueryOrderSubject(idList);
    }

    @Override
    public Long createOrder(PayOrderCreateReqDTO reqDTO) {
        // 校验 App
        PayAppDO app = appService.validPayApp(reqDTO.getAppId());

        // 查询对应的支付交易单是否已经存在。如果是，则直接返回
        PayOrderDO order = orderMapper.selectByAppIdAndMerchantOrderId(
                reqDTO.getAppId(), reqDTO.getMerchantOrderId());
        if (order != null) {
            log.warn("[createOrder][appId({}) merchantOrderId({}) 已经存在对应的支付单({})]", order.getAppId(),
                    order.getMerchantOrderId(), toJsonString(order)); // 理论来说，不会出现这个情况
            return order.getId();
        }

        // 创建支付交易单
        order = PayOrderConvert.INSTANCE.convert(reqDTO).setAppId(app.getId())
                // 商户相关字段
                .setNotifyUrl(app.getOrderNotifyUrl()).setNotifyStatus(PayOrderNotifyStatusEnum.NO.getStatus())
                // 订单相关字段
                .setStatus(PayOrderStatusEnum.WAITING.getStatus())
                // 退款相关字段
                .setRefundStatus(PayOrderRefundStatusEnum.NO.getStatus()).setRefundTimes(0).setRefundPrice(0);
        orderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderSubmitRespVO submitOrder(PayOrderSubmitReqVO reqVO, String userIp) {
        // 1. 获得 PayOrderDO ，并校验其是否存在
        PayOrderDO order = validateOrderCanSubmit(reqVO.getId());
        // 1.2 校验支付渠道是否有效
        PayChannelDO channel = validatePayChannelCanSubmit(order.getAppId(), reqVO.getChannelCode());
        PayClient client = payClientFactory.getPayClient(channel.getId());

        // 2. 插入 PayOrderExtensionDO
        PayOrderExtensionDO orderExtension = PayOrderConvert.INSTANCE.convert(reqVO, userIp)
                .setOrderId(order.getId()).setNo(generateOrderExtensionNo())
                .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus());
        orderExtensionMapper.insert(orderExtension);

        // 3. 调用三方接口
        PayOrderUnifiedReqDTO unifiedOrderReqDTO = PayOrderConvert.INSTANCE.convert2(reqVO, userIp)
                // 商户相关的字段
                .setOutTradeNo(orderExtension.getNo()) // 注意，此处使用的是 PayOrderExtensionDO.no 属性！
                .setSubject(order.getSubject()).setBody(order.getBody())
                .setNotifyUrl(genChannelOrderNotifyUrl(channel))
                .setReturnUrl(reqVO.getReturnUrl())
                // 订单相关字段
                .setPrice(order.getPrice()).setExpireTime(order.getExpireTime());
        PayOrderUnifiedRespDTO unifiedOrderRespDTO = client.unifiedOrder(unifiedOrderReqDTO);

        // 4. 如果调用直接支付成功，则直接更新支付单状态为成功。例如说：付款码支付，免密支付时，就直接验证支付成功
        if (unifiedOrderRespDTO.getOrder() != null) {
            notifyPayOrder(channel, unifiedOrderRespDTO.getOrder());
            // 此处需要读取最新的状态
            order = orderMapper.selectById(order.getId());
        }

        // 返回成功
        return PayOrderConvert.INSTANCE.convert(order, unifiedOrderRespDTO);
    }

    private PayOrderDO validateOrderCanSubmit(Long id) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) { // 是否存在
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        if (LocalDateTimeUtils.beforeNow(order.getExpireTime())) { // 校验是否过期
            throw exception(PAY_ORDER_IS_EXPIRED);
        }
        return order;
    }

    private PayChannelDO validatePayChannelCanSubmit(Long appId, String channelCode) {
        // 校验 App
        appService.validPayApp(appId);
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(appId, channelCode);
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[validatePayChannelCanSubmit][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }
        return channel;
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelOrderNotifyUrl(PayChannelDO channel) {
        return payProperties.getOrderNotifyUrl() + "/" + channel.getId();
    }

    private String generateOrderExtensionNo() {
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
    @Transactional(rollbackFor = Exception.class)
    public void notifyOrder(Long channelId, PayOrderRespDTO notify) {
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 更新支付订单为已支付
        TenantUtils.execute(channel.getTenantId(), () -> notifyPayOrder(channel, notify));
    }

    @Override
    public void updateOrderRefundPrice(Long id, Integer incrRefundPrice) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.isSuccess(order.getStatus())) {
            throw exception(PAY_REFUND_PRICE_EXCEED);
        }
        if (order.getRefundPrice() + incrRefundPrice > order.getPrice()) {
            throw exception(PAY_REFUND_PRICE_EXCEED);
        }

        // 更新订单
        PayOrderDO updateObj = new PayOrderDO()
                .setRefundPrice(order.getRefundPrice() + incrRefundPrice)
                .setRefundTimes(order.getRefundTimes() + 1);
        if (Objects.equals(updateObj.getRefundPrice(), order.getPrice())) {
            updateObj.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                    .setRefundStatus(PayOrderRefundStatusEnum.ALL.getStatus());
        } else {
            updateObj.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                    .setRefundStatus(PayOrderRefundStatusEnum.PART.getStatus());
        }
        orderMapper.updateByIdAndStatus(id, PayOrderStatusEnum.SUCCESS.getStatus(), updateObj);
    }

    private void notifyPayOrder(PayChannelDO channel, PayOrderRespDTO notify) {
        // 情况一：支付成功的回调
        if (PayOrderStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyOrderSuccess(channel, notify);
            return;
        }
        // 情况二：非支付成功的回调，进行忽略
        log.info("[notifyPayOrder][非支付成功的回调({})，直接忽略]", toJsonString(notify));
    }

    private void notifyOrderSuccess(PayChannelDO channel, PayOrderRespDTO notify) {
        // 1. 更新 PayOrderExtensionDO 支付成功
        PayOrderExtensionDO orderExtension = updateOrderExtensionSuccess(notify);
        // 2. 更新 PayOrderDO 支付成功
        Pair<Boolean, PayOrderDO> order = updateOrderExtensionSuccess(channel, orderExtension, notify);
        if (order.getKey()) { // 如果之前已经成功回调，则直接返回，不用重复记录支付通知记录；例如说：支付平台重复回调
            return;
        }

        // 3. 插入支付通知记录
        notifyService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
                .type(PayNotifyTypeEnum.ORDER.getType()).dataId(order.getValue().getId()).build());
    }

    /**
     * 更新 PayOrderExtensionDO 支付成功
     *
     * @param notify 通知
     * @return PayOrderExtensionDO 对象
     */
    private PayOrderExtensionDO updateOrderExtensionSuccess(PayOrderRespDTO notify) {
        // 1. 查询 PayOrderExtensionDO
        PayOrderExtensionDO orderExtension = orderExtensionMapper.selectByNo(notify.getOutTradeNo());
        if (orderExtension == null) {
            throw exception(PAY_ORDER_EXTENSION_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[updateOrderExtensionSuccess][支付拓展单({}) 已经是已支付，无需更新为已支付]", orderExtension.getId());
            return orderExtension;
        }
        if (ObjectUtil.notEqual(orderExtension.getStatus(), PayOrderStatusEnum.WAITING.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新 PayOrderExtensionDO
        int updateCounts = orderExtensionMapper.updateByIdAndStatus(orderExtension.getId(), orderExtension.getStatus(),
                PayOrderExtensionDO.builder().status(PayOrderStatusEnum.SUCCESS.getStatus()).channelNotifyData(toJsonString(notify)).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderExtensionSuccess][支付拓展单({}) 更新为已支付]", orderExtension.getId());
        return orderExtension;
    }

    /**
     * 更新 PayOrderDO 支付成功
     *
     * @param channel 支付渠道
     * @param orderExtension 支付拓展单
     * @param notify 通知回调
     * @return key：是否之前已经成功回调
     *         value：PayOrderDO 对象
     */
    private Pair<Boolean, PayOrderDO> updateOrderExtensionSuccess(PayChannelDO channel, PayOrderExtensionDO orderExtension,
                                                            PayOrderRespDTO notify) {
        // 1. 判断 PayOrderDO 是否处于待支付
        PayOrderDO order = orderMapper.selectById(orderExtension.getOrderId());
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus()) // 如果已经是成功，直接返回，不用重复更新
                && Objects.equals(order.getSuccessExtensionId(), orderExtension.getId())) {
            log.info("[updateOrderExtensionSuccess][支付订单({}) 已经是已支付，无需更新为已支付]", order.getId());
            return Pair.of(true, order);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新 PayOrderDO
        int updateCounts = orderMapper.updateByIdAndStatus(order.getId(), PayOrderStatusEnum.WAITING.getStatus(),
                PayOrderDO.builder().status(PayOrderStatusEnum.SUCCESS.getStatus())
                        .channelId(channel.getId()).channelCode(channel.getCode())
                        .successTime(notify.getSuccessTime()).successExtensionId(orderExtension.getId())
                        .channelOrderNo(notify.getChannelOrderNo()).channelUserId(notify.getChannelUserId())
                        .notifyTime(LocalDateTime.now()).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderExtensionSuccess][支付订单({}) 更新为已支付]", order.getId());
        return Pair.of(false, order);
    }

}
