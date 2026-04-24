package cn.iocoder.yudao.module.deepay.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Deepay 模块 Security 配置。
 *
 * <p>将前端（yudao-ui-deepay）调用的全部 {@code /api/**} 接口设为无需认证，
 * MVP 阶段采用 X-User-Id 匿名标识而非 JWT，Spring Security 默认会拦截无 Token
 * 请求，必须在此显式 permitAll，否则返回 401。</p>
 *
 * <p>每次新增对外接口时，同步在此声明。</p>
 */
@Configuration(proxyBeanMethods = false, value = "deepaySecurityConfiguration")
public class SecurityConfiguration {

    @Bean("deepayAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                registry.requestMatchers(
                        // ── 设计 / AI 生成 ──────────────────────────────────
                        "/api/design/**",

                        // ── 配额查询 ────────────────────────────────────────
                        "/api/quota/**",

                        // ── 支付 ────────────────────────────────────────────
                        "/api/pay/**",         // 创建支付订单 + 支付 Webhook
                        "/api/payment/**",     // 旧版支付接口（向后兼容）

                        // ── 店铺 / 商品 / 订单 ──────────────────────────────
                        "/api/shop/**",
                        "/api/order/**",
                        "/api/create-product", // 轻量版（向后兼容）
                        "/api/product/**",     // 商品 JSON API
                        "/product/**",         // 商品展示页

                        // ── 用户 / 平台统计 ──────────────────────────────────
                        "/api/user/**",
                        "/api/platform/**",

                        // ── 提现（MVP 阶段匿名标识，后期可改为 authenticated）─
                        "/api/withdraw/**",

                        // ── 功能开关菜单（前端动态路由） ─────────────────────
                        "/api/features",

                        // ── Deepay 内部流水线（无需登录） ────────────────────
                        "/deepay/run",
                        "/deepay/trend",
                        "/deepay/callback/payment",
                        "/deepay/callback/refund",
                        "/deepay/inventory/**",
                        "/deepay/order/**",
                        "/deepay/selection/**"
                ).permitAll();
            }

        };
    }

}
