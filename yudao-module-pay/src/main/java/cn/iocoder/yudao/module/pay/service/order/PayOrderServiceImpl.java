package cn.iocoder.yudao.module.pay.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
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
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.dal.redis.order.PayOrderLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
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

    /**
     * 提交支付订单的锁超时时间，单位：毫秒
     */
    private static final long SUBMIT_LOCK_TIMEOUT_MILLIS = 120 * DateUtils.SECOND_MILLIS;

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayOrderMapper orderMapper;
    @Resource
    private PayOrderExtensionMapper orderExtensionMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;
    @Resource
    private PayOrderLockRedisDAO orderLockRedisDAO;

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
    public PayOrderDO getOrder(String no) {
        return orderMapper.selectByNo(no);
    }

    @Override
    public PayOrderDO getOrder(Long appId, String merchantOrderId) {
        return orderMapper.selectByAppIdAndMerchantOrderId(appId, merchantOrderId);
    }

    @Override
    public List<PayOrderDO> getOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return orderMapper.selectByIds(ids);
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

    @Override
    public Long createOrder(PayOrderCreateReqDTO reqDTO) {
        // 校验 App
        PayAppDO app = appService.validPayApp(reqDTO.getAppKey());

        // 查询对应的支付交易单是否已经存在。如果是，则直接返回
        PayOrderDO order = orderMapper.selectByAppIdAndMerchantOrderId(
                app.getId(), reqDTO.getMerchantOrderId());
        if (order != null) {
            log.warn("[createOrder][appId({}) merchantOrderId({}) 已经存在对应的支付单({})]", order.getAppId(),
                    order.getMerchantOrderId(), toJsonString(order)); // 理论来说，不会出现这个情况
            return order.getId();
        }

        // 创建支付交易单
        order = PayOrderConvert.INSTANCE.convert(reqDTO).setAppId(app.getId())
                // 商户相关字段
                .setNotifyUrl(app.getOrderNotifyUrl())
                // 订单相关字段
                .setStatus(PayOrderStatusEnum.WAITING.getStatus())
                // 退款相关字段
                .setRefundPrice(0);
        orderMapper.insert(order);
        return order.getId();
    }

    @Override // 注意，这里不能添加事务注解，避免调用支付渠道失败时，将 PayOrderExtensionDO 回滚了
    public PayOrderSubmitRespVO submitOrder(PayOrderSubmitReqVO reqVO, String userIp) {
        // 微信渠道会复用待支付的支付订单拓展，需加锁避免并发提交时重复创建
        if (isOrderExtensionReusable(reqVO.getChannelCode())) {
            return orderLockRedisDAO.lock(reqVO.getId(), SUBMIT_LOCK_TIMEOUT_MILLIS,
                    () -> submitOrder0(reqVO, userIp));
        }
        // 其它渠道每次提交都新建支付订单拓展，保持原有逻辑
        return submitOrder0(reqVO, userIp);
    }

    private PayOrderSubmitRespVO submitOrder0(PayOrderSubmitReqVO reqVO, String userIp) {
        // 1.1 获得 PayOrderDO ，并校验其是否存在
        PayOrderDO order = validateOrderCanSubmit(reqVO.getId());
        // 1.32 校验支付渠道是否有效
        PayChannelDO channel = validateChannelCanSubmit(order.getAppId(), reqVO.getChannelCode());
        PayClient<?> client = channelService.getPayClient(channel.getId());

        // 2. 获得 PayOrderExtensionDO：优先复用待支付的拓展单，避免重复创建渠道订单
        PayOrderExtensionDO orderExtension = getReusableOrderExtension(order, channel, reqVO, userIp);
        boolean reused = orderExtension != null;
        if (!reused) {
            orderExtension = createOrderExtension(order, channel, reqVO, userIp);
        }

        // 3.1 调用三方接口
        PayOrderUnifiedReqDTO unifiedOrderReqDTO = PayOrderConvert.INSTANCE.convert2(reqVO, userIp)
                // 商户相关的字段
                .setOutTradeNo(orderExtension.getNo()) // 注意，此处使用的是 PayOrderExtensionDO.no 属性！
                .setUserIp(orderExtension.getUserIp()) // 复用时，需保持与原渠道订单的参数一致
                .setSubject(order.getSubject()).setBody(order.getBody())
                .setNotifyUrl(genChannelOrderNotifyUrl(channel))
                .setReturnUrl(reqVO.getReturnUrl())
                // 订单相关字段
                .setPrice(order.getPrice()).setExpireTime(order.getExpireTime());
        PayOrderRespDTO unifiedOrderResp = client.unifiedOrder(unifiedOrderReqDTO);
        // 3.2 复用的拓展单下单失败时，以渠道订单的真实状态为准，决定是否新建拓展单重试
        if (reused && unifiedOrderResp != null && PayOrderStatusEnum.isClosed(unifiedOrderResp.getStatus())) {
            unifiedOrderResp = retryUnifiedOrder(order, channel, client, reqVO, userIp,
                    orderExtension, unifiedOrderReqDTO, unifiedOrderResp);
        }

        // 4. 如果调用直接支付成功，则直接更新支付单状态为成功。例如说：付款码支付，免密支付时，就直接验证支付成功
        if (unifiedOrderResp != null) {
            try {
                getSelf().notifyOrder(channel, unifiedOrderResp);
            } catch (Exception e) {
                // 兼容 https://gitee.com/zhijiantianya/yudao-cloud/issues/I8SM9H 场景
                // 支付宝或微信扫码之后时，由于 PayClient 是直接返回支付成功，而支付也会有回调，导致存在并发更新问题，此时一般是可以 try catch 直接忽略
                log.warn("[submitOrder][order({}) channel({}) 支付结果({}) 通知时发生异常，可能是并发问题]",
                        order, channel, unifiedOrderResp, e);
            }
            // 如有渠道错误码，则抛出业务异常，提示用户
            if (StrUtil.isNotEmpty(unifiedOrderResp.getChannelErrorCode())) {
                throw exception(PAY_ORDER_SUBMIT_CHANNEL_ERROR, unifiedOrderResp.getChannelErrorCode(),
                        unifiedOrderResp.getChannelErrorMsg());
            }
            // 此处需要读取最新的状态
            order = orderMapper.selectById(order.getId());
        }
        return PayOrderConvert.INSTANCE.convert(order, unifiedOrderResp);
    }

    /**
     * 创建支付订单拓展
     *
     * @param order   支付订单
     * @param channel 支付渠道
     * @param reqVO   支付订单提交参数
     * @param userIp  用户 IP
     * @return 支付订单拓展
     */
    private PayOrderExtensionDO createOrderExtension(PayOrderDO order, PayChannelDO channel,
                                                     PayOrderSubmitReqVO reqVO, String userIp) {
        String no = noRedisDAO.generate(payProperties.getOrderNoPrefix());
        PayOrderExtensionDO orderExtension = PayOrderConvert.INSTANCE.convert(reqVO, userIp)
                .setOrderId(order.getId()).setNo(no)
                .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus());
        orderExtensionMapper.insert(orderExtension);
        return orderExtension;
    }

    /**
     * 获得可复用的支付订单拓展
     *
     * 需满足：同渠道、同 channelExtras、未下单失败过的待支付拓展单，且创建后支付订单未被修改（例如说：改价）
     *
     * @param order   支付订单
     * @param channel 支付渠道
     * @param reqVO   提交请求
     * @param userIp  用户 IP
     * @return 支付订单拓展；不可复用时，返回 null
     */
    private PayOrderExtensionDO getReusableOrderExtension(PayOrderDO order, PayChannelDO channel,
                                                          PayOrderSubmitReqVO reqVO, String userIp) {
        if (!isOrderExtensionReusable(channel.getCode())) {
            return null;
        }
        PayOrderExtensionDO orderExtension = orderExtensionMapper.selectLastByOrderIdAndChannelIdAndStatus(
                order.getId(), channel.getId(), PayOrderStatusEnum.WAITING.getStatus());
        if (orderExtension == null
                || ObjectUtil.notEqual(orderExtension.getChannelExtras(), reqVO.getChannelExtras())) {
            return null;
        }
        // 已记录渠道结果（复用下单失败过）的拓展单，不再复用，避免每次提交都重复失败
        if (StrUtil.isNotEmpty(orderExtension.getChannelNotifyData())) {
            return null;
        }
        // 微信 H5 支付会校验拉起支付的用户 IP 与下单 IP 一致，IP 变化（例如说：切换网络）时不能复用
        if (PayChannelEnum.WX_WAP.getCode().equals(channel.getCode())
                && ObjectUtil.notEqual(orderExtension.getUserIp(), userIp)) {
            return null;
        }
        // 拓展单创建后，支付订单被修改过（例如说：改价），渠道订单参数可能不一致，不能复用
        if (isOrderUpdatedAfter(order, orderExtension.getCreateTime())) {
            return null;
        }
        return orderExtension;
    }

    /**
     * 判断支付订单在指定时间（含）之后，是否可能被修改过
     *
     * 注意：数据库时间精度为秒，同一秒内无法区分修改与拓展单创建的先后（例如说：创建订单、创建拓展单、改价都在同一秒），
     * 所以修改时间需严格早于拓展单的创建时间，同一秒内统一按已修改处理，避免改价后误复用。
     * 代价是：支付订单与拓展单在同一秒内创建时，再次提交会新建一次拓展单
     *
     * @param order 支付订单
     * @param time  拓展单的创建时间
     * @return 是否可能被修改过
     */
    private static boolean isOrderUpdatedAfter(PayOrderDO order, LocalDateTime time) {
        if (order.getUpdateTime() == null || time == null) {
            return true;
        }
        return !order.getUpdateTime().isBefore(time);
    }

    /**
     * 复用的支付订单拓展下单失败时，查询渠道订单的真实状态后，再决定是否重试
     *
     * 原因是：微信下单失败（已支付、系统异常、参数不一致等），都会被转换成 CLOSED 状态，不能直接认为渠道订单已关闭。
     * 只有渠道订单确认关闭后，才新建拓展单重试，保证同一时刻最多只有一个可支付的复用渠道订单
     *
     * @return 下单结果
     */
    private PayOrderRespDTO retryUnifiedOrder(PayOrderDO order, PayChannelDO channel, PayClient<?> client,
                                              PayOrderSubmitReqVO reqVO, String userIp,
                                              PayOrderExtensionDO orderExtension,
                                              PayOrderUnifiedReqDTO unifiedOrderReqDTO,
                                              PayOrderRespDTO unifiedOrderResp) {
        // 1. 查询渠道订单的真实状态
        PayOrderRespDTO channelOrderResp = null;
        try {
            channelOrderResp = client.getOrder(orderExtension.getNo());
        } catch (Exception ex) {
            log.error("[retryUnifiedOrder][orderExtension({}) 查询渠道订单异常，下单失败({})]",
                    orderExtension.getId(), toJsonString(unifiedOrderResp), ex);
        }
        Integer channelStatus = channelOrderResp != null ? channelOrderResp.getStatus() : null;
        // 1.1 渠道订单已支付（例如说：回调延迟），按支付成功处理
        if (PayOrderStatusEnum.isSuccess(channelStatus)) {
            log.warn("[retryUnifiedOrder][orderExtension({}) 渠道订单已支付，可能是回调延迟]", orderExtension.getId());
            return channelOrderResp;
        }
        // 2. 渠道订单未确认关闭（待支付、已退款、状态未知或查询异常），先主动关闭，避免新旧渠道订单都可支付，导致重复支付
        if (!PayOrderStatusEnum.isClosed(channelStatus)) {
            PayOrderRespDTO closeResp = null;
            try {
                closeResp = client.closeOrder(orderExtension.getNo());
            } catch (Exception ex) {
                log.error("[retryUnifiedOrder][orderExtension({}) 关闭渠道订单异常]", orderExtension.getId(), ex);
            }
            Integer closeStatus = closeResp != null ? closeResp.getStatus() : null;
            // 2.1 关闭时发现已支付，按支付成功处理
            if (PayOrderStatusEnum.isSuccess(closeStatus)) {
                log.warn("[retryUnifiedOrder][orderExtension({}) 关闭时发现渠道订单已支付]", orderExtension.getId());
                return closeResp;
            }
            // 2.2 无法确认已关闭，不新建拓展单，提示渠道错误；原拓展单保持可复用，用户再次提交时重新确认
            if (!PayOrderStatusEnum.isClosed(closeStatus)) {
                log.warn("[retryUnifiedOrder][orderExtension({}) 渠道状态({}) 关闭结果({})，下单失败({})]",
                        orderExtension.getId(), channelStatus, toJsonString(closeResp), toJsonString(unifiedOrderResp));
                throw exception(PAY_ORDER_SUBMIT_CHANNEL_ERROR, unifiedOrderResp.getChannelErrorCode(),
                        unifiedOrderResp.getChannelErrorMsg());
            }
        }

        // 3. 记录原拓展单的下单失败结果，后续提交不再复用它
        // 注意：本地拓展单保持待支付状态，最终状态交由回调、同步、过期任务处理，确保渠道订单实际支付成功时仍可正常处理
        int updateCounts = orderExtensionMapper.updateByIdAndStatus(orderExtension.getId(),
                PayOrderStatusEnum.WAITING.getStatus(), PayOrderExtensionDO.builder()
                        .channelErrorCode(unifiedOrderResp.getChannelErrorCode())
                        .channelErrorMsg(unifiedOrderResp.getChannelErrorMsg())
                        .channelNotifyData(toJsonString(unifiedOrderResp)).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付；例如说：并发回调已支付
            throw exception(PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        // 4. 渠道订单已关闭，新建拓展单重试
        log.info("[retryUnifiedOrder][orderExtension({}) 渠道订单已关闭，新建拓展单重试]", orderExtension.getId());
        PayOrderExtensionDO newOrderExtension = createOrderExtension(order, channel, reqVO, userIp);
        unifiedOrderReqDTO.setOutTradeNo(newOrderExtension.getNo()).setUserIp(newOrderExtension.getUserIp());
        return client.unifiedOrder(unifiedOrderReqDTO);
    }

    /**
     * 判断支付渠道是否可复用待支付的支付订单拓展
     *
     * 1. 微信付款码：authCode 只能使用一次，不可复用
     * 2. 支付宝、钱包等其它渠道：保持每次提交新建
     *
     * @param channelCode 渠道编码
     * @return 是否可复用
     */
    private static boolean isOrderExtensionReusable(String channelCode) {
        return PayChannelEnum.isWeixin(channelCode)
                && ObjectUtil.notEqual(PayChannelEnum.WX_BAR.getCode(), channelCode);
    }

    private PayOrderDO validateOrderCanSubmit(Long id) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) { // 是否存在
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus())) { // 校验状态，发现已支付
            throw exception(PAY_ORDER_STATUS_IS_SUCCESS);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        if (LocalDateTimeUtils.beforeNow(order.getExpireTime())) { // 校验是否过期
            throw exception(PAY_ORDER_IS_EXPIRED);
        }

        // 【重要】校验是否支付拓展单已支付，只是没有回调、或者数据不正常
        validateOrderActuallyPaid(id);
        return order;
    }

    /**
     * 校验支付订单实际已支付
     *
     * @param id 支付编号
     */
    @VisibleForTesting
    void validateOrderActuallyPaid(Long id) {
        List<PayOrderExtensionDO> orderExtensions = orderExtensionMapper.selectListByOrderId(id);
        orderExtensions.forEach(orderExtension -> {
            // 情况一：校验数据库中的 orderExtension 是不是已支付
            if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
                log.warn("[validateOrderCanSubmit][order({}) 的 extension({}) 已支付，可能是数据不一致]",
                        id, orderExtension.getId());
                throw exception(PAY_ORDER_EXTENSION_IS_PAID);
            }
            // 情况二：调用三方接口，查询支付单状态，是不是已支付
            PayClient<?> payClient = channelService.getPayClient(orderExtension.getChannelId());
            if (payClient == null) {
                log.error("[validateOrderCanSubmit][渠道编号({}) 找不到对应的支付客户端]", orderExtension.getChannelId());
                return;
            }
            PayOrderRespDTO respDTO = payClient.getOrder(orderExtension.getNo());
            if (respDTO != null && PayOrderStatusEnum.isSuccess(respDTO.getStatus())) {
                log.warn("[validateOrderCanSubmit][order({}) 的 PayOrderRespDTO({}) 已支付，可能是回调延迟]",
                        id, toJsonString(respDTO));
                throw exception(PAY_ORDER_EXTENSION_IS_PAID);
            }
        });
    }

    private PayChannelDO validateChannelCanSubmit(Long appId, String channelCode) {
        // 校验 App
        appService.validPayApp(appId);
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(appId, channelCode);
        PayClient<?> client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[validatePayChannelCanSubmit][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
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

    @Override
    public void notifyOrder(Long channelId, PayOrderRespDTO notify) {
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 更新支付订单为已支付
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyOrder(channel, notify));
    }

    /**
     * 通知并更新订单的支付结果
     *
     * @param channel 支付渠道
     * @param notify  通知
     */
    @Transactional(rollbackFor = Exception.class)
    // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyPayOrder(channel, notify) 调用，否则事务不生效
    public void notifyOrder(PayChannelDO channel, PayOrderRespDTO notify) {
        // 情况一：支付成功的回调
        if (PayOrderStatusEnum.isSuccess(notify.getStatus())) {
            notifyOrderSuccess(channel, notify);
            return;
        }
        // 情况二：支付失败的回调
        if (PayOrderStatusEnum.isClosed(notify.getStatus())) {
            notifyOrderClosed(channel, notify);
        }
        // 情况三：WAITING：无需处理
        // 情况四：REFUND：通过退款回调处理
    }

    private void notifyOrderSuccess(PayChannelDO channel, PayOrderRespDTO notify) {
        // 1. 更新 PayOrderExtensionDO 支付成功
        PayOrderExtensionDO orderExtension = updateOrderSuccess(notify);
        // 2. 更新 PayOrderDO 支付成功
        Boolean paid = updateOrderSuccess(channel, orderExtension, notify);
        if (paid) { // 如果之前已经成功回调，则直接返回，不用重复记录支付通知记录；例如说：支付平台重复回调
            return;
        }

        // 3. 插入支付通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.ORDER.getType(),
                orderExtension.getOrderId());
    }

    /**
     * 更新 PayOrderExtensionDO 支付成功
     *
     * @param notify 通知
     * @return PayOrderExtensionDO 对象
     */
    private PayOrderExtensionDO updateOrderSuccess(PayOrderRespDTO notify) {
        // 1. 查询 PayOrderExtensionDO
        PayOrderExtensionDO orderExtension = orderExtensionMapper.selectByNo(notify.getOutTradeNo());
        if (orderExtension == null) {
            throw exception(PAY_ORDER_EXTENSION_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[updateOrderExtensionSuccess][orderExtension({}) 已经是已支付，无需更新]", orderExtension.getId());
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
        log.info("[updateOrderExtensionSuccess][orderExtension({}) 更新为已支付]", orderExtension.getId());
        return orderExtension;
    }

    /**
     * 更新 PayOrderDO 支付成功
     *
     * @param channel        支付渠道
     * @param orderExtension 支付拓展单
     * @param notify         通知回调
     * @return 是否之前已经成功回调
     */
    private Boolean updateOrderSuccess(PayChannelDO channel, PayOrderExtensionDO orderExtension,
                                       PayOrderRespDTO notify) {
        // 1. 判断 PayOrderDO 是否处于待支付
        PayOrderDO order = orderMapper.selectById(orderExtension.getOrderId());
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus()) // 如果已经是成功，直接返回，不用重复更新
                && Objects.equals(order.getExtensionId(), orderExtension.getId())) {
            log.info("[updateOrderExtensionSuccess][order({}) 已经是已支付，无需更新]", order.getId());
            return true;
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新 PayOrderDO
        int updateCounts = orderMapper.updateByIdAndStatus(order.getId(), PayOrderStatusEnum.WAITING.getStatus(),
                PayOrderDO.builder().status(PayOrderStatusEnum.SUCCESS.getStatus())
                        .channelId(channel.getId()).channelCode(channel.getCode())
                        .successTime(notify.getSuccessTime()).extensionId(orderExtension.getId()).no(orderExtension.getNo())
                        .channelOrderNo(notify.getChannelOrderNo()).channelUserId(notify.getChannelUserId())
                        .channelFeeRate(channel.getFeeRate())
                        .channelFeePrice(MoneyUtils.calculateRatePrice(order.getPrice(), channel.getFeeRate()))
                        .build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderExtensionSuccess][order({}) 更新为已支付]", order.getId());
        return false;
    }

    private void notifyOrderClosed(PayChannelDO channel, PayOrderRespDTO notify) {
        updateOrderExtensionClosed(channel, notify);
    }

    @SuppressWarnings("unused")
    private void updateOrderExtensionClosed(PayChannelDO channel, PayOrderRespDTO notify) {
        // 1. 查询 PayOrderExtensionDO
        PayOrderExtensionDO orderExtension = orderExtensionMapper.selectByNo(notify.getOutTradeNo());
        if (orderExtension == null) {
            throw exception(PAY_ORDER_EXTENSION_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isClosed(orderExtension.getStatus())) { // 如果已经是关闭，直接返回，不用重复更新
            log.info("[updateOrderExtensionClosed][orderExtension({}) 已经是支付关闭，无需更新]", orderExtension.getId());
            return;
        }
        // 一般出现先是支付成功，然后支付关闭，都是全部退款导致关闭的场景。这个情况，我们不更新支付拓展单，只通过退款流程，更新支付单
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
            log.info("[updateOrderExtensionClosed][orderExtension({}) 是已支付，无需更新为支付关闭]", orderExtension.getId());
            return;
        }
        if (ObjectUtil.notEqual(orderExtension.getStatus(), PayOrderStatusEnum.WAITING.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新 PayOrderExtensionDO
        int updateCounts = orderExtensionMapper.updateByIdAndStatus(orderExtension.getId(), orderExtension.getStatus(),
                PayOrderExtensionDO.builder().status(PayOrderStatusEnum.CLOSED.getStatus()).channelNotifyData(toJsonString(notify))
                        .channelErrorCode(notify.getChannelErrorCode()).channelErrorMsg(notify.getChannelErrorMsg()).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderExtensionClosed][orderExtension({}) 更新为支付关闭]", orderExtension.getId());
    }

    @Override
    public void updateOrderRefundPrice(Long id, Integer incrRefundPrice) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.isSuccessOrRefund(order.getStatus())) {
            throw exception(PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
        }
        if (order.getRefundPrice() + incrRefundPrice > order.getPrice()) {
            throw exception(REFUND_PRICE_EXCEED);
        }

        // 更新订单
        PayOrderDO updateObj = new PayOrderDO()
                .setRefundPrice(order.getRefundPrice() + incrRefundPrice)
                .setStatus(PayOrderStatusEnum.REFUND.getStatus());
        int updateCount = orderMapper.updateByIdAndStatus(id, order.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
        }
    }

    @Override
    public void updatePayOrderPrice(Long id, Integer payPrice) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(PayOrderStatusEnum.WAITING.getStatus(), order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        if (ObjectUtil.equal(order.getPrice(), payPrice)) {
            return;
        }

        orderMapper.updateById(new PayOrderDO().setId(order.getId()).setPrice(payPrice));
    }

    @Override
    public PayOrderExtensionDO getOrderExtension(Long id) {
        return orderExtensionMapper.selectById(id);
    }

    @Override
    public PayOrderExtensionDO getOrderExtensionByNo(String no) {
        return orderExtensionMapper.selectByNo(no);
    }

    @Override
    public int syncOrder(LocalDateTime minCreateTime) {
        // 1. 查询指定创建时间前的待支付订单
        List<PayOrderExtensionDO> orderExtensions = orderExtensionMapper.selectListByStatusAndCreateTimeGe(
                PayOrderStatusEnum.WAITING.getStatus(), minCreateTime);
        if (CollUtil.isEmpty(orderExtensions)) {
            return 0;
        }
        // 2. 遍历执行
        int count = 0;
        for (PayOrderExtensionDO orderExtension : orderExtensions) {
            count += syncOrder(orderExtension) ? 1 : 0;
        }
        return count;
    }

    @Override
    public void syncOrderQuietly(Long id) {
        // 1. 查询待支付订单
        List<PayOrderExtensionDO> orderExtensions = orderExtensionMapper.selectListByOrderIdAndStatus(id,
                PayOrderStatusEnum.WAITING.getStatus());

        // 2. 遍历执行
        for (PayOrderExtensionDO orderExtension : orderExtensions) {
            syncOrder(orderExtension);
        }
    }

    /**
     * 同步单个支付拓展单
     *
     * @param orderExtension 支付拓展单
     * @return 是否已支付
     */
    private boolean syncOrder(PayOrderExtensionDO orderExtension) {
        try {
            // 1.1 查询支付订单信息
            PayClient<?> payClient = channelService.getPayClient(orderExtension.getChannelId());
            if (payClient == null) {
                log.error("[syncOrder][渠道编号({}) 找不到对应的支付客户端]", orderExtension.getChannelId());
                return false;
            }
            PayOrderRespDTO respDTO = payClient.getOrder(orderExtension.getNo());
            // 如果查询到订单不存在，PayClient 返回的状态为关闭。但此时不能关闭订单。存在以下一种场景：
            //  拉起渠道支付后，短时间内用户未及时完成支付，但是该订单同步定时任务恰巧自动触发了，主动查询结果为订单不存在。
            //  当用户支付成功之后，该订单状态在渠道的回调中无法从已关闭改为已支付，造成重大影响。
            // 考虑此定时任务是异常场景的兜底操作，因此这里不做变更，优先以回调为准。
            // 让订单自动随着支付渠道那边一起等到过期，确保渠道先过期关闭支付入口，而后通过订单过期定时任务关闭自己的订单。
            if (PayOrderStatusEnum.isClosed(respDTO.getStatus())) {
                return false;
            }
            // 1.2 回调支付结果
            notifyOrder(orderExtension.getChannelId(), respDTO);

            // 2. 如果是已支付，则返回 true
            return PayOrderStatusEnum.isSuccess(respDTO.getStatus());
        } catch (Throwable e) {
            log.error("[syncOrder][orderExtension({}) 同步支付状态异常]", orderExtension.getId(), e);
            return false;
        }
    }

    @Override
    public int expireOrder() {
        // 1. 查询过期的待支付订单
        List<PayOrderDO> orders = orderMapper.selectListByStatusAndExpireTimeLt(
                PayOrderStatusEnum.WAITING.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行
        int count = 0;
        for (PayOrderDO order : orders) {
            count += expireOrder(order) ? 1 : 0;
        }
        return count;
    }

    /**
     * 同步单个支付单
     *
     * @param order 支付单
     * @return 是否已过期
     */
    private boolean expireOrder(PayOrderDO order) {
        try {
            // 1. 需要先处理关联的支付拓展单，避免错误的过期已支付 or 已退款的订单
            List<PayOrderExtensionDO> orderExtensions = orderExtensionMapper.selectListByOrderId(order.getId());
            for (PayOrderExtensionDO orderExtension : orderExtensions) {
                if (PayOrderStatusEnum.isClosed(orderExtension.getStatus())) {
                    continue;
                }
                // 情况一：校验数据库中的 orderExtension 是不是已支付
                if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
                    log.error("[expireOrder][order({}) 的 extension({}) 已支付，可能是数据不一致]",
                            order.getId(), orderExtension.getId());
                    return false;
                }
                // 情况二：调用三方接口，查询支付单状态，是不是已支付/已退款
                PayClient<?> payClient = channelService.getPayClient(orderExtension.getChannelId());
                if (payClient == null) {
                    log.error("[expireOrder][渠道编号({}) 找不到对应的支付客户端]", orderExtension.getChannelId());
                    return false;
                }
                PayOrderRespDTO respDTO = payClient.getOrder(orderExtension.getNo());
                if (PayOrderStatusEnum.isRefund(respDTO.getStatus())) {
                    // 补充说明：按道理，应该是 WAITING => SUCCESS => REFUND 状态，如果直接 WAITING => REFUND 状态，说明中间丢了过程
                    // 此时，需要人工介入，手工补齐数据，保持 WAITING => SUCCESS => REFUND 的过程
                    log.error("[expireOrder][extension({}) 的 PayOrderRespDTO({}) 已退款，可能是回调延迟]",
                            orderExtension.getId(), toJsonString(respDTO));
                    return false;
                }
                if (PayOrderStatusEnum.isSuccess(respDTO.getStatus())) {
                    notifyOrder(orderExtension.getChannelId(), respDTO);
                    return false;
                }
                // 兜底逻辑：将支付拓展单更新为已关闭
                PayOrderExtensionDO updateObj = new PayOrderExtensionDO().setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setChannelNotifyData(toJsonString(respDTO));
                if (orderExtensionMapper.updateByIdAndStatus(orderExtension.getId(), PayOrderStatusEnum.WAITING.getStatus(),
                        updateObj) == 0) {
                    log.error("[expireOrder][extension({}) 更新为支付关闭失败]", orderExtension.getId());
                    return false;
                }
                log.info("[expireOrder][extension({}) 更新为支付关闭成功]", orderExtension.getId());
            }

            // 2. 都没有上述情况，可以安心更新为已关闭
            PayOrderDO updateObj = new PayOrderDO().setStatus(PayOrderStatusEnum.CLOSED.getStatus());
            if (orderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateObj) == 0) {
                log.error("[expireOrder][order({}) 更新为支付关闭失败]", order.getId());
                return false;
            }
            log.info("[expireOrder][order({}) 更新为支付关闭成功]", order.getId());
            return true;
        } catch (Throwable e) {
            log.error("[expireOrder][order({}) 过期订单异常]", order.getId(), e);
            return false;
        }
    }

    @Override
    public void closeOrder(Long id) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null || PayOrderStatusEnum.isClosed(order.getStatus())
                || PayOrderStatusEnum.isRefund(order.getStatus())) {
            return;
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus())) {
            log.error("[closeOrder][order({}) 已支付，订单业务可能已取消，需要人工退款]", id);
            return;
        }
        try {
            // 1. 逐个关闭支付拓展单，避免遗漏重复支付尝试
            List<PayOrderExtensionDO> extensions = orderExtensionMapper.selectListByOrderId(id);
            for (PayOrderExtensionDO extension : extensions) {
                if (PayOrderStatusEnum.isClosed(extension.getStatus())) {
                    continue;
                }
                if (PayOrderStatusEnum.isSuccess(extension.getStatus())) {
                    log.error("[closeOrder][order({}) extension({}) 已支付，订单业务可能已取消，需要人工退款]",
                            id, extension.getId());
                    return;
                }
                if (!PayOrderStatusEnum.isWaiting(extension.getStatus())) {
                    continue;
                }
                PayClient<?> client = channelService.getPayClient(extension.getChannelId());
                if (client == null) {
                    log.error("[closeOrder][order({}) extension({}) 渠道({})不存在]",
                            id, extension.getId(), extension.getChannelId());
                    return;
                }
                // 1.1 调用渠道关单，已支付结果由渠道客户端转换并保留
                PayOrderRespDTO respDTO = client.closeOrder(extension.getNo());
                if (PayOrderStatusEnum.isSuccess(respDTO.getStatus())) {
                    notifyOrder(extension.getChannelId(), respDTO);
                    log.error("[closeOrder][order({}) extension({}) 查询发现已支付]", id, extension.getId());
                    return;
                }
                if (!PayOrderStatusEnum.isClosed(respDTO.getStatus())) {
                    log.warn("[closeOrder][order({}) extension({}) 渠道状态({})，暂不关闭本地支付单]",
                            id, extension.getId(), respDTO.getStatus());
                    return;
                }
                // 1.2 更新 PayOrderExtensionDO 状态
                PayOrderExtensionDO updateObj = new PayOrderExtensionDO()
                        .setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setChannelErrorCode(respDTO.getChannelErrorCode())
                        .setChannelErrorMsg(respDTO.getChannelErrorMsg())
                        .setChannelNotifyData(toJsonString(respDTO));
                if (orderExtensionMapper.updateByIdAndStatus(extension.getId(),
                        PayOrderStatusEnum.WAITING.getStatus(), updateObj) == 0) {
                    log.error("[closeOrder][order({}) extension({}) 更新为支付关闭失败]", id, extension.getId());
                    return;
                }
            }
            // 2. 所有支付拓展单都关闭后，再关闭支付主单
            if (orderMapper.updateByIdAndStatus(id, PayOrderStatusEnum.WAITING.getStatus(),
                    new PayOrderDO().setStatus(PayOrderStatusEnum.CLOSED.getStatus())) == 0) {
                log.error("[closeOrder][order({}) 更新为支付关闭失败]", id);
            }
        } catch (Throwable e) {
            log.error("[closeOrder][order({}) 关闭支付单异常]", id, e);
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
