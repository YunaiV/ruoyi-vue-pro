package cn.iocoder.yudao.framework.extension.pay.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @description 下单请求
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-30 10:48
 * @class cn.iocoder.yudao.framework.extension.pay.command.TransactionsCommand.java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsCommand implements Serializable {
    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 商品描述
     */
    private String productDescription;

    /**
     * 通知地址
     */
    private String notifyUrl;
}
