package cn.iocoder.yudao.module.trade.framework.delivery.config;

import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProviderEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * 交易快递查询的配置项
 *
 * @author jason
 */
@Component
@ConfigurationProperties(prefix = "yudao.trade.express.query")
@Data
@Validated
public class TradeExpressQueryProperties {

    /**
     * 快递查询服务商, 如果未配置，默认使用快递鸟
     */
    private ExpressQueryProviderEnum expressQueryProvider;
    /**
     * 快递鸟配置
     */
    @Valid
    private KdNiaoConfig kdNiao;
    /**
     * 快递 100 配置
     */
    @Valid
    private Kd100Config kd100;

    /**
     * 快递鸟配置项目
     */
    @Data
    public static class KdNiaoConfig {

        /**
         * 快递鸟用户 ID
         */
        @NotEmpty(message = "快递鸟用户 ID 配置项不能为空")
        private String businessId;

        /**
         * 快递鸟 API Key
         */
        @NotEmpty(message = "快递鸟 Api Key 配置项不能为空")
        private String apiKey;
    }

    /**
     * 快递100 配置项
     */
    @Data
    public static class Kd100Config {
        /**
         * 快递 100 授权码
         */
        @NotEmpty(message = "快递 100 授权码配置项不能为空")
        private String customer;
        /**
         * 快递 100 授权 key
         */
        @NotEmpty(message = "快递 100 授权 Key 配置项不能为空")
        private String key;
    }


}
