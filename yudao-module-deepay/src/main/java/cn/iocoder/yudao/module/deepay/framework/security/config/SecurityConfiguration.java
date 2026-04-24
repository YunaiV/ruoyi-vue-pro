package cn.iocoder.yudao.module.deepay.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Deepay 模块 Security 配置。
 *
 * <p>将 {@code /api/create-product} 设为无需认证的公开接口，
 * 方便 MVP 阶段直接通过 curl 或 Postman 测试。</p>
 */
@Configuration(proxyBeanMethods = false, value = "deepaySecurityConfiguration")
public class SecurityConfiguration {

    @Bean("deepayAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                registry.requestMatchers(
                        "/api/create-product",       // 轻量版（向后兼容）
                        "/deepay/run",               // 完整生产流水线
                        "/deepay/trend",             // 趋势查询
                        "/deepay/callback/payment",  // 支付回调（Webhook，无 Token）
                        "/deepay/callback/refund",   // 退款回调
                        "/api/order/create",         // 创建订单 - DeepayDesignController（设计下单）
                        "/api/payment/**",           // 支付相关接口（创建订单/回调/模拟/商品详情）
                        "/product/**",               // 商品展示页（PR#5）
                        "/api/product/**",           // 商品 JSON API（PR#5）
                        "/deepay/inventory/**",      // 库存管理（MVP 阶段）
                        "/deepay/order/**",          // 订单模拟（MVP 阶段）
                        "/api/features"              // 功能菜单列表（前端动态菜单，无需登录）
                ).permitAll();
            }

        };
    }

}
