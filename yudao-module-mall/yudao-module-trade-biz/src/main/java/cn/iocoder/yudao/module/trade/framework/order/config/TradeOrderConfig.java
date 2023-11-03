package cn.iocoder.yudao.module.trade.framework.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// TODO @LeeYan9: 可以直接给 TradeOrderProperties 一个 @Component生效哈
/**
 * @author LeeYan9
 * @since 2022-09-15
 */
@Configuration
@EnableConfigurationProperties(TradeOrderProperties.class)
public class TradeOrderConfig {
}
