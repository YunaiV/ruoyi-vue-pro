package cn.iocoder.yudao.module.trade.framework.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author LeeYan9
 * @since 2022-09-15
 */
@ConfigurationProperties(prefix = "yudao.trade.order")
@Data
@Validated
public class TradeOrderProperties {

    /**
     * 商户订单编号
     */
    @NotNull(message = "商户订单编号不能为空")
    private String merchantOrderId;

    /**
     * 应用编号
     */
    @NotNull(message = "应用编号不能为空")
    private Long appId;

}
