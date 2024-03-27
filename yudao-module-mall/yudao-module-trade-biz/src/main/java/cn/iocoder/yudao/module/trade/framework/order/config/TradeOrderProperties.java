package cn.iocoder.yudao.module.trade.framework.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

/**
 * 交易订单的配置项
 *
 * @author LeeYan9
 * @since 2022-09-15
 */
@ConfigurationProperties(prefix = "yudao.trade.order")
@Data
@Validated
public class TradeOrderProperties {

    /**
     * 应用编号
     */
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    /**
     * 支付超时时间
     */
    @NotNull(message = "支付超时时间不能为空")
    private Duration payExpireTime;

    /**
     * 收货超时时间
     */
    @NotNull(message = "收货超时时间不能为空")
    private Duration receiveExpireTime;

    /**
     * 评论超时时间
     */
    @NotNull(message = "评论超时时间不能为空")
    private Duration commentExpireTime;

}
