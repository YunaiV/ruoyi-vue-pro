package cn.iocoder.yudao.framework.social.core;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.social.core.enums.AuthExtendSource;
import cn.iocoder.yudao.framework.social.core.request.AuthWeChatMiniProgramRequest;
import com.xkcoding.http.config.HttpConfig;
import com.xkcoding.justauth.AuthRequestFactory;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

/**
 * 第三方授权拓展request工厂类
 *
 * @author timfruit
 * @date 2021-10-31
 */
public class YudaoAuthRequestFactory extends AuthRequestFactory {
    protected JustAuthProperties properties;
    protected AuthStateCache authStateCache;

    public YudaoAuthRequestFactory(JustAuthProperties properties, AuthStateCache authStateCache) {
        super(properties, authStateCache);
        this.properties = properties;
        this.authStateCache = authStateCache;
    }

    /**
     * 返回AuthRequest对象
     *
     * @param source {@link AuthSource}
     * @return {@link AuthRequest}
     */
    public AuthRequest get(String source) {
        //先尝试获取自定义扩展的
        AuthRequest authRequest = getExtendRequest(source);

        if (authRequest == null) {
            authRequest = super.get(source);
        }

        return authRequest;
    }


    protected AuthRequest getExtendRequest(String source) {
        AuthExtendSource authExtendSource;

        try {
            authExtendSource = EnumUtil.fromString(AuthExtendSource.class, source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 无自定义匹配
            return null;
        }

        // 拓展配置和默认配置齐平，properties放在一起
        AuthConfig config = properties.getType().get(authExtendSource.name());
        // 找不到对应关系，直接返回空
        if (config == null) {
            return null;
        }

        // 配置 http config
        configureHttpConfig(authExtendSource.name(), config, properties.getHttpConfig());

        switch (authExtendSource) {
            case WECHAT_MINI_PROGRAM:
                return new AuthWeChatMiniProgramRequest(config, authStateCache);
            default:
                return null;
        }
    }

    /**
     * 配置 http 相关的配置
     *
     * @param authSource {@link AuthSource}
     * @param authConfig {@link AuthConfig}
     */
    protected void configureHttpConfig(String authSource, AuthConfig authConfig, JustAuthProperties.JustAuthHttpConfig httpConfig) {
        if (null == httpConfig) {
            return;
        }
        Map<String, JustAuthProperties.JustAuthProxyConfig> proxyConfigMap = httpConfig.getProxy();
        if (CollectionUtils.isEmpty(proxyConfigMap)) {
            return;
        }
        JustAuthProperties.JustAuthProxyConfig proxyConfig = proxyConfigMap.get(authSource);

        if (null == proxyConfig) {
            return;
        }

        authConfig.setHttpConfig(HttpConfig.builder()
                .timeout(httpConfig.getTimeout())
                .proxy(new Proxy(Proxy.Type.valueOf(proxyConfig.getType()), new InetSocketAddress(proxyConfig.getHostname(), proxyConfig.getPort())))
                .build());
    }

}
