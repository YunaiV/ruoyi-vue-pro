package cn.iocoder.yudao.module.deepay.config;

import cn.iocoder.yudao.module.deepay.client.ima.ImaClient;
import cn.iocoder.yudao.module.deepay.client.ima.ImaProperties;
import cn.iocoder.yudao.module.deepay.service.ima.ImaService;
import cn.iocoder.yudao.module.deepay.service.ima.ImaServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ima 知识库自动配置。
 *
 * <p>仅当 {@code deepay.ima.enabled=true} 时才创建 {@link ImaService} Bean，
 * 否则 {@link cn.iocoder.yudao.module.deepay.agent.ImaAgent} 会跳过所有 ima 操作。</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ImaProperties.class)
@EnableScheduling
public class ImaAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "deepay.ima", name = "enabled", havingValue = "true")
    public ImaClient imaClient(ImaProperties props) {
        return new ImaClient(props.getBaseUrl(), props.getApiKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "deepay.ima", name = "enabled", havingValue = "true")
    public ImaService imaService(ImaClient imaClient) {
        return new ImaServiceImpl(imaClient);
    }

}
