package cn.iocoder.yudao.module.system.framework.social.config;

import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import com.binarywang.spring.starter.wxjava.miniapp.properties.WxMaProperties;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.WxMpHostConfig;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * WxJava Boot 4 compatibility: bypass starter storage config that still initializes Apache HttpClient 4 builders.
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class YudaoWxClientConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = WxMpProperties.PREFIX + ".config-storage", name = "type", havingValue = "redistemplate")
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnMissingBean(WxMpConfigStorage.class)
    public WxMpConfigStorage wxMpConfigStorage(WxMpProperties properties, ApplicationContext applicationContext) {
        StringRedisTemplate redisTemplate = applicationContext.getBean(StringRedisTemplate.class);
        WxRedisOps redisOps = new RedisTemplateWxRedisOps(redisTemplate);
        WxMpRedisConfigImpl config = new WxMpRedisConfigImpl(redisOps, properties.getConfigStorage().getKeyPrefix());
        applyWxMpConfig(config, properties);
        log.debug("[wxMpConfigStorage][using local Boot 4 compatible WxJava config]");
        return config;
    }

    @Bean
    @ConditionalOnProperty(prefix = WxMaProperties.PREFIX + ".config-storage", name = "type", havingValue = "redistemplate")
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnMissingBean(WxMaConfig.class)
    public WxMaConfig wxMaConfig(WxMaProperties properties, ApplicationContext applicationContext) {
        StringRedisTemplate redisTemplate = applicationContext.getBean(StringRedisTemplate.class);
        WxRedisOps redisOps = new RedisTemplateWxRedisOps(redisTemplate);
        WxMaRedisBetterConfigImpl config = new WxMaRedisBetterConfigImpl(redisOps, properties.getConfigStorage().getKeyPrefix());
        applyWxMaConfig(config, properties);
        log.debug("[wxMaConfig][using local Boot 4 compatible WxJava config]");
        return config;
    }

    private void applyWxMpConfig(WxMpDefaultConfigImpl config, WxMpProperties properties) {
        config.setAppId(properties.getAppId());
        config.setSecret(properties.getSecret());
        config.setToken(properties.getToken());
        config.setAesKey(properties.getAesKey());
        config.setUseStableAccessToken(properties.isUseStableAccessToken());

        WxMpProperties.ConfigStorage storage = properties.getConfigStorage();
        config.setHttpProxyHost(storage.getHttpProxyHost());
        config.setHttpProxyUsername(storage.getHttpProxyUsername());
        config.setHttpProxyPassword(storage.getHttpProxyPassword());
        if (storage.getHttpProxyPort() != null) {
            config.setHttpProxyPort(storage.getHttpProxyPort());
        }

        if (properties.getHosts() != null && StringUtils.isNotEmpty(properties.getHosts().getApiHost())) {
            WxMpHostConfig hostConfig = new WxMpHostConfig();
            hostConfig.setApiHost(properties.getHosts().getApiHost());
            hostConfig.setOpenHost(properties.getHosts().getOpenHost());
            hostConfig.setMpHost(properties.getHosts().getMpHost());
            config.setHostConfig(hostConfig);
        }
    }

    private void applyWxMaConfig(WxMaDefaultConfigImpl config, WxMaProperties properties) {
        WxMaProperties.ConfigStorage storage = properties.getConfigStorage();
        config.setAppid(StringUtils.trimToNull(properties.getAppid()));
        config.setSecret(StringUtils.trimToNull(properties.getSecret()));
        config.setToken(StringUtils.trimToNull(properties.getToken()));
        config.setAesKey(StringUtils.trimToNull(properties.getAesKey()));
        config.setMsgDataFormat(StringUtils.trimToNull(properties.getMsgDataFormat()));
        config.useStableAccessToken(properties.isUseStableAccessToken());
        config.setApiHostUrl(StringUtils.trimToNull(properties.getApiHostUrl()));
        config.setAccessTokenUrl(StringUtils.trimToNull(properties.getAccessTokenUrl()));

        config.setHttpProxyHost(storage.getHttpProxyHost());
        config.setHttpProxyUsername(storage.getHttpProxyUsername());
        config.setHttpProxyPassword(storage.getHttpProxyPassword());
        if (storage.getHttpProxyPort() != null) {
            config.setHttpProxyPort(storage.getHttpProxyPort());
        }

        config.setRetrySleepMillis(Math.max(storage.getRetrySleepMillis(), 1000));
        config.setMaxRetryTimes(Math.max(storage.getMaxRetryTimes(), 0));
    }

}
