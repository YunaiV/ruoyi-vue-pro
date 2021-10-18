package cn.iocoder.yudao.coreservice.modules.pay.service.order.impl;

import cn.iocoder.yudao.coreservice.modules.pay.convert.order.PayOrderCoreServiceConvert;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayAppCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayOrderCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitRespDTO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 支付订单 Core Service 实现类
 */
@Service
@Valid
@Slf4j
public class PayOrderCoreServiceImpl implements PayOrderCoreService {

    @Resource
    private PayAppCoreService payAppCoreService;

    @Resource
    private PayOrderCoreMapper payOrderCoreMapper;

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
        order = PayOrderCoreServiceConvert.INSTANCE.convert(reqDTO)
                .setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setNotifyUrl(app.getPayNotifyUrl());
        payOrderCoreMapper.insert(order);
        // 最终返回
        return order.getId();
    }

    @Override
    public PayOrderSubmitRespDTO submitPayOrder(PayOrderSubmitReqDTO reqDTO) {
        // 校验 App
        PayAppDO app = payAppCoreService.validPayApp(reqDTO.getId());
        // TODO 校验支付渠道是否有效

        // 获得 PayOrderDO ，并校验其是否存在
        PayOrderDO order = payOrderCoreMapper.selectById(reqDTO.getId());
        if (order == null) { // 是否存在
            throw exception(PAY_TRANSACTION_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_TRANSACTION_STATUS_IS_NOT_WAITING);
        }

        // 插入 PayTransactionExtensionDO
        PayTransactionExtensionDO payTransactionExtensionDO = PayTransactionConvert.INSTANCE.convert(submitReqDTO)
                .setTransactionId(payTransaction.getId()).setTransactionCode(generateTransactionCode())
                .setStatus(PayTransactionStatusEnum.WAITING.getStatus());
        payTransactionExtensionMapper.insert(payTransactionExtensionDO);

        // 调用三方接口
        AbstractThirdPayClient thirdPayClient = ThirdPayClientFactory.getThirdPayClient(submitReqDTO.getPayChannel());
        CommonResult<String> invokeResult = thirdPayClient.submitTransaction(payTransaction, payTransactionExtensionDO, null); // TODO 暂时传入 extra = null
        invokeResult.checkError();

        // TODO 轮询三方接口，是否已经支付的任务
        // 返回成功
        return new PayTransactionSubmitRespDTO().setId(payTransactionExtensionDO.getId()).setInvokeResponse(invokeResult.getData());
    }

}
