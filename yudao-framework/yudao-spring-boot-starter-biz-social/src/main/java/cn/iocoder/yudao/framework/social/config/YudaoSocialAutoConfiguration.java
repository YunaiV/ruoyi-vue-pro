package cn.iocoder.yudao.framework.social.config;

import cn.iocoder.yudao.framework.social.core.YudaoAuthRequestFactory;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 社交自动装配类
 *
 * @author timfruit
 * @date 2021-10-30
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(JustAuthProperties.class)
public class YudaoSocialAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "justauth", value = "enabled", havingValue = "true", matchIfMissing = true)
    public YudaoAuthRequestFactory yudaoAuthRequestFactory(JustAuthProperties properties, AuthStateCache authStateCache) {
        return new YudaoAuthRequestFactory(properties, authStateCache);
    }

}
