package cn.iocoder.yudao.framework.extension.pay.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.extension.core.stereotype.Extension;
import cn.iocoder.yudao.framework.extension.pay.PayExtensionPoint;
import cn.iocoder.yudao.framework.extension.pay.command.TransactionsCommand;
import cn.iocoder.yudao.framework.extension.pay.domain.TransactionsResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @description 微信 JSAPI 支付
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-30 10:38
 * @class cn.iocoder.yudao.framework.extension.pay.impl.AlipayService.java
 */
@Extension(businessId = "pay", useCase = "jsapi", scenario = "alipay")
@Slf4j
public class AlipayService implements PayExtensionPoint {
    @Override
    public TransactionsResult unifiedOrder(TransactionsCommand command) {
        log.info("微信 JSAPI 支付：{}", JSONUtil.toJsonStr(command));
        return new TransactionsResult("alipay26112221580621e9b071c00d9e093b0000", command.getOrderNo(), IdUtil.objectId(), "alipay");
    }
}
