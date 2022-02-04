package cn.iocoder.yudao.module.pay.service.order;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.config.PayProperties;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.pay.convert.order.PayOrderConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderExtensionMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.service.merchant.PayAppService;
import cn.iocoder.yudao.module.pay.service.merchant.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.notify.dto.PayNotifyTaskCreateReqDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.service.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.module.pay.service.order.dto.PayOrderSubmitRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    public Long createPayOrder(PayOrderCreateReqDTO reqDTO) {
        // 校验 App
        PayAppDO app = appService.validPayApp(reqDTO.getAppId());

        // 查询对应的支付交易单是否已经存在。如果是，则直接返回
        PayOrderDO order = orderMapper.selectByAppIdAndMerchantOrderId(
                reqDTO.getAppId(), reqDTO.getMerchantOrderId());
        if (order != null) {
            log.warn("[createPayOrder][appId({}) merchantOrderId({}) 已经存在对应的支付单({})]", order.getAppId(),
                    order.getMerchantOrderId(), JsonUtils.toJsonString(order)); // 理论来说，不会出现这个情况
            return app.getId();
        }

        // 创建支付交易单
        order = PayOrderConvert.INSTANCE.convert(reqDTO)
                .setMerchantId(app.getMerchantId()).setAppId(app.getId());
        // 商户相关字段
        order.setNotifyUrl(app.getPayNotifyUrl())
                .setNotifyStatus(PayOrderNotifyStatusEnum.NO.getStatus());
        // 订单相关字段
        order.setStatus(PayOrderStatusEnum.WAITING.getStatus());
        // 退款相关字段
        // todo @芋艿 创建支付的订单的退款状态枚举是不是有问题，应该是 PayRefundTypeEnum 吧 您这填写的是 PayOrderNotifyStatusEnum 回调状态枚举
        order.setRefundStatus(PayOrderNotifyStatusEnum.NO.getStatus())
                .setRefundTimes(0).setRefundAmount(0L);
        orderMapper.insert(order);
        // 最终返回
        return order.getId();
    }

    @Override
    public PayOrderSubmitRespDTO submitPayOrder(PayOrderSubmitReqDTO reqDTO) {
        // 校验 App
        appService.validPayApp(reqDTO.getAppId());
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(reqDTO.getAppId(), reqDTO.getChannelCode());
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[submitPayOrder][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_CHANNEL_CLIENT_NOT_FOUND);
        }

        // 获得 PayOrderDO ，并校验其是否存在
        PayOrderDO order = orderMapper.selectById(reqDTO.getId());
        if (order == null || !Objects.equals(order.getAppId(), reqDTO.getAppId())) { // 是否存在
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_STATUS_IS_NOT_WAITING);
        }

        // 插入 PayOrderExtensionDO
        PayOrderExtensionDO orderExtension = PayOrderConvert.INSTANCE.convert(reqDTO)
                .setOrderId(order.getId()).setNo(generateOrderExtensionNo())
                .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus());
        orderExtensionMapper.insert(orderExtension);

        // 调用三方接口
        PayOrderUnifiedReqDTO unifiedOrderReqDTO = PayOrderConvert.INSTANCE.convert2(reqDTO);
        // 商户相关字段
        //TODO jason @芋艿 是否加一个属性  如tradeNo 支付订单号， 用这个merchantOrderId让人迷糊
        unifiedOrderReqDTO.setMerchantOrderId(orderExtension.getNo()) // 注意，此处使用的是 PayOrderExtensionDO.no 属性！
                .setSubject(order.getSubject()).setBody(order.getBody())
                .setNotifyUrl(genChannelPayNotifyUrl(channel))
                .setReturnUrl(genChannelReturnUrl(channel));
        // 订单相关字段
        unifiedOrderReqDTO.setAmount(order.getAmount()).setExpireTime(order.getExpireTime());
        CommonResult<?> unifiedOrderResult = client.unifiedOrder(unifiedOrderReqDTO);
        unifiedOrderResult.checkError();

        // TODO 轮询三方接口，是否已经支付的任务
        // 返回成功
        return new PayOrderSubmitRespDTO().setExtensionId(orderExtension.getId())
                .setInvokeResponse(unifiedOrderResult.getData());
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的返回地址
     * @param channel 支付渠道
     * @return 支付成功返回的地址。 配置地址 + "/" + channel id
     */
    private String genChannelReturnUrl(PayChannelDO channel) {
        return payProperties.getPayReturnUrl() + "/" + channel.getId();
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelPayNotifyUrl(PayChannelDO channel) {
        //去掉channel code, 似乎没啥用， 用统一的回调地址
        return payProperties.getPayNotifyUrl() + "/" + channel.getId();
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
        return DateUtil.format(new Date(), "yyyyMMddHHmmss") + // 时间序列
                RandomUtil.randomInt(100000, 999999) // 随机。为什么是这个范围，因为偷懒
                ;
    }

    @Override
    @Transactional
    public void notifyPayOrder(Long channelId,  PayNotifyDataDTO notifyData) throws Exception {
        // TODO 芋艿，记录回调日志
        log.info("[notifyPayOrder][channelId({}) 回调数据({})]", channelId, notifyData.getBody());

        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[notifyPayOrder][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_CHANNEL_CLIENT_NOT_FOUND);
        }

        // 解析支付结果
        PayOrderNotifyRespDTO notifyRespDTO = client.parseOrderNotify(notifyData);

        // TODO 芋艿，先最严格的校验。即使调用方重复调用，实际哪个订单已经被重复回调的支付，也返回 false 。也没问题，因为实际已经回调成功了。
        // 1.1 查询 PayOrderExtensionDO
        PayOrderExtensionDO orderExtension = orderExtensionMapper.selectByNo(notifyRespDTO.getOrderExtensionNo());
        if (orderExtension == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_EXTENSION_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(orderExtension.getStatus())) { // 校验状态，必须是待支付
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayOrderExtensionDO
        //TODO 支付宝交易超时 TRADE_FINISHED 需要更新交易关闭
        int updateCounts = orderExtensionMapper.updateByIdAndStatus(orderExtension.getId(),
                PayOrderStatusEnum.WAITING.getStatus(), PayOrderExtensionDO.builder().id(orderExtension.getId())
                        .status(PayOrderStatusEnum.SUCCESS.getStatus()).channelNotifyData(notifyData.getBody()).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyPayOrder][支付拓展单({}) 更新为已支付]", orderExtension.getId());

        // 2.1 判断 PayOrderDO 是否处于待支付
        PayOrderDO order = orderMapper.selectById(orderExtension.getOrderId());
        if (order == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        // 2.2 更新 PayOrderDO
        updateCounts = orderMapper.updateByIdAndStatus(order.getId(), PayOrderStatusEnum.WAITING.getStatus(),
                PayOrderDO.builder().status(PayOrderStatusEnum.SUCCESS.getStatus()).channelId(channelId).channelCode(channel.getCode())
                        .successTime(notifyRespDTO.getSuccessTime()).successExtensionId(orderExtension.getId())
                        .channelOrderNo(notifyRespDTO.getChannelOrderNo()).channelUserId(notifyRespDTO.getChannelUserId())
                        .notifyTime(new Date()).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyPayOrder][支付订单({}) 更新为已支付]", order.getId());

        // 3. 插入支付通知记录
        notifyService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
                .type(PayNotifyTypeEnum.ORDER.getType()).dataId(order.getId()).build());
    }

}
