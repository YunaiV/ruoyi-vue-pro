package cn.iocoder.yudao.module.pay.service.refund;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
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
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

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
    private PayRefundMapper refundMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;

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
    public PayRefundDO getRefundByNo(String no) {
        return refundMapper.selectByNo(no);
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
    public Long createPayRefund(PayRefundCreateReqDTO reqDTO) {
        // 1.1 校验 App
        PayAppDO app = appService.validPayApp(reqDTO.getAppId());
        // 1.2 校验支付订单
        PayOrderDO order = validatePayOrderCanRefund(reqDTO);
        // 1.3 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(order.getChannelId());
        PayClient client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[refund][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 1.4 校验退款订单是否已经存在
        PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(
                app.getId(), reqDTO.getMerchantRefundId());
        if (refund != null) {
            throw exception(REFUND_EXISTS);
        }

        // 2.1 插入退款单
        String no = noRedisDAO.generate(payProperties.getRefundNoPrefix());
        refund = PayRefundConvert.INSTANCE.convert(reqDTO)
                .setNo(no).setOrderId(order.getId()).setOrderNo(order.getNo())
                .setChannelId(order.getChannelId()).setChannelCode(order.getChannelCode())
                // 商户相关的字段
                .setNotifyUrl(app.getRefundNotifyUrl())
                // 渠道相关字段
                .setChannelOrderNo(order.getChannelOrderNo())
                // 退款相关字段
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setPayPrice(order.getPrice()).setRefundPrice(reqDTO.getPrice());
        refundMapper.insert(refund);
        try {
            // 2.2 向渠道发起退款申请
            PayRefundUnifiedReqDTO unifiedReqDTO = new PayRefundUnifiedReqDTO()
                    .setPayPrice(order.getPrice())
                    .setRefundPrice(reqDTO.getPrice())
                    .setOutTradeNo(order.getNo())
                    .setOutRefundNo(refund.getNo())
                    .setNotifyUrl(genChannelRefundNotifyUrl(channel))
                    .setReason(reqDTO.getReason());
            PayRefundRespDTO refundRespDTO = client.unifiedRefund(unifiedReqDTO);
            // 2.3 处理退款返回
            getSelf().notifyRefund(channel, refundRespDTO);
        } catch (Throwable e) {
            // 注意：这里仅打印异常，不进行抛出。
            // 原因是：虽然调用支付渠道进行退款发生异常（网络请求超时），实际退款成功。这个结果，后续通过退款回调、或者退款轮询补偿可以拿到。
            // 最终，在异常的情况下，支付中心会异步回调业务的退款回调接口，提供退款结果
            log.error("[createPayRefund][退款 id({}) requestDTO({}) 发生异常]",
                    refund.getId(), reqDTO, e);
        }

        // 返回退款编号
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
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        // 校验状态，必须是已支付、或者已退款
        if (!PayOrderStatusEnum.isSuccessOrRefund(order.getStatus())) {
            throw exception(PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
        }

        // 校验金额，退款金额不能大于原定的金额
        if (reqDTO.getPrice() + order.getRefundPrice() > order.getPrice()){
            throw exception(REFUND_PRICE_EXCEED);
        }
        // 是否有退款中的订单
        if (refundMapper.selectCountByAppIdAndOrderId(reqDTO.getAppId(), order.getId(),
                PayRefundStatusEnum.WAITING.getStatus()) > 0) {
            throw exception(REFUND_HAS_REFUNDING);
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

    @Override
    public void notifyRefund(Long channelId, PayRefundRespDTO notify) {
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 更新退款订单
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyRefund(channel, notify));
    }

    /**
     * 通知并更新订单的退款结果
     *
     * @param channel 支付渠道
     * @param notify 通知
     */
    @Transactional(rollbackFor = Exception.class)  // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyRefund(channel, notify) 调用，否则事务不生效
    public void notifyRefund(PayChannelDO channel, PayRefundRespDTO notify) {
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

    private void notifyRefundSuccess(PayChannelDO channel, PayRefundRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isSuccess(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款成功，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setSuccessTime(notify.getSuccessTime())
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setChannelNotifyData(toJsonString(notify));
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundSuccess][退款订单({}) 更新为退款成功]", refund.getId());

        // 2. 更新订单
        orderService.updateOrderRefundPrice(refund.getOrderId(), refund.getRefundPrice());

        // 3. 插入退款通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
                refund.getId());
    }

    private void notifyRefundFailure(PayChannelDO channel, PayRefundRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isFailure(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款关闭，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelNotifyData(toJsonString(notify))
                .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg());
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundFailure][退款订单({}) 更新为退款失败]", refund.getId());

        // 2. 插入退款通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
                refund.getId());
    }

    @Override
    public int syncRefund() {
        // 1. 查询指定创建时间内的待退款订单
        List<PayRefundDO> refunds = refundMapper.selectListByStatus(PayRefundStatusEnum.WAITING.getStatus());
        if (CollUtil.isEmpty(refunds)) {
            return 0;
        }
        // 2. 遍历执行
        int count = 0;
        for (PayRefundDO refund : refunds) {
            count += syncRefund(refund) ? 1 : 0;
        }
        return count;
    }

    /**
     * 同步单个退款订单
     *
     * @param refund 退款订单
     * @return 是否同步到
     */
    private boolean syncRefund(PayRefundDO refund) {
        try {
            // 1.1 查询退款订单信息
            PayClient payClient = channelService.getPayClient(refund.getChannelId());
            if (payClient == null) {
                log.error("[syncRefund][渠道编号({}) 找不到对应的支付客户端]", refund.getChannelId());
                return false;
            }
            PayRefundRespDTO respDTO = payClient.getRefund(refund.getOrderNo(), refund.getNo());
            // 1.2 回调退款结果
            notifyRefund(refund.getChannelId(), respDTO);

            // 2. 如果同步到，则返回 true
            return PayRefundStatusEnum.isSuccess(respDTO.getStatus())
                    || PayRefundStatusEnum.isFailure(respDTO.getStatus());
        } catch (Throwable e) {
            log.error("[syncRefund][refund({}) 同步退款状态异常]", refund.getId(), e);
            return false;
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayRefundServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
