package cn.iocoder.yudao.module.trade.framework.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

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
     * 默认应用标识
     */
    private static final String APP_KEY_DEFAULT = "mall";

    /**
     * 应用标识，用于区分不同的应用程序
     * 通过注解@NotNull确保应用标识不能为空
     */
    @NotNull(message = "应用标识不能为空")
    private String appKey = APP_KEY_DEFAULT;

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
