package cn.iocoder.yudao.coreservice.modules.pay.service.order.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.coreservice.modules.pay.convert.order.PayOrderCoreConvert;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderExtensionCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayAppCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayChannelCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayOrderCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitRespDTO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 支付订单 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PayOrderCoreServiceImpl implements PayOrderCoreService {

    @Resource
    private PayAppCoreService payAppCoreService;
    @Resource
    private PayChannelCoreService payChannelCoreService;

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayOrderCoreMapper payOrderCoreMapper;
    @Resource
    private PayOrderExtensionCoreMapper payOrderExtensionCoreMapper;

    @Override
    public PayOrderDO getPayOrder(Long id) {
        return payOrderCoreMapper.selectById(id);
    }

    @Override
    public Long createPayOrder(PayOrderCreateReqDTO reqDTO) {
        // 校验 App
        PayAppDO app = payAppCoreService.validPayApp(reqDTO.getAppId());

        // 查询对应的支付交易单是否已经存在。如果是，则直接返回
        PayOrderDO order = payOrderCoreMapper.selectByAppIdAndMerchantOrderId(
                reqDTO.getAppId(), reqDTO.getMerchantOrderId());
        if (order != null) {
            log.warn("[createPayOrder][appId({}) merchantOrderId({}) 已经存在对应的支付单({})]", order.getAppId(),
                    order.getMerchantOrderId(), JsonUtils.toJsonString(order)); // 理论来说，不会出现这个情况
            return app.getId();
        }

        // 创建支付交易单
        order = PayOrderCoreConvert.INSTANCE.convert(reqDTO)
                .setMerchantId(app.getMerchantId()).setAppId(app.getId());
        // 商户相关字段
        order.setNotifyUrl(app.getPayNotifyUrl())
                .setNotifyStatus(PayOrderNotifyStatusEnum.NO.getStatus());
        // 订单相关字段
        order.setStatus(PayOrderStatusEnum.WAITING.getStatus());
        // 退款相关字段
        order.setRefundStatus(PayOrderNotifyStatusEnum.NO.getStatus())
                .setRefundTimes(0).setRefundAmount(0L);
        payOrderCoreMapper.insert(order);
        // 最终返回
        return order.getId();
    }

    @Override
    public PayOrderSubmitRespDTO submitPayOrder(PayOrderSubmitReqDTO reqDTO) {
        // 校验 App
        PayAppDO app = payAppCoreService.validPayApp(reqDTO.getAppId());
        // 校验支付渠道是否有效
        PayChannelDO channel = payChannelCoreService.validPayChannel(reqDTO.getAppId(), reqDTO.getChannelCode());
        // 校验支付客户端是否正确初始化
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[submitPayOrder][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }

        // 获得 PayOrderDO ，并校验其是否存在
        PayOrderDO order = payOrderCoreMapper.selectById(reqDTO.getId());
        if (order == null || !Objects.equals(order.getAppId(), reqDTO.getAppId())) { // 是否存在
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }

        // 插入 PayOrderExtensionDO
        PayOrderExtensionDO orderExtension = PayOrderCoreConvert.INSTANCE.convert(reqDTO)
                .setOrderId(order.getId()).setNo(generateOrderExtensionNo())
                .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus());
        payOrderExtensionCoreMapper.insert(orderExtension);

        // 调用三方接口
        PayOrderUnifiedReqDTO unifiedOrderReqDTO = PayOrderCoreConvert.INSTANCE.convert2(reqDTO);
        // 商户相关字段
        unifiedOrderReqDTO.setMerchantOrderId(order.getMerchantOrderId())
                .setSubject(order.getSubject()).setBody(order.getBody())
                .setNotifyUrl(app.getPayNotifyUrl());
        // 订单相关字段
        unifiedOrderReqDTO.setAmount(order.getAmount()).setExpireTime(order.getExpireTime());
        CommonResult<?> unifiedOrderResult = client.unifiedOrder(unifiedOrderReqDTO);
        unifiedOrderResult.checkError();

        // TODO 轮询三方接口，是否已经支付的任务
        // 返回成功
        return new PayOrderSubmitRespDTO().setExtensionId(orderExtension.getId())
                .setInvokeResponse(unifiedOrderResult.getData());
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

}
