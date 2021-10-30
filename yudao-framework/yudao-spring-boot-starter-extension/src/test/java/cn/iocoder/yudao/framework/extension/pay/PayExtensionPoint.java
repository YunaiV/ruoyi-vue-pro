package cn.iocoder.yudao.framework.extension.pay;

import cn.iocoder.yudao.framework.extension.core.point.ExtensionPoint;
import cn.iocoder.yudao.framework.extension.pay.command.TransactionsCommand;
import cn.iocoder.yudao.framework.extension.pay.domain.TransactionsResult;

/**
 * @description 支付操作接口
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-30 10:35
 * @class cn.iocoder.yudao.framework.extension.pay.PayExtensionPoint.java
 */
public interface PayExtensionPoint extends ExtensionPoint {

    /**
     * 统一下单：获取"预支付交易会话标识"
     * @param command
     * @return
     */
    TransactionsResult unifiedOrder(TransactionsCommand command);
}
